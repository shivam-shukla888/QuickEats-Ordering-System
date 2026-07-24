import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { Utensils, ShoppingBag, Sparkles, User, ShieldAlert, Clock } from 'lucide-react';

const MobileBottomNav = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { cartItems, setIsCartOpen } = useCart();

  const cartCount = cartItems.reduce((acc, item) => acc + item.quantity, 0);

  const handleOpenAiChat = () => {
    window.dispatchEvent(new Event('open-ai-chat'));
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200/90 shadow-2xl py-1.5 px-2 flex justify-around items-center">
      
      {/* Home Link */}
      <Link
        to="/"
        className={`flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl transition-all ${
          isActive('/') ? 'text-orange-600 font-extrabold' : 'text-slate-500 hover:text-slate-800'
        }`}
      >
        <Utensils className={`w-5 h-5 ${isActive('/') ? 'text-orange-600' : ''}`} />
        <span className="text-[10px] font-bold">Explore</span>
      </Link>

      {/* Orders Link */}
      {isAuthenticated && (
        <Link
          to="/orders/history"
          className={`flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl transition-all ${
            isActive('/orders/history') ? 'text-orange-600 font-extrabold' : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <Clock className={`w-5 h-5 ${isActive('/orders/history') ? 'text-orange-600' : ''}`} />
          <span className="text-[10px] font-bold">Orders</span>
        </Link>
      )}

      {/* AI Assistant Button */}
      <button
        onClick={handleOpenAiChat}
        className="flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl text-slate-600 hover:text-orange-600 transition-all group"
      >
        <div className="relative">
          <Sparkles className="w-5 h-5 text-amber-500 group-hover:scale-110 transition-transform" />
          <span className="absolute -top-0.5 -right-0.5 w-2 h-2 rounded-full bg-orange-600 animate-ping" />
        </div>
        <span className="text-[10px] font-bold text-amber-900">Ask AI</span>
      </button>

      {/* Cart Button with Count Badge */}
      <button
        onClick={() => setIsCartOpen(true)}
        className="relative flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl text-slate-500 hover:text-orange-600 transition-all"
      >
        <div className="relative">
          <ShoppingBag className="w-5 h-5" />
          {cartCount > 0 && (
            <span className="absolute -top-1.5 -right-2 bg-orange-600 text-white text-[9px] font-black w-4 h-4 rounded-full flex items-center justify-center shadow-xs">
              {cartCount}
            </span>
          )}
        </div>
        <span className="text-[10px] font-bold">Cart</span>
      </button>

      {/* Admin Link if Admin/Owner */}
      {isAuthenticated && (user?.role === 'ADMIN' || user?.role === 'RESTAURANT_OWNER') && (
        <Link
          to="/admin"
          className={`flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl transition-all ${
            isActive('/admin') ? 'text-orange-600 font-extrabold' : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <ShieldAlert className="w-5 h-5 text-orange-500" />
          <span className="text-[10px] font-bold">Admin</span>
        </Link>
      )}

      {/* Profile / Auth Button */}
      {isAuthenticated ? (
        <button
          onClick={() => navigate('/orders/history')}
          className="flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl text-slate-500 hover:text-slate-800"
        >
          <div className="w-5 h-5 rounded-full bg-slate-900 text-emerald-400 flex items-center justify-center font-bold text-[9px]">
            {user?.name?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <span className="text-[10px] font-bold truncate max-w-[50px]">{user?.name?.split(' ')[0]}</span>
        </button>
      ) : (
        <Link
          to="/login"
          className={`flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl transition-all ${
            isActive('/login') ? 'text-orange-600 font-extrabold' : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <User className="w-5 h-5" />
          <span className="text-[10px] font-bold">Sign In</span>
        </Link>
      )}

    </nav>
  );
};

export default MobileBottomNav;
