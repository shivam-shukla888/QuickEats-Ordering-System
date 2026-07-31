import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { CreditCard, QrCode, Banknote, ShieldCheck, X, Check, ArrowRight, Sparkles, ExternalLink } from 'lucide-react';

const PaymentModal = ({ isOpen, onClose, onConfirmPayment, submitting }) => {
  const { paymentMethod, setPaymentMethod, calculateBill } = useCart();
  const [vpaId, setVpaId] = useState('');
  const [selectedTab, setSelectedTab] = useState('RAZORPAY'); // 'RAZORPAY' | 'UPI' | 'COD' | 'CARD'
  const [showRzpSuccessModal, setShowRzpSuccessModal] = useState(false);

  if (!isOpen) return null;

  const bill = calculateBill();
  const grandTotal = bill.grandTotal || 0.0;

  const handlePaySubmit = (e) => {
    e.preventDefault();
    onConfirmPayment(selectedTab);
  };

  const handleRzpConfirmSuccess = () => {
    setShowRzpSuccessModal(false);
    onConfirmPayment('RAZORPAY');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="bg-white w-full max-w-lg rounded-3xl border border-slate-200 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="p-5 bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-orange-600 flex items-center justify-center text-white font-black text-sm shadow-md">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-black text-base tracking-tight flex items-center gap-1.5">
                Checkout Payment Gateway
                <span className="bg-emerald-500/20 text-emerald-300 text-[10px] px-2 py-0.5 rounded-full font-bold border border-emerald-500/30">
                  Razorpay Sandbox
                </span>
              </h3>
              <p className="text-[10px] text-slate-300">256-bit SSL Encrypted Payment Portal</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-slate-200 bg-slate-50 p-1.5 gap-1 text-xs font-bold">
          <button
            type="button"
            onClick={() => { setSelectedTab('RAZORPAY'); setPaymentMethod('RAZORPAY_SANDBOX'); }}
            className={`flex-1 py-2.5 rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              selectedTab === 'RAZORPAY' ? 'bg-white text-blue-600 shadow-sm border border-slate-200' : 'text-slate-500 hover:text-slate-900'
            }`}
          >
            <Sparkles className="w-4 h-4 text-blue-600" />
            <span>Razorpay</span>
          </button>

          <button
            type="button"
            onClick={() => { setSelectedTab('UPI'); setPaymentMethod('UPI_GPLAY'); }}
            className={`flex-1 py-2.5 rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              selectedTab === 'UPI' ? 'bg-white text-orange-600 shadow-sm border border-slate-200' : 'text-slate-500 hover:text-slate-900'
            }`}
          >
            <QrCode className="w-4 h-4" />
            <span>UPI Apps</span>
          </button>

          <button
            type="button"
            onClick={() => { setSelectedTab('COD'); setPaymentMethod('COD'); }}
            className={`flex-1 py-2.5 rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              selectedTab === 'COD' ? 'bg-white text-orange-600 shadow-sm border border-slate-200' : 'text-slate-500 hover:text-slate-900'
            }`}
          >
            <Banknote className="w-4 h-4" />
            <span>Cash (COD)</span>
          </button>
        </div>

        {/* Tab Content Body */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6">

          {/* Tab 1: Razorpay Sandbox Mode */}
          {selectedTab === 'RAZORPAY' && (
            <div className="space-y-4">
              <div className="p-4 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-2xl space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-xl">💳</span>
                    <span className="font-black text-blue-900 text-sm">Razorpay Test Gateway Mode</span>
                  </div>
                  <span className="bg-blue-600 text-white text-[9px] font-black px-2 py-0.5 rounded-full uppercase">TEST MODE</span>
                </div>
                <p className="text-xs text-blue-800 leading-relaxed">
                  Real-time Razorpay checkout overlay simulator. Supports test cards (`4111 1111 1111 1111`), test UPI VPAs (`success@razorpay`), and instant webhooks!
                </p>
              </div>

              <div className="space-y-2">
                <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider">Test Payment Instruments Available:</p>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl">
                    <p className="font-bold text-slate-900">⚡ Test UPI VPA</p>
                    <p className="text-[10px] text-slate-500 font-mono">success@razorpay</p>
                  </div>
                  <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl">
                    <p className="font-bold text-slate-900">💳 Test Credit Card</p>
                    <p className="text-[10px] text-slate-500 font-mono">4111 •••• •••• 1111</p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Tab 2: UPI Apps */}
          {selectedTab === 'UPI' && (
            <div className="space-y-4">
              <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider">Fast UPI Payment Options</p>
              
              <div className="grid grid-cols-2 gap-3">
                {[
                  { id: 'UPI_GPLAY', name: 'Google Pay', icon: '🔵' },
                  { id: 'UPI_PHONEPE', name: 'PhonePe', icon: '💜' },
                  { id: 'UPI_PAYTM', name: 'Paytm UPI', icon: '🔷' },
                  { id: 'UPI_BHIM', name: 'BHIM / CRED', icon: '🇮🇳' }
                ].map((app) => (
                  <button
                    key={app.id}
                    type="button"
                    onClick={() => setPaymentMethod(app.id)}
                    className={`p-3 rounded-2xl border flex items-center justify-between text-left transition-all ${
                      paymentMethod === app.id
                        ? 'border-orange-500 bg-orange-50/70 shadow-sm'
                        : 'border-slate-200 hover:border-slate-300 bg-white'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <span className="text-lg">{app.icon}</span>
                      <span className="font-bold text-xs text-slate-800">{app.name}</span>
                    </div>
                    {paymentMethod === app.id && <Check className="w-4 h-4 text-orange-600" />}
                  </button>
                ))}
              </div>

              <div className="pt-2">
                <label className="block text-[10px] font-extrabold text-slate-500 uppercase tracking-wider mb-1">Enter VPA / UPI ID (Optional)</label>
                <input
                  type="text"
                  value={vpaId}
                  onChange={(e) => setVpaId(e.target.value)}
                  placeholder="e.g. mobileNumber@upi / username@okaxis"
                  className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-orange-500"
                />
              </div>
            </div>
          )}

          {/* Tab 3: Cash on Delivery */}
          {selectedTab === 'COD' && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-2xl space-y-2 text-emerald-900">
              <div className="flex items-center gap-2 font-bold text-sm">
                <Banknote className="w-5 h-5 text-emerald-700" />
                <span>Cash on Delivery Selected</span>
              </div>
              <p className="text-xs text-emerald-700 leading-relaxed">
                Pay <span className="font-bold">₹{grandTotal.toFixed(2)}</span> in cash to the delivery partner when your meal arrives at your doorstep. Please keep exact change ready!
              </p>
            </div>
          )}

          {/* Amount Confirmation Summary */}
          <div className="pt-4 border-t border-slate-100 flex justify-between items-center text-sm">
            <span className="font-bold text-slate-600">Total Payable Amount</span>
            <span className="text-xl font-black text-orange-600">₹{grandTotal.toFixed(2)}</span>
          </div>

        </div>

        {/* CTA Footer */}
        <div className="p-4 bg-slate-50 border-t border-slate-200">
          <button
            onClick={handlePaySubmit}
            disabled={submitting}
            className="w-full py-3.5 bg-orange-600 hover:bg-orange-700 text-white font-black text-sm rounded-2xl shadow-lg shadow-orange-600/30 flex items-center justify-center gap-2 transition-all disabled:opacity-50"
          >
            {submitting ? (
              <span>Processing Payment...</span>
            ) : (
              <>
                <span>Pay ₹{grandTotal.toFixed(2)} via {selectedTab === 'RAZORPAY' ? 'Razorpay' : selectedTab}</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </div>

      </div>

      {/* Simulated Razorpay Test Checkout Modal Overlay */}
      {showRzpSuccessModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="bg-slate-900 text-white w-full max-w-sm rounded-3xl p-6 shadow-2xl border border-blue-500/40 space-y-6 text-center">
            
            {/* Razorpay Brand Header */}
            <div className="space-y-2">
              <div className="w-12 h-12 rounded-2xl bg-blue-600 mx-auto flex items-center justify-center text-white font-black text-xl shadow-lg shadow-blue-600/30">
                rzp
              </div>
              <h3 className="font-black text-lg text-white">Razorpay Test Gateway</h3>
              <p className="text-xs text-blue-300">Order ID: order_rzp_test_{Math.floor(100000 + Math.random() * 900000)}</p>
            </div>

            <div className="p-4 bg-slate-800/90 rounded-2xl border border-slate-700 text-left space-y-2 text-xs">
              <div className="flex justify-between font-medium">
                <span className="text-slate-400">Merchant:</span>
                <span className="font-bold text-white">QuickEats Foods Ltd</span>
              </div>
              <div className="flex justify-between font-medium">
                <span className="text-slate-400">Amount:</span>
                <span className="font-black text-emerald-400">₹{grandTotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between font-medium">
                <span className="text-slate-400">Environment:</span>
                <span className="font-bold text-blue-400 uppercase">Sandbox Test Mode</span>
              </div>
            </div>

            <div className="space-y-3 pt-2">
              <button
                onClick={handleRzpConfirmSuccess}
                className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-black text-xs rounded-xl shadow-lg shadow-emerald-600/30 flex items-center justify-center gap-1.5 transition-all"
              >
                <Check className="w-4 h-4" />
                <span>Simulate Successful Payment</span>
              </button>

              <button
                onClick={() => setShowRzpSuccessModal(false)}
                className="w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs rounded-xl transition-all"
              >
                Cancel Transaction
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
};

export default PaymentModal;
