import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import LocationModal from './LocationModal';
import { ShoppingBag, Utensils, LogOut, ShieldAlert, MapPin, ChevronDown } from 'lucide-react';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { cartItems, setIsCartOpen, deliveryLocation } = useCart();
  const [isLocationModalOpen, setIsLocationModalOpen] = useState(false);
  const navigate = useNavigate();

  const cartCount = cartItems.reduce((acc, item) => acc + item.quantity, 0);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <>
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-2">
          
          {/* Left: Brand Logo & Delivery Location Picker */}
          <div className="flex items-center gap-2 sm:gap-6 min-w-0">
            <Link to="/" className="flex items-center gap-2 font-extrabold text-xl sm:text-2xl tracking-tight text-slate-900 group shrink-0">
              <div className="w-9 h-9 sm:w-10 sm:h-10 rounded-2xl bg-slate-900 flex items-center justify-center text-emerald-400 shadow-md shadow-emerald-900/10 group-hover:scale-105 transition-transform border border-slate-800">
                <Utensils className="w-4 h-4 sm:w-5 sm:h-5 text-emerald-400" />
              </div>
              <span className="font-extrabold tracking-tight">Quick<span className="text-emerald-600">Eats</span></span>
            </Link>

            {/* Swiggy/Zomato Style Delivery Location Bar */}
            <button
              onClick={() => setIsLocationModalOpen(true)}
              className="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-100 hover:bg-orange-50 border border-slate-200 hover:border-orange-200 rounded-2xl text-left transition-all group min-w-0"
            >
              <MapPin className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-orange-600 shrink-0" />
              <div className="max-w-[100px] xs:max-w-[140px] sm:max-w-[200px] truncate">
                <p className="text-[9px] sm:text-[10px] font-bold text-slate-400 leading-tight uppercase tracking-wider flex items-center gap-1">
                  Deliver to ({deliveryLocation?.tag || 'Home'})
                </p>
                <p className="text-xs font-extrabold text-slate-800 truncate leading-tight group-hover:text-orange-600 transition-colors">
                  {deliveryLocation?.address || 'Select Address'}
                </p>
              </div>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 group-hover:text-orange-600 transition-colors shrink-0" />
            </button>
          </div>

          {/* Right Action Controls */}
          <div className="flex items-center gap-2 sm:gap-3 shrink-0">
            
            {/* Cart Button */}
            <button
              onClick={() => setIsCartOpen(true)}
              className="relative p-2 sm:px-4 sm:py-2 bg-orange-50 hover:bg-orange-100 text-orange-700 font-semibold rounded-full flex items-center gap-2 transition-colors border border-orange-200/60"
              aria-label="Open cart"
            >
              <ShoppingBag className="w-5 h-5" />
              <span className="hidden sm:inline text-sm">Cart</span>
              {cartCount > 0 && (
                <span className="bg-orange-600 text-white text-xs font-bold w-5 h-5 rounded-full flex items-center justify-center shadow-sm animate-pulse">
                  {cartCount}
                </span>
              )}
            </button>

            {/* User Auth Section */}
            {isAuthenticated ? (
              <div className="flex items-center gap-2 sm:gap-4 pl-1 sm:pl-2 border-l border-slate-200">
                
                {/* My Orders History Link (Desktop) */}
                <Link
                  to="/orders/history"
                  className="hidden md:flex px-3 py-1.5 bg-orange-100 hover:bg-orange-200 text-orange-800 text-xs font-bold rounded-lg transition-colors items-center gap-1.5"
                >
                  <ShoppingBag className="w-3.5 h-3.5 text-orange-600" />
                  <span>My Orders</span>
                </Link>

                {/* Admin Link if Admin/Owner */}
                {(user?.role === 'ADMIN' || user?.role === 'RESTAURANT_OWNER') && (
                  <Link
                    to="/admin"
                    className="hidden sm:flex px-3 py-1.5 bg-slate-900 text-slate-100 text-xs font-semibold rounded-lg hover:bg-slate-800 transition-colors items-center gap-1.5 shadow-sm"
                  >
                    <ShieldAlert className="w-3.5 h-3.5 text-orange-400" />
                    <span>Dashboard</span>
                  </Link>
                )}

                {/* User Profile info */}
                <div className="hidden sm:flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 font-bold text-xs">
                    {user?.name?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <div className="text-left">
                    <p className="text-xs font-bold text-slate-800 leading-tight">{user?.name}</p>
                    <p className="text-[10px] text-slate-500 leading-tight capitalize">{user?.role?.toLowerCase()}</p>
                  </div>
                </div>

                {/* Logout Button */}
                <button
                  onClick={handleLogout}
                  className="p-1.5 sm:p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  title="Logout"
                >
                  <LogOut className="w-4 h-4 sm:w-5 sm:h-5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-1.5 sm:gap-2 pl-1 sm:pl-2">
                <Link
                  to="/login"
                  className="px-2.5 sm:px-4 py-1.5 sm:py-2 text-xs sm:text-sm font-semibold text-slate-700 hover:text-orange-600 transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-3 sm:px-4 py-1.5 sm:py-2 text-xs sm:text-sm font-semibold text-white bg-orange-600 hover:bg-orange-700 rounded-full shadow-md shadow-orange-600/20 transition-all hover:shadow-lg"
                >
                  Register
                </Link>
              </div>
            )}

          </div>
        </div>
      </header>

      {/* Location Modal */}
      <LocationModal
        isOpen={isLocationModalOpen}
        onClose={() => setIsLocationModalOpen(false)}
      />
    </>
  );
};

export default Navbar;
