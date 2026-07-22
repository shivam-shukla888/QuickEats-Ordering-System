import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { useFcmNotification } from './hooks/useFcmNotification';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import CartDrawer from './components/CartDrawer';
import AiChatWidget from './components/AiChatWidget';
import ProtectedRoute from './components/ProtectedRoute';

import HomePage from './pages/HomePage';
import RestaurantDetailPage from './pages/RestaurantDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OrderTrackingPage from './pages/OrderTrackingPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import AdminPage from './pages/AdminPage';
import { Bell, X } from 'lucide-react';

const AppContent = () => {
  const { notificationBanner, clearBanner } = useFcmNotification();

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans relative">
      {/* Foreground Push Notification Banner Toast */}
      {notificationBanner && (
        <div className="fixed top-20 right-6 z-50 bg-slate-900 text-white p-4 rounded-2xl shadow-2xl border border-orange-500/40 flex items-start gap-3 max-w-sm animate-in slide-in-from-top-5">
          <div className="p-2 bg-orange-600 rounded-xl text-white shrink-0 mt-0.5">
            <Bell className="w-5 h-5 animate-bounce" />
          </div>
          <div className="flex-1 min-w-0 space-y-1">
            <h4 className="font-extrabold text-xs text-orange-400">{notificationBanner.title}</h4>
            <p className="text-xs text-slate-200 leading-snug">{notificationBanner.body}</p>
          </div>
          <button
            onClick={clearBanner}
            className="p-1 text-slate-400 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      <Navbar />
      <CartDrawer />
      <AiChatWidget />
      
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 pt-8">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/restaurant/:id" element={<RestaurantDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          {/* Protected Routes */}
          <Route
            path="/orders/history"
            element={
              <ProtectedRoute>
                <OrderHistoryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders/:id/track"
            element={
              <ProtectedRoute>
                <OrderTrackingPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute requiredRole="RESTAURANT_OWNER">
                <AdminPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>

      <Footer />
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Router>
          <AppContent />
        </Router>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
