import React, { useState, useEffect, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getOrderById } from '../api/orderApi';
import { useCart } from '../context/CartContext';
import LoadingSpinner from '../components/LoadingSpinner';
import LiveMap from '../components/LiveMap';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '../config/apiConfig';
import { Clock, CheckCircle2, ChefHat, Truck, Home, ArrowLeft, RefreshCw, PhoneCall, Star, Bike, MapPin, ShieldCheck, Wifi, WifiOff } from 'lucide-react';

const OrderTrackingPage = () => {
  const { id } = useParams();
  const { deliveryLocation } = useCart();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lastUpdated, setLastUpdated] = useState(new Date());
  const [etaMinutes, setEtaMinutes] = useState(25);
  const [wsConnected, setWsConnected] = useState(false);
  const { setDeliveryLocation } = useCart();
  const baseLat = deliveryLocation?.lat || 28.6245;
  const baseLng = deliveryLocation?.lng || 77.2140;

  const [activeCoords, setActiveCoords] = useState({ lat: baseLat, lng: baseLng });

  useEffect(() => {
    let isMounted = true;
    if (deliveryLocation?.address && (deliveryLocation.lat === 28.6245 || !deliveryLocation.lat) && !deliveryLocation.address.toLowerCase().includes('connaught')) {
      fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(deliveryLocation.address)}&limit=1`, {
        headers: { 'User-Agent': 'QuickEats-Portfolio-Project/1.0 (Contact: localdev@quickeats.app)' }
      })
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0 && isMounted) {
          const newLat = parseFloat(data[0].lat);
          const newLng = parseFloat(data[0].lon);
          setActiveCoords({ lat: newLat, lng: newLng });
          if (setDeliveryLocation) {
            setDeliveryLocation(prev => ({ ...prev, lat: newLat, lng: newLng }));
          }
        }
      })
      .catch(err => console.warn('Geocoding fallback in tracking page failed:', err));
    } else if (deliveryLocation?.lat && deliveryLocation?.lng) {
      setActiveCoords({ lat: deliveryLocation.lat, lng: deliveryLocation.lng });
    }
    return () => { isMounted = false; };
  }, [deliveryLocation?.address]);

  const customerLat = activeCoords.lat;
  const customerLng = activeCoords.lng;
  const customerCoords = [customerLat, customerLng];
  const restaurantCoords = [customerLat + 0.008, customerLng + 0.006];

  const [riderLocation, setRiderLocation] = useState({ lat: customerLat + 0.008, lng: customerLng + 0.006 });

  const stompClientRef = useRef(null);
  const pollingIntervalRef = useRef(null);

  const statuses = [
    { key: 'PENDING', label: 'Order Placed', icon: Clock },
    { key: 'PREPARING', label: 'Kitchen Preparing', icon: ChefHat },
    { key: 'OUT_FOR_DELIVERY', label: 'Rider On The Way', icon: Truck },
    { key: 'DELIVERED', label: 'Delivered', icon: Home }
  ];

  const fetchOrderDetails = async () => {
    try {
      const data = await getOrderById(id);
      setOrder(data);
      setLastUpdated(new Date());
    } catch (err) {
      setError('Failed to fetch order details. Please check the order ID.');
    } finally {
      setLoading(false);
    }
  };

  // Initial Fetch
  useEffect(() => {
    fetchOrderDetails();
  }, [id]);

  // WebSocket Subscription with Polling Fallback
  useEffect(() => {
    let client = null;

    const setupWebSocket = () => {
      try {
        client = new Client({
          webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            setWsConnected(true);
            if (pollingIntervalRef.current) {
              clearInterval(pollingIntervalRef.current);
              pollingIntervalRef.current = null;
            }

            // Subscribe to order topic
            client.subscribe(`/topic/orders/${id}`, (message) => {
              try {
                const updateData = JSON.parse(message.body);
                setOrder(prev => prev ? { ...prev, status: updateData.status } : prev);
                if (updateData.etaMinutes !== undefined) setEtaMinutes(updateData.etaMinutes);
                if (updateData.lat && updateData.lng) {
                  // Calculate dynamic rider position relative to user's real delivery location
                  const eta = updateData.etaMinutes !== undefined ? updateData.etaMinutes : 20;
                  const progress = Math.max(0, Math.min(1.0, (20 - eta) / 20.0));
                  const currentRiderLat = (customerLat + 0.008) + (customerLat - (customerLat + 0.008)) * progress;
                  const currentRiderLng = (customerLng + 0.006) + (customerLng - (customerLng + 0.006)) * progress;
                  setRiderLocation({ lat: currentRiderLat, lng: currentRiderLng });
                }
                setLastUpdated(new Date());
              } catch (e) {
                console.error('Error parsing STOMP message:', e);
              }
            });
          },
          onStompError: (frame) => {
            console.warn('STOMP error, falling back to polling:', frame);
            setWsConnected(false);
            startPollingFallback();
          },
          onWebSocketClose: () => {
            setWsConnected(false);
            startPollingFallback();
          }
        });

        client.activate();
        stompClientRef.current = client;
      } catch (err) {
        console.warn('Failed to initiate WebSocket, falling back to polling:', err);
        setWsConnected(false);
        startPollingFallback();
      }
    };

    const startPollingFallback = () => {
      if (!pollingIntervalRef.current) {
        pollingIntervalRef.current = setInterval(() => {
          if (order?.status !== 'DELIVERED') {
            fetchOrderDetails();
          }
        }, 10000);
      }
    };

    setupWebSocket();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current);
      }
    };
  }, [id, order?.status]);

  const getCurrentStepIndex = () => {
    if (!order?.status) return 0;
    const idx = statuses.findIndex(s => s.key === order.status);
    return idx >= 0 ? idx : 0;
  };

  if (loading) return <LoadingSpinner message="Connecting to live GPS map & order stream..." />;
  if (error) {
    return (
      <div className="max-w-md mx-auto my-12 p-8 bg-red-50 border border-red-200 rounded-3xl text-center text-red-700 space-y-3">
        <p className="font-bold">{error}</p>
        <Link to="/" className="inline-block text-xs font-bold text-slate-800 underline">
          Back to Home
        </Link>
      </div>
    );
  }

  const currentStep = getCurrentStepIndex();

  return (
    <div className="max-w-3xl mx-auto space-y-8 pb-16">
      
      {/* Navigation */}
      <div className="flex items-center justify-between">
        <Link
          to="/"
          className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-orange-600 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Home</span>
        </Link>

        {/* Real-time connection badge */}
        <div className={`px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1.5 border ${
          wsConnected
            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
            : 'bg-amber-50 text-amber-700 border-amber-200'
        }`}>
          {wsConnected ? <Wifi className="w-3.5 h-3.5 text-emerald-600 animate-pulse" /> : <WifiOff className="w-3.5 h-3.5 text-amber-600" />}
          <span>{wsConnected ? 'STOMP Live GPS Active' : 'Polling Fallback'}</span>
        </div>
      </div>

      {/* Header Card */}
      <div className="bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white rounded-3xl p-6 sm:p-8 shadow-xl flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 sm:gap-6 relative overflow-hidden">
        <div className="space-y-2 relative z-10">
          <span className="bg-orange-500/20 border border-orange-500/30 text-orange-400 font-bold text-xs px-3 py-1 rounded-full uppercase tracking-wider">
            Order #{order.id}
          </span>
          <h1 className="text-2xl sm:text-3xl lg:text-4xl font-black tracking-tight">Live Real-Time Delivery</h1>
          <p className="text-xs text-slate-300">From <span className="text-white font-bold">{order.restaurantName}</span></p>
        </div>

        {/* Live ETA Card */}
        <div className="bg-white/10 backdrop-blur-md border border-white/20 p-3.5 sm:p-4 rounded-2xl flex items-center gap-3 sm:gap-4 shrink-0 relative z-10 w-full sm:w-auto">
          <div className="w-10 h-10 sm:w-12 sm:h-12 rounded-2xl bg-orange-600 text-white flex items-center justify-center font-black text-lg sm:text-xl shadow-lg shrink-0">
            {order?.status === 'DELIVERED' ? '0' : etaMinutes}
          </div>
          <div>
            <p className="text-[10px] sm:text-xs text-slate-300 font-bold uppercase tracking-wider">Estimated Arrival</p>
            <p className="text-sm sm:text-base font-black text-white">
              {order?.status === 'DELIVERED' ? 'Arrived & Delivered!' : `${etaMinutes} Mins`}
            </p>
          </div>
        </div>
      </div>

      {/* Live Swiggy/Zomato Delivery Partner Card */}
      <div className="bg-gradient-to-r from-orange-500 via-amber-500 to-orange-600 text-white p-5 sm:p-6 rounded-3xl shadow-lg flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 sm:gap-6">
        <div className="flex items-center gap-3.5 sm:gap-4">
          <div className="w-12 h-12 sm:w-14 sm:h-14 rounded-2xl bg-white/20 backdrop-blur-md border border-white/30 flex items-center justify-center text-white font-bold text-xl sm:text-2xl shadow-inner shrink-0">
            <Bike className="w-7 h-7 sm:w-8 sm:h-8" />
          </div>
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h3 className="text-base sm:text-lg font-black tracking-tight">Ramesh Kumar</h3>
              <span className="bg-white/20 text-white font-bold text-[10px] px-2 py-0.5 rounded-md flex items-center gap-1">
                <Star className="w-3 h-3 fill-amber-300 text-amber-300" /> 4.9
              </span>
            </div>
            <p className="text-[11px] sm:text-xs text-orange-100 font-medium flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-emerald-300 shrink-0" />
              <span>Verified Delivery Partner • TVS Jupiter</span>
            </p>
          </div>
        </div>

        <a
          href="tel:+919876543210"
          className="w-full sm:w-auto py-2.5 sm:py-3 px-5 bg-white text-orange-700 hover:bg-orange-50 font-black text-xs rounded-2xl shadow-md flex items-center justify-center gap-2 transition-all shrink-0 active:scale-95"
        >
          <PhoneCall className="w-4 h-4 text-orange-600" />
          <span>Call Rider</span>
        </a>
      </div>

      {/* Live Leaflet Map Component (OpenStreetMap Tiles) */}
      <div className="space-y-2">
        <div className="flex items-center justify-between px-1">
          <span className="text-xs font-extrabold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
            <MapPin className="w-4 h-4 text-orange-600" /> Live GPS Rider Map
          </span>
          <span className="text-[10px] text-slate-400 font-bold">OpenStreetMap • Free Tiles</span>
        </div>
        <LiveMap
          riderLocation={riderLocation}
          restaurantCoords={restaurantCoords}
          customerCoords={customerCoords}
          customerAddress={deliveryLocation?.address || 'Your Current Delivery Location'}
        />
      </div>

      {/* Live Horizontal Status Stepper */}
      <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-sm space-y-6">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Tracking Progression</h2>
          <span className="text-[10px] text-slate-400 font-bold flex items-center gap-1">
            <RefreshCw className="w-3 h-3 animate-spin text-orange-600" />
            Updated {lastUpdated.toLocaleTimeString()}
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 relative">
          {statuses.map((s, idx) => {
            const Icon = s.icon;
            const isDone = idx <= currentStep;
            const isCurrent = idx === currentStep;

            return (
              <div
                key={s.key}
                className={`flex flex-col items-center text-center p-4 rounded-2xl transition-all ${
                  isCurrent
                    ? 'bg-orange-50 border-2 border-orange-500 shadow-md scale-105'
                    : isDone
                    ? 'bg-slate-50 border border-slate-200'
                    : 'opacity-40 bg-slate-50'
                }`}
              >
                <div
                  className={`w-12 h-12 rounded-2xl flex items-center justify-center font-bold mb-3 ${
                    isDone
                      ? 'bg-orange-600 text-white shadow-md shadow-orange-600/20'
                      : 'bg-slate-200 text-slate-500'
                  }`}
                >
                  {isDone && idx < currentStep ? (
                    <CheckCircle2 className="w-6 h-6" />
                  ) : (
                    <Icon className="w-6 h-6" />
                  )}
                </div>
                <p className={`text-xs font-bold ${isDone ? 'text-slate-900' : 'text-slate-500'}`}>{s.label}</p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Delivery Address & Items Breakdown */}
      <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-sm space-y-6">
        <div className="flex items-center gap-3 bg-slate-50 p-4 rounded-2xl border border-slate-200">
          <MapPin className="w-5 h-5 text-orange-600 shrink-0" />
          <div>
            <p className="text-xs font-bold text-slate-900">Delivering To ({deliveryLocation?.tag || 'Home'})</p>
            <p className="text-xs text-slate-600">{deliveryLocation?.address}</p>
            {deliveryLocation?.landmark && (
              <p className="text-[10px] text-orange-700 font-bold mt-0.5">Landmark: {deliveryLocation.landmark}</p>
            )}
          </div>
        </div>

        <div className="space-y-3">
          <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider border-b border-slate-100 pb-2">Ordered Items</h3>
          <div className="divide-y divide-slate-100">
            {order.items?.map((item, i) => (
              <div key={i} className="py-3 flex justify-between items-center text-sm">
                <div className="flex items-center gap-3">
                  <span className="w-6 h-6 rounded-lg bg-orange-100 text-orange-700 font-bold text-xs flex items-center justify-center">
                    {item.quantity}x
                  </span>
                  <span className="font-semibold text-slate-800">{item.itemName}</span>
                </div>
                <span className="font-bold text-slate-900">₹{(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
          </div>

          <div className="pt-3 border-t border-slate-100 flex justify-between items-center text-sm font-black">
            <span className="text-slate-700">Grand Total Paid</span>
            <span className="text-xl text-orange-600">₹{order.totalAmount.toFixed(2)}</span>
          </div>
        </div>
      </div>

    </div>
  );
};

export default OrderTrackingPage;
