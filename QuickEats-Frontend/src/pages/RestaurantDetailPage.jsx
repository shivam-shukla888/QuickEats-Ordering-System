import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getRestaurantById } from '../api/restaurantApi';
import { getMenuByRestaurant } from '../api/menuApi';
import { useCart } from '../context/CartContext';
import { getAiSurgeDetails } from '../utils/aiPricingEngine';
import LoadingSpinner from '../components/LoadingSpinner';
import { MapPin, Utensils, Star, Plus, Minus, ArrowLeft, Sparkles, TrendingUp } from 'lucide-react';

const RestaurantDetailPage = () => {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const { cartItems, addToCart, updateQuantity } = useCart();

  useEffect(() => {
    fetchRestaurantAndMenu();
  }, [id]);

  const fetchRestaurantAndMenu = async () => {
    setLoading(true);
    setError('');
    try {
      const [restaurantData, menuData] = await Promise.all([
        getRestaurantById(id),
        getMenuByRestaurant(id)
      ]);
      setRestaurant(restaurantData);
      setMenuItems(menuData || []);
    } catch (err) {
      setError('Failed to load restaurant menu. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getItemCartQuantity = (menuId) => {
    const item = cartItems.find(i => i.menuId === menuId);
    return item ? item.quantity : 0;
  };

  if (loading) return <LoadingSpinner message="Loading menu items with AI real-time dynamic pricing..." />;
  if (error) {
    return (
      <div className="max-w-md mx-auto my-12 p-8 bg-red-50 border border-red-200 rounded-3xl text-center text-red-700">
        <p className="font-bold">{error}</p>
        <Link to="/" className="inline-block mt-4 text-xs font-bold text-slate-800 underline">
          Back to Restaurants
        </Link>
      </div>
    );
  }

  // Get general surge info for banner
  const sampleSurge = getAiSurgeDetails(100);

  return (
    <div className="space-y-8 pb-16">
      
      {/* Back Button */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-orange-600 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to all restaurants</span>
      </Link>

      {/* Restaurant Info Header Card */}
      {restaurant && (
        <div className="bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 rounded-3xl p-6 sm:p-8 text-white shadow-xl flex flex-col md:flex-row justify-between items-start md:items-center gap-4 sm:gap-6 relative overflow-hidden">
          <div className="space-y-2 sm:space-y-3 relative z-10">
            <span className="bg-orange-500/20 border border-orange-500/30 text-orange-400 font-bold text-xs px-3 py-1 rounded-full inline-flex items-center gap-1.5">
              <Utensils className="w-3.5 h-3.5" />
              {restaurant.cuisineType}
            </span>
            <h1 className="text-2xl sm:text-4xl md:text-5xl font-black tracking-tight">{restaurant.name}</h1>
            <p className="text-slate-300 text-xs sm:text-sm flex items-center gap-2">
              <MapPin className="w-4 h-4 text-orange-400 shrink-0" />
              {restaurant.address}
            </p>
          </div>

          <div className="bg-white/10 backdrop-blur-md border border-white/20 p-3.5 sm:p-4 rounded-2xl flex items-center gap-3 shrink-0">
            <div className="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-amber-400/20 text-amber-300 flex items-center justify-center font-bold">
              <Star className="w-4 h-4 sm:w-5 sm:h-5 fill-amber-300" />
            </div>
            <div>
              <p className="text-base sm:text-lg font-black text-white">4.8 / 5.0</p>
              <p className="text-[10px] text-slate-300 font-medium">Customer Rating</p>
            </div>
          </div>
        </div>
      )}

      {/* AI Dynamic Market Surge Pricing Banner */}
      <div className="p-3.5 sm:p-4 bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 shadow-xs">
        <div className="flex items-center gap-2 text-xs font-bold text-amber-900">
          <Sparkles className="w-4 h-4 text-orange-600 animate-pulse shrink-0" />
          <span>Real-Time AI Market Pricing Engine: <span className="font-extrabold text-orange-700">{sampleSurge.label}</span></span>
        </div>
        <span className="bg-orange-600 text-white text-[10px] font-black px-2.5 py-0.5 rounded-full uppercase flex items-center gap-1 shrink-0">
          <TrendingUp className="w-3 h-3" /> Live Market Rates
        </span>
      </div>

      {/* Menu List Section */}
      <div className="space-y-6">
        <div className="flex items-center justify-between border-b border-slate-200 pb-4">
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">Menu Items</h2>
          <span className="text-xs font-semibold text-slate-500">{menuItems.length} items available</span>
        </div>

        {menuItems.length === 0 ? (
          <div className="text-center py-16 bg-white border border-slate-200 rounded-3xl text-slate-400">
            <Utensils className="w-12 h-12 mx-auto mb-3 text-slate-300" />
            <p className="text-sm font-bold text-slate-600">No menu items added yet</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
            {menuItems.map((menu) => {
              const qty = getItemCartQuantity(menu.id);
              const surge = getAiSurgeDetails(menu.price);

              return (
                <div
                  key={menu.id}
                  className="bg-white p-4 sm:p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all flex justify-between items-center gap-3 sm:gap-4"
                >
                  <div className="space-y-1.5 flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-1.5 sm:gap-2">
                      <h3 className="font-bold text-sm sm:text-base text-slate-900 leading-snug">{menu.itemName}</h3>
                      <span className={`text-[9px] font-black px-2 py-0.5 rounded-md border ${surge.badgeColor}`}>
                        {surge.surgePercent > 0 ? `+${surge.surgePercent}% Surge` : `${surge.surgePercent}% Deal`}
                      </span>
                    </div>

                    {menu.description && (
                      <p className="text-xs text-slate-500 line-clamp-2">{menu.description}</p>
                    )}

                    <div className="flex items-baseline gap-2 mt-2">
                      <span className="text-base font-black text-orange-600">₹{surge.dynamicPrice.toFixed(2)}</span>
                      {surge.basePrice !== surge.dynamicPrice && (
                        <span className="text-xs text-slate-400 line-through font-medium">₹{surge.basePrice.toFixed(2)}</span>
                      )}
                    </div>
                  </div>

                  {/* Add / Quantity Controller */}
                  <div className="shrink-0">
                    {qty === 0 ? (
                      <button
                        onClick={() => addToCart({
                          menuId: menu.id,
                          itemName: menu.itemName,
                          price: surge.dynamicPrice
                        }, { id: restaurant.id, name: restaurant.name })}
                        className="px-4 py-2.5 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-md shadow-orange-600/20 transition-all flex items-center gap-1.5 active:scale-95"
                      >
                        <Plus className="w-4 h-4" />
                        <span>Add</span>
                      </button>
                    ) : (
                      <div className="flex items-center gap-2 bg-orange-50 border border-orange-200 px-3 py-1.5 rounded-xl">
                        <button
                          onClick={() => updateQuantity(menu.id, -1)}
                          className="p-1 text-orange-700 hover:bg-orange-200/60 rounded-lg transition-colors"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <span className="text-xs font-black text-orange-900 w-5 text-center">{qty}</span>
                        <button
                          onClick={() => updateQuantity(menu.id, 1)}
                          className="p-1 text-orange-700 hover:bg-orange-200/60 rounded-lg transition-colors"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

    </div>
  );
};

export default RestaurantDetailPage;
