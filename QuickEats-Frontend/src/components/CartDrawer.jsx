import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { placeOrder } from '../api/orderApi';
import PaymentModal from './PaymentModal';
import { X, ShoppingBag, Plus, Minus, Trash2, MapPin, ArrowRight, Tag, HeartHandshake, Check, Sparkles } from 'lucide-react';

const CartDrawer = () => {
  const {
    cartItems,
    cartRestaurant,
    isCartOpen,
    setIsCartOpen,
    updateQuantity,
    addToCart,
    clearCart,
    calculateBill,
    appliedCoupon,
    applyCouponCode,
    removeCoupon,
    deliveryTip,
    setDeliveryTip,
    deliveryInstructions,
    toggleDeliveryInstruction,
    paymentMethod,
    deliveryLocation
  } = useCart();

  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [couponInput, setCouponInput] = useState('');
  const [couponMessage, setCouponMessage] = useState('');
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (!isCartOpen) return null;

  const bill = calculateBill();

  const handleApplyCoupon = (e) => {
    e.preventDefault();
    if (!couponInput.trim()) return;
    const res = applyCouponCode(couponInput);
    setCouponMessage(res.message);
  };

  const handleOpenPaymentModal = () => {
    if (!isAuthenticated) {
      setIsCartOpen(false);
      navigate('/login');
      return;
    }
    setError('');
    setIsPaymentModalOpen(true);
  };

  const handleConfirmOrder = async () => {
    setSubmitting(true);
    setError('');

    try {
      const fullAddress = `${deliveryLocation.address} (${deliveryLocation.landmark || 'No landmark'})`;
      const orderPayload = {
        userId: user.id,
        restaurantId: cartRestaurant ? cartRestaurant.id : 1,
        items: cartItems.map(item => ({
          menuId: item.menuId,
          quantity: item.quantity,
          price: item.price,
          itemName: item.itemName
        }))
      };

      const newOrder = await placeOrder(orderPayload);
      clearCart();
      setIsPaymentModalOpen(false);
      setIsCartOpen(false);
      navigate(`/orders/${newOrder.id}/track`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const availableInstructions = [
    { id: 'DONT_RING', label: 'Don\'t ring bell 🔕' },
    { id: 'LEAVE_DOOR', label: 'Leave with guard 🛡️' },
    { id: 'CALL_ARRIVE', label: 'Call upon arrival 📞' },
    { id: 'NO_CUTLERY', label: 'Avoid plastic cutlery 🍃' }
  ];

  const aiRecommendations = [
    { menuId: 991, itemName: 'Hot Gulab Jamun (2 Pcs)', price: 60.0, icon: '🍮' },
    { menuId: 992, itemName: 'Chilled Mango Lassi', price: 70.0, icon: '🥛' },
    { menuId: 993, itemName: 'Garlic Butter Naan', price: 45.0, icon: '🫓' }
  ];

  return (
    <>
      <div className="fixed inset-0 z-50 overflow-hidden">
        {/* Backdrop */}
        <div
          className="absolute inset-0 bg-slate-900/60 backdrop-blur-xs transition-opacity"
          onClick={() => setIsCartOpen(false)}
        />

        <div className="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
          <div className="pointer-events-auto w-screen max-w-md bg-white shadow-2xl flex flex-col">
            
            {/* Header */}
            <div className="p-5 bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white flex items-center justify-between">
              <div className="flex items-center gap-3">
                <ShoppingBag className="w-6 h-6 text-orange-400" />
                <div>
                  <h2 className="text-base font-black">Your Cart</h2>
                  {cartRestaurant && <p className="text-xs text-slate-300 font-medium">from {cartRestaurant.name}</p>}
                </div>
              </div>
              <button
                onClick={() => setIsCartOpen(false)}
                className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            {/* Cart Body */}
            <div className="flex-1 overflow-y-auto p-5 space-y-6">
              {error && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs font-semibold rounded-xl">
                  {error}
                </div>
              )}

              {cartItems.length === 0 ? (
                <div className="text-center py-16 text-slate-400">
                  <ShoppingBag className="w-16 h-16 mx-auto mb-4 stroke-[1.5] text-slate-300" />
                  <p className="text-base font-bold text-slate-600">Your cart is empty</p>
                  <p className="text-xs text-slate-400 mt-1">Browse restaurants & add delicious North Indian meals!</p>
                </div>
              ) : (
                <>
                  {/* Cart Items List */}
                  <div className="space-y-3">
                    {cartItems.map((item) => (
                      <div
                        key={item.menuId}
                        className="p-3.5 rounded-2xl border border-slate-200/80 bg-slate-50 flex items-center justify-between gap-3 shadow-xs"
                      >
                        <div className="flex-1 min-w-0">
                          <h4 className="font-bold text-xs text-slate-900 truncate">{item.itemName}</h4>
                          <p className="text-xs text-orange-600 font-black mt-0.5">₹{(item.price * item.quantity).toFixed(2)}</p>
                        </div>

                        {/* Quantity Stepper */}
                        <div className="flex items-center gap-2 bg-white px-2 py-1 rounded-xl border border-slate-200 shadow-xs">
                          <button
                            onClick={() => updateQuantity(item.menuId, -1)}
                            className="p-1 text-slate-600 hover:text-orange-600 transition-colors"
                          >
                            <Minus className="w-3.5 h-3.5" />
                          </button>
                          <span className="text-xs font-black text-slate-800 w-4 text-center">{item.quantity}</span>
                          <button
                            onClick={() => updateQuantity(item.menuId, 1)}
                            className="p-1 text-slate-600 hover:text-orange-600 transition-colors"
                          >
                            <Plus className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* AI Smart Order Recommendations */}
                  <div className="p-4 bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-2xl space-y-2.5">
                    <div className="flex items-center gap-1.5 text-xs font-bold text-amber-900">
                      <Sparkles className="w-4 h-4 text-orange-600 animate-pulse" />
                      <span>Frequently Ordered Together</span>
                    </div>
                    <div className="grid grid-cols-3 gap-2">
                      {aiRecommendations.map((rec) => (
                        <button
                          key={rec.menuId}
                          onClick={() => addToCart(rec, cartRestaurant || { id: 1, name: 'North Indian Kitchen' })}
                          className="p-2 bg-white border border-amber-200 hover:border-orange-500 rounded-xl text-left transition-all shadow-xs flex flex-col justify-between"
                        >
                          <div>
                            <span className="text-base">{rec.icon}</span>
                            <p className="text-[10px] font-bold text-slate-800 truncate mt-1">{rec.itemName}</p>
                          </div>
                          <div className="flex items-center justify-between mt-2 pt-1 border-t border-slate-100">
                            <span className="text-[10px] font-black text-orange-600">₹{rec.price}</span>
                            <Plus className="w-3 h-3 text-orange-600" />
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Delivery Instructions Chips */}
                  <div className="space-y-2 pt-2 border-t border-slate-100">
                    <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider">Delivery Instructions</p>
                    <div className="flex flex-wrap gap-2">
                      {availableInstructions.map((ins) => {
                        const isSelected = deliveryInstructions.includes(ins.id);
                        return (
                          <button
                            key={ins.id}
                            onClick={() => toggleDeliveryInstruction(ins.id)}
                            className={`px-3 py-1.5 rounded-xl text-[10px] font-bold transition-all ${
                              isSelected
                                ? 'bg-orange-600 text-white shadow-xs'
                                : 'bg-slate-100 border border-slate-200 text-slate-600 hover:bg-slate-200'
                            }`}
                          >
                            {ins.label}
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  {/* Delivery Partner Tip */}
                  <div className="p-3.5 bg-gradient-to-r from-orange-50 to-amber-50 border border-orange-200 rounded-2xl space-y-2">
                    <div className="flex items-center justify-between text-xs font-bold text-slate-800">
                      <span className="flex items-center gap-1.5">
                        <HeartHandshake className="w-4 h-4 text-orange-600" />
                        Tip Delivery Partner
                      </span>
                      {deliveryTip > 0 && <span className="text-orange-600">+₹{deliveryTip.toFixed(2)}</span>}
                    </div>
                    <p className="text-[10px] text-slate-500">100% of your tip goes directly to your delivery rider.</p>
                    <div className="flex gap-2">
                      {[20, 30, 50].map((amount) => (
                        <button
                          key={amount}
                          onClick={() => setDeliveryTip(prev => prev === amount ? 0 : amount)}
                          className={`flex-1 py-1.5 rounded-xl text-xs font-bold transition-all ${
                            deliveryTip === amount
                              ? 'bg-orange-600 text-white shadow-xs'
                              : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
                          }`}
                        >
                          +₹{amount}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Coupons Section */}
                  <div className="space-y-2 pt-2 border-t border-slate-100">
                    <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider flex items-center gap-1">
                      <Tag className="w-3 h-3 text-orange-600" />
                      Apply Coupon / Promo Code
                    </p>

                    {appliedCoupon ? (
                      <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl flex items-center justify-between">
                        <div>
                          <p className="text-xs font-bold text-emerald-800">{appliedCoupon.code} Applied!</p>
                          <p className="text-[10px] text-emerald-600">{appliedCoupon.description}</p>
                        </div>
                        <button
                          onClick={removeCoupon}
                          className="text-xs font-bold text-red-600 hover:underline"
                        >
                          Remove
                        </button>
                      </div>
                    ) : (
                      <form onSubmit={handleApplyCoupon} className="flex gap-2">
                        <input
                          type="text"
                          value={couponInput}
                          onChange={(e) => setCouponInput(e.target.value)}
                          placeholder="Try WELCOME50 or QUICKEATS"
                          className="flex-1 px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs uppercase font-bold text-slate-800 focus:outline-none focus:ring-2 focus:ring-orange-500"
                        />
                        <button
                          type="submit"
                          className="px-4 py-2 bg-slate-900 text-white font-bold text-xs rounded-xl hover:bg-slate-800 transition-all"
                        >
                          Apply
                        </button>
                      </form>
                    )}

                    {couponMessage && !appliedCoupon && (
                      <p className="text-[10px] font-bold text-orange-600">{couponMessage}</p>
                    )}
                  </div>

                  {/* Itemized Bill Breakdown */}
                  <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 space-y-2 text-xs">
                    <p className="font-extrabold text-slate-900 border-b border-slate-200 pb-1.5 uppercase text-[10px] tracking-wider">Bill Summary</p>
                    <div className="flex justify-between text-slate-600">
                      <span>Item Total</span>
                      <span className="font-bold text-slate-800">₹{bill.itemTotal?.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-slate-600">
                      <span>Delivery Fee</span>
                      <span className="font-bold text-slate-800">
                        {bill.deliveryFee === 0 ? <span className="text-emerald-600">FREE</span> : `₹${bill.deliveryFee?.toFixed(2)}`}
                      </span>
                    </div>
                    <div className="flex justify-between text-slate-600">
                      <span>Restaurant Packaging</span>
                      <span className="font-bold text-slate-800">₹{bill.packagingCharge?.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-slate-600">
                      <span>GST & Govt Taxes (5%)</span>
                      <span className="font-bold text-slate-800">₹{bill.gstTax?.toFixed(2)}</span>
                    </div>

                    {bill.discount > 0 && (
                      <div className="flex justify-between text-emerald-700 font-bold">
                        <span>Coupon Discount</span>
                        <span>-₹{bill.discount?.toFixed(2)}</span>
                      </div>
                    )}

                    {bill.tip > 0 && (
                      <div className="flex justify-between text-orange-700 font-bold">
                        <span>Delivery Rider Tip</span>
                        <span>+₹{bill.tip?.toFixed(2)}</span>
                      </div>
                    )}

                    <div className="pt-2 border-t border-slate-200 flex justify-between items-center text-sm font-black text-slate-900">
                      <span>To Pay</span>
                      <span className="text-lg text-orange-600">₹{bill.grandTotal?.toFixed(2)}</span>
                    </div>
                  </div>
                </>
              )}
            </div>

            {/* Footer / Checkout CTA */}
            {cartItems.length > 0 && (
              <div className="p-5 border-t border-slate-200 bg-white space-y-3">
                <div className="flex items-center gap-2 text-xs text-slate-600 bg-slate-50 p-2.5 rounded-xl border border-slate-200">
                  <MapPin className="w-4 h-4 text-orange-600 shrink-0" />
                  <div className="truncate">
                    <span className="font-bold text-slate-800">Delivering to: </span>
                    <span className="text-slate-500">{deliveryLocation?.address}</span>
                  </div>
                </div>

                <button
                  onClick={handleOpenPaymentModal}
                  className="w-full py-3.5 bg-gradient-to-r from-orange-600 to-amber-600 hover:from-orange-700 hover:to-amber-700 text-white font-black text-sm rounded-2xl shadow-lg shadow-orange-600/30 flex items-center justify-center gap-2 transition-all"
                >
                  <span>Select Payment Method • ₹{bill.grandTotal?.toFixed(2)}</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            )}

          </div>
        </div>
      </div>

      {/* Payment Modal */}
      <PaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        onConfirmPayment={handleConfirmOrder}
        submitting={submitting}
      />
    </>
  );
};

export default CartDrawer;
