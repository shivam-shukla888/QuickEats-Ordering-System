import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Utensils, Star, ArrowRight } from 'lucide-react';

const RestaurantCard = ({ restaurant }) => {
  // Generate deterministic gradient background based on ID for aesthetics
  const gradients = [
    'from-orange-500 to-amber-500',
    'from-rose-500 to-red-500',
    'from-emerald-500 to-teal-500',
    'from-indigo-500 to-purple-500',
    'from-amber-500 to-yellow-500'
  ];
  const bgGradient = gradients[(restaurant.id || 0) % gradients.length];

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 overflow-hidden flex flex-col group">
      
      {/* Banner Header */}
      <div className={`h-36 bg-gradient-to-br ${bgGradient} p-4 flex flex-col justify-between relative overflow-hidden`}>
        <div className="absolute inset-0 bg-black/10 backdrop-blur-[1px]" />
        
        <div className="relative z-10 flex justify-between items-start">
          <span className="bg-white/90 backdrop-blur-md text-slate-800 font-bold text-xs px-2.5 py-1 rounded-full shadow-sm flex items-center gap-1">
            <Utensils className="w-3 h-3 text-orange-600" />
            {restaurant.cuisineType}
          </span>
          <span className="bg-slate-900/80 backdrop-blur-md text-amber-300 font-bold text-xs px-2 py-1 rounded-full flex items-center gap-1">
            <Star className="w-3 h-3 fill-amber-300" />
            4.8
          </span>
        </div>

        <div className="relative z-10">
          <h3 className="text-xl font-black text-white tracking-tight drop-shadow-sm group-hover:text-amber-200 transition-colors">
            {restaurant.name}
          </h3>
        </div>
      </div>

      {/* Body Details */}
      <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
        <div className="flex items-start gap-2 text-slate-500 text-xs font-medium">
          <MapPin className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
          <span className="line-clamp-2">{restaurant.address}</span>
        </div>

        {/* Footer CTA */}
        <Link
          to={`/restaurant/${restaurant.id}`}
          className="w-full py-2.5 px-4 bg-slate-50 hover:bg-orange-50 text-slate-700 hover:text-orange-600 font-bold text-xs rounded-xl border border-slate-200 hover:border-orange-200 flex items-center justify-center gap-2 transition-all group-hover:bg-orange-600 group-hover:text-white group-hover:border-orange-600"
        >
          <span>View Menu</span>
          <ArrowRight className="w-4 h-4" />
        </Link>
      </div>

    </div>
  );
};

export default RestaurantCard;
