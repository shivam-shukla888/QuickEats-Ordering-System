import React from 'react';
import { useCart } from '../context/CartContext';
import { ShoppingBag, ArrowRight } from 'lucide-react';

const MobileCartBanner = () => {
  const { cartItems, isCartOpen, setIsCartOpen, calculateBill, cartRestaurant } = useCart();

  const cartCount = cartItems.reduce((acc, item) => acc + item.quantity, 0);

  if (cartCount === 0 || isCartOpen) return null;

  const bill = calculateBill();

  return (
    <div className="md:hidden fixed bottom-14 left-3 right-3 z-30 animate-in slide-in-from-bottom-3">
      <button
        onClick={() => setIsCartOpen(true)}
        className="w-full bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white p-3.5 rounded-2xl shadow-xl border border-orange-500/30 flex items-center justify-between gap-3 active:scale-[0.98] transition-transform"
      >
        <div className="flex items-center gap-3 min-w-0">
          <div className="w-9 h-9 rounded-xl bg-orange-600 flex items-center justify-center text-white font-bold shrink-0 shadow-md">
            <ShoppingBag className="w-5 h-5" />
          </div>
          <div className="text-left min-w-0">
            <p className="text-xs font-black truncate">{cartCount} Item{cartCount > 1 ? 's' : ''} • ₹{bill.grandTotal?.toFixed(2)}</p>
            {cartRestaurant && <p className="text-[10px] text-slate-300 truncate">From {cartRestaurant.name}</p>}
          </div>
        </div>

        <div className="flex items-center gap-1.5 text-xs font-black text-orange-400 bg-white/10 px-3 py-1.5 rounded-xl backdrop-blur-xs shrink-0">
          <span>View Cart</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </div>
      </button>
    </div>
  );
};

export default MobileCartBanner;
