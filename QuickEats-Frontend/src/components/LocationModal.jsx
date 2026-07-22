import React, { useState, useEffect, useRef } from 'react';
import { useCart } from '../context/CartContext';
import { MapPin, Navigation, Home, Briefcase, X, Check, Search, Loader2 } from 'lucide-react';
import { fetchAccurateDeviceGpsLocation } from '../config/locationConfig';

const LocationModal = ({ isOpen, onClose }) => {
  const { deliveryLocation, setDeliveryLocation } = useCart();
  
  const [address, setAddress] = useState(deliveryLocation?.address || '');
  const [landmark, setLandmark] = useState(deliveryLocation?.landmark || '');
  const [tag, setTag] = useState(deliveryLocation?.tag || 'Home');
  const [coords, setCoords] = useState({ lat: deliveryLocation?.lat || 28.6245, lng: deliveryLocation?.lng || 77.2140 });

  const [detecting, setDetecting] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [loadingSuggestions, setLoadingSuggestions] = useState(false);
  const [searchError, setSearchError] = useState('');

  const debounceTimerRef = useRef(null);

  useEffect(() => {
    if (deliveryLocation) {
      setAddress(deliveryLocation.address || '');
      setLandmark(deliveryLocation.landmark || '');
      setTag(deliveryLocation.tag || 'Home');
    }
  }, [deliveryLocation]);

  if (!isOpen) return null;

  const handleAddressInputChange = (e) => {
    const query = e.target.value;
    setAddress(query);
    setSearchError('');

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    if (query.trim().length < 3) {
      setSuggestions([]);
      setLoadingSuggestions(false);
      return;
    }

    setLoadingSuggestions(true);

    // 450ms Debounce to comply with Nominatim 1 req/sec usage policy
    debounceTimerRef.current = setTimeout(async () => {
      try {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5`,
          {
            headers: {
              'User-Agent': 'QuickEats-Portfolio-Project/1.0 (Contact: localdev@quickeats.app)'
            }
          }
        );

        if (!response.ok) {
          throw new Error('Geocoding service unavailable');
        }

        const data = await response.json();
        setSuggestions(data);
        if (data.length === 0) {
          setSearchError('No matching address locations found.');
        }
      } catch (err) {
        console.warn('Nominatim geocoding failed:', err);
        setSearchError('Address search unavailable. You can enter manually.');
        setSuggestions([]);
      } finally {
        setLoadingSuggestions(false);
      }
    }, 450);
  };

  const handleSelectSuggestion = (item) => {
    setAddress(item.display_name);
    setCoords({ lat: parseFloat(item.lat), lng: parseFloat(item.lon) });
    setSuggestions([]);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!address.trim()) return;

    let targetLat = coords.lat;
    let targetLng = coords.lng;

    // Auto-geocode manually typed address if lat/lng are default Connaught Place
    if ((targetLat === 28.6245 && targetLng === 77.2140) && !address.toLowerCase().includes('connaught')) {
      try {
        const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address.trim())}&limit=1`, {
          headers: { 'User-Agent': 'QuickEats-Portfolio-Project/1.0 (Contact: localdev@quickeats.app)' }
        });
        if (res.ok) {
          const data = await res.json();
          if (data && data.length > 0) {
            targetLat = parseFloat(data[0].lat);
            targetLng = parseFloat(data[0].lon);
          }
        }
      } catch (err) {
        console.warn('Geocoding on save failed:', err);
      }
    }

    setDeliveryLocation({
      address: address.trim(),
      landmark: landmark.trim(),
      tag,
      lat: targetLat,
      lng: targetLng
    });
    onClose();
  };

  const handleDetectGps = async () => {
    setDetecting(true);
    setSearchError('');
    try {
      const accurateLocation = await fetchAccurateDeviceGpsLocation();
      if (accurateLocation) {
        setAddress(accurateLocation.address);
        setLandmark(`Exact GPS (${accurateLocation.source || 'Pinpoint Accuracy'})`);
        setCoords({ lat: accurateLocation.lat, lng: accurateLocation.lng });
      } else {
        setAddress('Connaught Place, Block M, Inner Circle, New Delhi');
        setCoords({ lat: 28.6245, lng: 77.2140 });
      }
    } catch (err) {
      console.warn('GPS detection error:', err);
    } finally {
      setDetecting(false);
    }
  };

  const savedAddresses = [
    { tag: 'Home', icon: Home, label: 'Connaught Place, New Delhi', landmark: 'Near Rajiv Chowk Metro Gate #2', lat: 28.6245, lng: 77.2140 },
    { tag: 'Work', icon: Briefcase, label: 'Cyber City, Phase 2, Gurugram', landmark: 'Opposite IndusInd Bank Tower', lat: 28.4950, lng: 77.0895 }
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="bg-white w-full max-w-md rounded-3xl border border-slate-200 shadow-2xl overflow-hidden">
        
        {/* Header */}
        <div className="p-5 bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-orange-600 flex items-center justify-center text-white">
              <MapPin className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-black text-base tracking-tight">Select Delivery Location</h3>
              <p className="text-[10px] text-orange-300">OpenStreetMap Nominatim Geocoding</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-white rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 text-slate-900">
          
          {/* GPS Auto Detect */}
          <button
            type="button"
            onClick={handleDetectGps}
            disabled={detecting}
            className="w-full py-3 px-4 bg-orange-50 hover:bg-orange-100 border border-orange-200 rounded-2xl text-orange-700 font-bold text-xs flex items-center justify-center gap-2 transition-all shadow-xs"
          >
            <Navigation className={`w-4 h-4 text-orange-600 ${detecting ? 'animate-spin' : ''}`} />
            <span>{detecting ? 'Detecting GPS Location...' : 'Use Current Location (GPS)'}</span>
          </button>

          {/* Quick Saved Addresses */}
          <div className="space-y-2">
            <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider">Saved Addresses</p>
            <div className="grid grid-cols-2 gap-3">
              {savedAddresses.map((addr, idx) => {
                const Icon = addr.icon;
                const isSelected = address === addr.label;

                return (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => {
                      setAddress(addr.label);
                      setLandmark(addr.landmark);
                      setTag(addr.tag);
                      setCoords({ lat: addr.lat, lng: addr.lng });
                    }}
                    className={`p-3 rounded-2xl border text-left flex flex-col justify-between transition-all ${
                      isSelected
                        ? 'border-orange-500 bg-orange-50/60 shadow-xs'
                        : 'border-slate-200 hover:border-slate-300 bg-white'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-bold text-xs text-slate-800 flex items-center gap-1.5">
                        <Icon className="w-3.5 h-3.5 text-orange-600" />
                        {addr.tag}
                      </span>
                      {isSelected && <Check className="w-3.5 h-3.5 text-orange-600" />}
                    </div>
                    <p className="text-[10px] text-slate-500 truncate">{addr.label}</p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Location Form with Nominatim Autocomplete */}
          <form onSubmit={handleSave} className="space-y-4">
            <div className="relative">
              <label className="block text-[10px] font-extrabold text-slate-500 uppercase tracking-wider mb-1 flex items-center justify-between">
                <span>Address / Autocomplete</span>
                {loadingSuggestions && (
                  <span className="text-orange-600 flex items-center gap-1 font-normal">
                    <Loader2 className="w-3 h-3 animate-spin" /> Searching...
                  </span>
                )}
              </label>

              <div className="relative">
                <input
                  type="text"
                  value={address}
                  onChange={handleAddressInputChange}
                  placeholder="Type address (e.g. Connaught Place, Delhi)..."
                  className="w-full p-3 pl-9 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
              </div>

              {/* Nominatim Suggestions Dropdown */}
              {suggestions.length > 0 && (
                <div className="absolute z-20 top-full left-0 right-0 mt-1 bg-white border border-slate-200 rounded-2xl shadow-xl overflow-hidden divide-y divide-slate-100 max-h-48 overflow-y-auto">
                  {suggestions.map((item) => (
                    <button
                      key={item.place_id}
                      type="button"
                      onClick={() => handleSelectSuggestion(item)}
                      className="w-full p-3 text-left hover:bg-orange-50 transition-colors flex items-start gap-2 text-xs"
                    >
                      <MapPin className="w-4 h-4 text-orange-600 shrink-0 mt-0.5" />
                      <span className="text-slate-800 font-medium truncate">{item.display_name}</span>
                    </button>
                  ))}
                </div>
              )}

              {searchError && (
                <p className="text-[10px] text-amber-600 font-bold mt-1">{searchError}</p>
              )}
            </div>

            <div>
              <label className="block text-[10px] font-extrabold text-slate-500 uppercase tracking-wider mb-1">Nearby Landmark (Indian Delivery Tip)</label>
              <input
                type="text"
                value={landmark}
                onChange={(e) => setLandmark(e.target.value)}
                placeholder="e.g. Near Rajiv Chowk Metro Gate #2 / Opp. SBI Bank"
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
            </div>

            <div>
              <label className="block text-[10px] font-extrabold text-slate-500 uppercase tracking-wider mb-1">Save Address As</label>
              <div className="flex gap-2">
                {['Home', 'Work', 'Other'].map((t) => (
                  <button
                    key={t}
                    type="button"
                    onClick={() => setTag(t)}
                    className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shrink-0 ${
                      tag === t
                        ? 'bg-slate-900 text-white shadow-xs'
                        : 'bg-slate-100 border border-slate-200 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    {t}
                  </button>
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-lg shadow-orange-600/20 transition-all mt-2"
            >
              Save & Deliver Here
            </button>
          </form>

        </div>

      </div>
    </div>
  );
};

export default LocationModal;
