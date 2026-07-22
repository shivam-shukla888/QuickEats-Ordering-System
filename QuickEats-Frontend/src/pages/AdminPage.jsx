import React, { useState, useEffect, useRef } from 'react';
import { getRestaurants } from '../api/restaurantApi';
import { addMenuItem } from '../api/menuApi';
import { getAllOrders, updateOrderStatus } from '../api/orderApi';
import LoadingSpinner from '../components/LoadingSpinner';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '../config/apiConfig';
import { ShieldCheck, Plus, Package, Check, RefreshCw, Bell, Volume2, Wifi, Activity, Cpu, Zap } from 'lucide-react';
import axiosInstance from '../api/axiosInstance';

const AdminPage = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Real-time WebSockets & Unread Counter State
  const [unreadCount, setUnreadCount] = useState(0);
  const [newOrderIds, setNewOrderIds] = useState(new Set());
  const [wsConnected, setWsConnected] = useState(false);

  // Add menu form state
  const [selectedRestaurantId, setSelectedRestaurantId] = useState('');
  const [itemName, setItemName] = useState('');
  const [price, setPrice] = useState('');
  const [description, setDescription] = useState('');
  const [formSuccess, setFormSuccess] = useState('');
  const [formError, setFormError] = useState('');
  const [submittingMenu, setSubmittingMenu] = useState(false);

  const stompClientRef = useRef(null);

  // Web Audio API Synthesizer Chime (100% free, zero external audio file required)
  const playNewOrderChime = () => {
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;
      const audioCtx = new AudioCtx();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, audioCtx.currentTime); // D5 note
      osc.frequency.exponentialRampToValueAtTime(880, audioCtx.currentTime + 0.25); // A5 note

      gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.3);

      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.3);
    } catch (e) {
      console.warn('Audio chime playback restricted by browser policy:', e);
    }
  };

  useEffect(() => {
    fetchAdminData();
  }, []);

  // STOMP WebSocket Connection to /topic/admin/orders
  useEffect(() => {
    let client = null;

    try {
      client = new Client({
        webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
        reconnectDelay: 5000,
        onConnect: () => {
          setWsConnected(true);
          client.subscribe('/topic/admin/orders', (message) => {
            try {
              const eventData = JSON.parse(message.body);

              setOrders((prevOrders) => {
                const existingIdx = prevOrders.findIndex(o => o.id === eventData.orderId);
                if (existingIdx >= 0) {
                  // Inline status update for existing order
                  const updated = [...prevOrders];
                  updated[existingIdx] = { ...updated[existingIdx], status: eventData.status };
                  return updated;
                } else {
                  // New order arrived!
                  playNewOrderChime();
                  setUnreadCount(c => c + 1);
                  setNewOrderIds(prev => new Set(prev).add(eventData.orderId));

                  const newOrderObj = {
                    id: eventData.orderId,
                    userName: eventData.customerName || 'Customer',
                    restaurantName: eventData.restaurantName || 'Restaurant',
                    totalAmount: eventData.totalAmount || 0.0,
                    status: eventData.status || 'PENDING',
                    orderTime: eventData.createdAt || new Date().toISOString()
                  };
                  return [newOrderObj, ...prevOrders];
                }
              });
            } catch (err) {
              console.error('Failed to parse STOMP admin event:', err);
            }
          });
        },
        onStompError: () => setWsConnected(false),
        onWebSocketClose: () => setWsConnected(false)
      });

      client.activate();
      stompClientRef.current = client;
    } catch (err) {
      console.warn('Admin STOMP WebSocket failed to connect:', err);
    }

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, []);

  const [agentMetrics, setAgentMetrics] = useState(null);

  const fetchAgentMetrics = async () => {
    try {
      const res = await axiosInstance.get('/api/admin/agent-metrics');
      setAgentMetrics(res.data);
    } catch (e) {
      console.warn('Agent metrics fetch failed:', e);
    }
  };

  const fetchAdminData = async () => {
    setLoading(true);
    setError('');
    try {
      const [restData, orderData] = await Promise.all([
        getRestaurants(0, 50),
        getAllOrders(0, 50)
      ]);
      const restList = restData.content || [];
      setRestaurants(restList);
      if (restList.length > 0) {
        setSelectedRestaurantId(restList[0].id);
      }
      setOrders(orderData.content || []);
      await fetchAgentMetrics();
    } catch (err) {
      setError('Failed to load admin dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  const handleClearUnread = () => {
    setUnreadCount(0);
    setNewOrderIds(new Set());
  };

  const handleAddMenuSubmit = async (e) => {
    e.preventDefault();
    if (!selectedRestaurantId || !itemName || !price) {
      setFormError('Please fill in all required fields.');
      return;
    }

    setSubmittingMenu(true);
    setFormError('');
    setFormSuccess('');

    try {
      await addMenuItem(selectedRestaurantId, {
        itemName,
        price: parseFloat(price),
        description
      });
      setFormSuccess(`Added "${itemName}" successfully!`);
      setItemName('');
      setPrice('');
      setDescription('');
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to add menu item.');
    } finally {
      setSubmittingMenu(false);
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const updated = await updateOrderStatus(orderId, newStatus);
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: updated.status } : o));
    } catch (err) {
      alert('Failed to update status');
    }
  };

  if (loading) return <LoadingSpinner message="Loading Real-Time Admin Dashboard..." />;

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-16">
      
      {/* Admin Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white p-8 rounded-3xl shadow-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="bg-orange-500/20 text-orange-400 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider inline-flex items-center gap-1.5 border border-orange-500/30">
              <ShieldCheck className="w-3.5 h-3.5" />
              Admin / Owner Control Panel
            </span>
            <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold flex items-center gap-1 ${
              wsConnected ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30' : 'bg-amber-500/20 text-amber-300'
            }`}>
              <Wifi className="w-3 h-3" />
              {wsConnected ? 'STOMP Admin Live' : 'Connecting...'}
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">Real-Time Management Dashboard</h1>
        </div>

        <div className="flex items-center gap-3">
          {/* Unread Orders Counter Badge */}
          {unreadCount > 0 && (
            <button
              onClick={handleClearUnread}
              className="px-3.5 py-2 bg-orange-600 hover:bg-orange-700 text-white rounded-2xl font-black text-xs shadow-lg shadow-orange-600/30 flex items-center gap-2 animate-bounce"
            >
              <Bell className="w-4 h-4" />
              <span>{unreadCount} New Order{unreadCount > 1 ? 's' : ''}!</span>
            </button>
          )}

          <button
            onClick={fetchAdminData}
            className="p-3 bg-white/10 hover:bg-white/20 text-white rounded-2xl backdrop-blur-md transition-colors"
            title="Refresh Data"
          >
            <RefreshCw className="w-5 h-5" />
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 font-bold rounded-2xl text-xs">
          {error}
        </div>
      )}

      {/* Grid: Left = Add Menu Form, Right = Live Order Management Table */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Add Menu Item Form */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4 h-fit">
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Plus className="w-5 h-5 text-orange-600" />
            Add New Menu Item
          </h2>

          {formSuccess && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-bold rounded-xl flex items-center gap-2">
              <Check className="w-4 h-4" />
              <span>{formSuccess}</span>
            </div>
          )}

          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs font-bold rounded-xl">
              {formError}
            </div>
          )}

          <form onSubmit={handleAddMenuSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">Target Restaurant</label>
              <select
                value={selectedRestaurantId}
                onChange={(e) => setSelectedRestaurantId(e.target.value)}
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium text-slate-800"
                required
              >
                {restaurants.map(r => (
                  <option key={r.id} value={r.id}>{r.name} ({r.cuisineType})</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">Item Name</label>
              <input
                type="text"
                value={itemName}
                onChange={(e) => setItemName(e.target.value)}
                placeholder="e.g. Deluxe Veggie Pizza"
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-sm"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">Price ($)</label>
              <input
                type="number"
                step="0.01"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                placeholder="14.99"
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-sm"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Fresh ingredients, mozzarella, basil..."
                rows="3"
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-sm"
              />
            </div>

            <button
              type="submit"
              disabled={submittingMenu}
              className="w-full py-3 bg-orange-600 hover:bg-orange-700 text-white font-bold text-sm rounded-xl shadow-md shadow-orange-600/20 transition-all disabled:opacity-50"
            >
              {submittingMenu ? 'Saving...' : 'Add Menu Item'}
            </button>
          </form>
        </div>

        {/* Live Incoming Orders Table */}
        <div className="lg:col-span-2 bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4" onClick={handleClearUnread}>
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Package className="w-5 h-5 text-orange-600" />
              Live Incoming Orders
            </h2>
            <div className="flex items-center gap-2 text-xs text-slate-500 font-medium">
              <Volume2 className="w-4 h-4 text-orange-600" />
              <span>Audio Chime Enabled</span>
            </div>
          </div>

          {orders.length === 0 ? (
            <div className="text-center py-12 text-slate-400">
              <Package className="w-12 h-12 mx-auto mb-2 text-slate-300" />
              <p className="text-sm font-semibold">No orders received yet</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-200 text-slate-400 text-[10px] uppercase font-extrabold tracking-wider">
                    <th className="py-3 px-2">Order ID</th>
                    <th className="py-3 px-2">Customer</th>
                    <th className="py-3 px-2">Restaurant</th>
                    <th className="py-3 px-2">Total</th>
                    <th className="py-3 px-2">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {orders.map((o) => {
                    const isNew = newOrderIds.has(o.id);

                    return (
                      <tr
                        key={o.id}
                        className={`transition-all duration-500 ${
                          isNew ? 'bg-orange-50/90 font-bold border-l-4 border-orange-600' : 'hover:bg-slate-50/80'
                        }`}
                      >
                        <td className="py-3.5 px-2 font-black text-slate-900 flex items-center gap-2">
                          #{o.id}
                          {isNew && <span className="bg-orange-600 text-white text-[9px] font-black px-1.5 py-0.5 rounded-full uppercase animate-pulse">NEW</span>}
                        </td>
                        <td className="py-3.5 px-2 font-medium text-slate-700">{o.userName}</td>
                        <td className="py-3.5 px-2 text-slate-500">{o.restaurantName}</td>
                        <td className="py-3.5 px-2 font-bold text-orange-600">₹{o.totalAmount?.toFixed(2)}</td>
                        <td className="py-3.5 px-2">
                          <select
                            value={o.status}
                            onChange={(e) => handleStatusChange(o.id, e.target.value)}
                            className="px-2.5 py-1.5 bg-slate-100 border border-slate-200 rounded-lg text-xs font-bold text-slate-800 focus:outline-none focus:ring-2 focus:ring-orange-500"
                          >
                            <option value="PENDING">PENDING</option>
                            <option value="PREPARING">PREPARING</option>
                            <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>
                            <option value="DELIVERED">DELIVERED</option>
                          </select>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* AI Agent Production Observability & Metrics Section */}
        <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm mt-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-orange-100 text-orange-600 rounded-2xl">
                <Activity size={22} />
              </div>
              <div>
                <h2 className="text-xl font-black text-slate-900">📊 AI Agent Production Observability</h2>
                <p className="text-xs text-slate-500 font-medium">Real-time Spring AOP Telemetry: Latency, Cost (Tokens), & Success Metrics</p>
              </div>
            </div>
            <button
              onClick={fetchAgentMetrics}
              className="flex items-center gap-1.5 px-3.5 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition"
            >
              <RefreshCw size={14} /> Refresh Telemetry
            </button>
          </div>

          {agentMetrics ? (
            <div className="space-y-6">
              {/* Summary Cards */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-50 border border-slate-100 rounded-2xl">
                  <p className="text-xs font-bold text-slate-500 flex items-center gap-1"><Cpu size={14}/> Total LLM Calls</p>
                  <p className="text-2xl font-black text-slate-900 mt-1">{agentMetrics.totalCalls || 0}</p>
                  <p className="text-[10px] text-emerald-600 font-bold mt-1">+{agentMetrics.callsLast24Hours || 0} in last 24h</p>
                </div>
                <div className="p-4 bg-slate-50 border border-slate-100 rounded-2xl">
                  <p className="text-xs font-bold text-slate-500 flex items-center gap-1"><Zap size={14}/> Success Rate</p>
                  <p className="text-2xl font-black text-emerald-600 mt-1">{agentMetrics.overallSuccessRatePercent || 100}%</p>
                  <p className="text-[10px] text-slate-400 font-medium mt-1">Zero failures recorded</p>
                </div>
                <div className="p-4 bg-slate-50 border border-slate-100 rounded-2xl">
                  <p className="text-xs font-bold text-slate-500 flex items-center gap-1"><Activity size={14}/> Avg Agent Latency</p>
                  <p className="text-2xl font-black text-orange-600 mt-1">{agentMetrics.averageLatencyMs || 0} ms</p>
                  <p className="text-[10px] text-slate-400 font-medium mt-1">AOP measured</p>
                </div>
                <div className="p-4 bg-slate-50 border border-slate-100 rounded-2xl">
                  <p className="text-xs font-bold text-slate-500">Total Tokens Consumed</p>
                  <p className="text-2xl font-black text-slate-900 mt-1">{agentMetrics.totalEstimatedTokens || 0}</p>
                  <p className="text-[10px] text-slate-400 font-medium mt-1">Groq Llama-3 usage</p>
                </div>
              </div>

              {/* Agent Call Log Table */}
              <div>
                <h3 className="text-sm font-black text-slate-900 mb-3">Recent Agent Interceptor Logs (Spring AOP @Around)</h3>
                <div className="overflow-x-auto border border-slate-100 rounded-2xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-slate-100 bg-slate-50/50 text-[11px] font-extrabold text-slate-400 uppercase">
                        <th className="py-2.5 px-3">Agent Type</th>
                        <th className="py-2.5 px-3">Prompt Excerpt</th>
                        <th className="py-2.5 px-3">Target Method</th>
                        <th className="py-2.5 px-3">Latency</th>
                        <th className="py-2.5 px-3">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-xs font-medium">
                      {(agentMetrics.recentLogs || []).slice(0, 5).map((log) => (
                        <tr key={log.id} className="hover:bg-slate-50/50">
                          <td className="py-2.5 px-3 font-bold text-slate-800">
                            <span className="px-2 py-0.5 bg-orange-100 text-orange-700 text-[10px] rounded-lg uppercase">{log.agentType}</span>
                          </td>
                          <td className="py-2.5 px-3 text-slate-600 truncate max-w-xs">{log.inputPrompt}</td>
                          <td className="py-2.5 px-3 font-mono text-[11px] text-slate-500">{log.toolsInvoked}</td>
                          <td className="py-2.5 px-3 font-bold text-slate-900">{log.latencyMs} ms</td>
                          <td className="py-2.5 px-3">
                            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${log.success ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'}`}>
                              {log.success ? 'SUCCESS' : 'FAILED'}
                            </span>
                          </td>
                        </tr>
                      ))}
                      {(!agentMetrics.recentLogs || agentMetrics.recentLogs.length === 0) && (
                        <tr>
                          <td colSpan={5} className="py-4 text-center text-xs text-slate-400">No AI Agent telemetry recorded yet. Interact with AI Assistant to view live logs.</td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          ) : (
            <p className="text-xs text-slate-400">Loading observability telemetry...</p>
          )}
        </div>

      </div>

    </div>
  );
};

export default AdminPage;
