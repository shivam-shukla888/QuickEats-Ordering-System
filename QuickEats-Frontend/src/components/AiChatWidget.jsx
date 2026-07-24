import React, { useState, useRef, useEffect } from 'react';
import { sendAiChatMessage } from '../api/aiAssistantApi';
import { useCart } from '../context/CartContext';
import { Sparkles, MessageSquare, X, Send, Bot, User, Plus, Flame, Leaf, Loader2 } from 'lucide-react';

const AiChatWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      sender: 'ai',
      text: 'Hello! I am QuickEats AI Concierge. I can recommend dishes, place orders, or check your delivery status. Try asking: "Suggest spicy biryani under ₹300" or "Recommend a light healthy dinner"!',
      recommendations: []
    }
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const { addToCart } = useCart();
  const chatEndRef = useRef(null);

  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    const handleOpenAiChat = () => setIsOpen(true);
    window.addEventListener('open-ai-chat', handleOpenAiChat);
    return () => window.removeEventListener('open-ai-chat', handleOpenAiChat);
  }, []);

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSend = async (textToSend) => {
    const text = textToSend || inputMessage;
    if (!text || !text.trim()) return;

    const userMsg = { sender: 'user', text: text.trim() };
    setMessages(prev => [...prev, userMsg]);
    if (!textToSend) setInputMessage('');
    setLoading(true);

    try {
      const data = await sendAiChatMessage(text.trim());
      const aiMsg = {
        sender: 'ai',
        text: data.reply || 'Here are some recommendations!',
        recommendations: data.recommendations || []
      };
      setMessages(prev => [...prev, aiMsg]);
    } catch (err) {
      setMessages(prev => [
        ...prev,
        { sender: 'ai', text: 'Sorry, I ran into a connection glitch. Please try asking again!', recommendations: [] }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const quickChips = [
    'Dinner for 2 under $25',
    'Butter Chicken & Naan',
    'Best veg starters',
    'North Indian specials'
  ];

  return (
    <>
      {/* Floating Launcher Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-20 sm:bottom-6 right-4 sm:right-6 z-50 p-3.5 sm:p-4 bg-gradient-to-r from-orange-600 to-amber-500 text-white rounded-full shadow-2xl hover:scale-110 transition-all group flex items-center gap-2 border border-white/20"
        aria-label="Ask QuickEats AI"
      >
        <Sparkles className="w-5 h-5 sm:w-6 sm:h-6 animate-pulse" />
        <span className="max-w-0 sm:group-hover:max-w-xs overflow-hidden transition-all duration-300 font-bold text-xs whitespace-nowrap pr-1">
          Ask QuickEats AI
        </span>
      </button>

      {/* Slide-Up Chat Window */}
      {isOpen && (
        <div className="fixed inset-0 sm:inset-auto sm:bottom-24 sm:right-6 z-50 w-full sm:w-96 sm:max-w-[calc(100vw-3rem)] h-full sm:h-[520px] bg-white sm:rounded-3xl border border-slate-200 shadow-2xl flex flex-col overflow-hidden animate-in slide-in-from-bottom-5">

          
          {/* Header */}
          <div className="p-4 bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 text-white flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-orange-600 flex items-center justify-center text-white shadow-md">
                <Bot className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-black text-sm tracking-tight flex items-center gap-1.5">
                  QuickEats AI Assistant
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                </h3>
                <p className="text-[10px] text-slate-300">Powered by Groq Llama 3.1</p>
              </div>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              className="p-1.5 text-slate-400 hover:text-white hover:bg-white/10 rounded-xl transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Chat Messages Body */}
          <div className="flex-1 p-4 overflow-y-auto space-y-4 bg-slate-50 text-xs">
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex gap-2.5 ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                {msg.sender === 'ai' && (
                  <div className="w-7 h-7 rounded-lg bg-orange-600 text-white flex items-center justify-center shrink-0 mt-0.5 shadow-sm">
                    <Bot className="w-4 h-4" />
                  </div>
                )}

                <div className={`space-y-2 max-w-[80%] ${msg.sender === 'user' ? 'text-right' : 'text-left'}`}>
                  <div
                    className={`p-3.5 rounded-2xl leading-relaxed ${
                      msg.sender === 'user'
                        ? 'bg-orange-600 text-white font-medium rounded-br-xs shadow-sm'
                        : 'bg-white text-slate-800 border border-slate-200/80 rounded-bl-xs shadow-sm'
                    }`}
                  >
                    {msg.text}
                  </div>

                  {/* Interactive Recommended Items */}
                  {msg.recommendations && msg.recommendations.length > 0 && (
                    <div className="space-y-2 pt-1">
                      <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider">Suggested Dishes:</p>
                      {msg.recommendations.map(dish => (
                        <div key={dish.id} className="bg-white p-3 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between gap-2 text-left">
                          <div className="min-w-0">
                            <p className="font-bold text-slate-900 truncate">{dish.itemName}</p>
                            <p className="text-[10px] text-slate-400">{dish.restaurantName} • ${dish.price?.toFixed(2)}</p>
                          </div>
                          <button
                            onClick={() => addToCart({
                              menuId: dish.id,
                              itemName: dish.itemName,
                              price: dish.price
                            }, { id: dish.restaurantId, name: dish.restaurantName })}
                            className="px-2.5 py-1 bg-orange-600 text-white font-bold text-[10px] rounded-lg hover:bg-orange-700 flex items-center gap-1 shrink-0 shadow-sm"
                          >
                            <Plus className="w-3 h-3" /> Add
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {msg.sender === 'user' && (
                  <div className="w-7 h-7 rounded-lg bg-slate-800 text-white flex items-center justify-center shrink-0 mt-0.5 shadow-sm">
                    <User className="w-4 h-4" />
                  </div>
                )}
              </div>
            ))}

            {loading && (
              <div className="flex gap-2 items-center text-slate-400 text-xs font-semibold">
                <Loader2 className="w-4 h-4 animate-spin text-orange-600" />
                <span>QuickEats AI is crafting recommendations...</span>
              </div>
            )}

            <div ref={chatEndRef} />
          </div>

          {/* Quick Suggestion Chips */}
          <div className="px-3 py-2 bg-white border-t border-slate-100 flex items-center gap-1.5 overflow-x-auto scrollbar-none">
            {quickChips.map((chip, i) => (
              <button
                key={i}
                onClick={() => handleSend(chip)}
                className="px-2.5 py-1 bg-slate-100 hover:bg-orange-50 text-slate-600 hover:text-orange-600 rounded-lg text-[10px] font-bold transition-all shrink-0 border border-slate-200/60"
              >
                {chip}
              </button>
            ))}
          </div>

          {/* Input Form */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="p-3 bg-white border-t border-slate-200 flex items-center gap-2"
          >
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Ask for meal advice or search..."
              className="flex-1 px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-orange-500"
            />
            <button
              type="submit"
              disabled={loading || !inputMessage.trim()}
              className="p-2.5 bg-orange-600 hover:bg-orange-700 text-white rounded-xl disabled:opacity-40 transition-all"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>

        </div>
      )}
    </>
  );
};

export default AiChatWidget;
