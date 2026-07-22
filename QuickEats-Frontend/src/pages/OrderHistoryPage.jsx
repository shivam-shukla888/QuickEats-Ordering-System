import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyOrders, cancelOrder } from '../api/orderApi';
import { useCart } from '../context/CartContext';
import LoadingSpinner from '../components/LoadingSpinner';
import { ShoppingBag, Navigation, Clock, CheckCircle2, Truck, RefreshCw, ArrowRight, XCircle } from 'lucide-react';

const OrderHistoryPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancellingId, setCancellingId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getMyOrders(0, 20);
      setOrders(data.content || []);
    } catch (err) {
      setError('Failed to load your order history.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm(`Are you sure you want to cancel Order #${orderId}?`)) return;

    setCancellingId(orderId);
    try {
      const updated = await cancelOrder(orderId);
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: updated.status } : o));
    } catch (err) {
      alert('Failed to cancel order.');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status?.toUpperCase()) {
      case 'DELIVERED':
        return (
          <span className="px-3 py-1 bg-emerald-100 text-emerald-800 font-extrabold text-xs rounded-full flex items-center gap-1 border border-emerald-300">
            <CheckCircle2 className="w-3.5 h-3.5" /> Delivered
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="px-3 py-1 bg-red-100 text-red-800 font-extrabold text-xs rounded-full flex items-center gap-1 border border-red-300">
            <XCircle className="w-3.5 h-3.5" /> Cancelled
          </span>
        );
      case 'OUT_FOR_DELIVERY':
        return (
          <span className="px-3 py-1 bg-blue-100 text-blue-800 font-extrabold text-xs rounded-full flex items-center gap-1 border border-blue-300 animate-pulse">
            <Truck className="w-3.5 h-3.5" /> Out for Delivery
          </span>
        );
      case 'PREPARING':
        return (
          <span className="px-3 py-1 bg-orange-100 text-orange-800 font-extrabold text-xs rounded-full flex items-center gap-1 border border-orange-300">
            <Clock className="w-3.5 h-3.5" /> Preparing
          </span>
        );
      default:
        return (
          <span className="px-3 py-1 bg-amber-100 text-amber-800 font-extrabold text-xs rounded-full flex items-center gap-1 border border-amber-300">
            <Clock className="w-3.5 h-3.5" /> Pending Confirmation
          </span>
        );
    }
  };

  if (loading) return <LoadingSpinner message="Loading your order history..." />;

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-16">
      
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-emerald-950 to-slate-900 text-white p-8 rounded-3xl shadow-xl flex items-center justify-between border border-slate-800">
        <div className="space-y-2">
          <span className="bg-emerald-500/10 text-emerald-400 text-xs font-extrabold px-3.5 py-1.5 rounded-full uppercase tracking-wider inline-flex items-center gap-1.5 border border-emerald-500/20">
            <ShoppingBag className="w-3.5 h-3.5" /> Order History
          </span>
          <h1 className="text-3xl font-extrabold tracking-tight font-outfit">Your QuickEats Orders</h1>
        </div>
        <button
          onClick={fetchOrders}
          className="p-3 bg-white/10 hover:bg-white/20 text-white rounded-2xl backdrop-blur-md transition-colors"
          title="Refresh Orders"
        >
          <RefreshCw className="w-5 h-5" />
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 font-bold rounded-2xl text-xs">
          {error}
        </div>
      )}

      {orders.length === 0 ? (
        <div className="bg-white p-12 rounded-3xl border border-slate-200 shadow-sm text-center space-y-4">
          <ShoppingBag className="w-16 h-16 text-slate-300 mx-auto" />
          <h3 className="text-lg font-extrabold text-slate-800 font-outfit">No Orders Placed Yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Hungry? Explore authentic culinary delights from top restaurants on QuickEats!
          </p>
          <button
            onClick={() => navigate('/')}
            className="px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl shadow-lg shadow-emerald-600/20 transition-all inline-flex items-center gap-2"
          >
            <span>Explore Restaurants</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => {
            const isEligibleForCancel = order.status === 'PENDING' || order.status === 'PREPARING';

            return (
              <div
                key={order.id}
                className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm hover:shadow-md transition-all space-y-4"
              >
                <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-100 pb-4">
                  <div>
                    <div className="flex items-center gap-3">
                      <h3 className="font-black text-slate-900 text-lg">Order #{order.id}</h3>
                      {getStatusBadge(order.status)}
                    </div>
                    <p className="text-xs text-slate-500 font-medium mt-1">
                      {order.restaurantName || 'North Indian Kitchen'} • {new Date(order.orderTime || Date.now()).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>

                  <div className="text-right">
                    <span className="text-2xl font-black text-orange-600">₹{order.totalAmount?.toFixed(2)}</span>
                  </div>
                </div>

                {/* Order Items Snapshot */}
                {order.orderItems && (
                  <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100 text-xs space-y-1 font-medium text-slate-700">
                    <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2">Items Ordered:</p>
                    <p className="leading-relaxed whitespace-pre-wrap">{order.orderItems}</p>
                  </div>
                )}

                {/* Actions Footer */}
                <div className="flex items-center justify-end gap-3 pt-2">
                  {isEligibleForCancel && (
                    <button
                      onClick={() => handleCancelOrder(order.id)}
                      disabled={cancellingId === order.id}
                      className="px-4 py-2.5 bg-red-50 hover:bg-red-100 text-red-700 font-bold text-xs rounded-xl border border-red-200 transition-all flex items-center gap-1.5 disabled:opacity-50"
                    >
                      <XCircle className="w-4 h-4 text-red-600" />
                      <span>{cancellingId === order.id ? 'Cancelling...' : 'Cancel Order'}</span>
                    </button>
                  )}

                  {order.status !== 'CANCELLED' && (
                    <button
                      onClick={() => navigate(`/orders/${order.id}/track`)}
                      className="px-4 py-2.5 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-md shadow-orange-600/20 flex items-center gap-1.5 transition-all"
                    >
                      <Navigation className="w-4 h-4" />
                      <span>Track Live 🛵</span>
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

    </div>
  );
};

export default OrderHistoryPage;
