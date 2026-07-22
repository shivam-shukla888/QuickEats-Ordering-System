import React from 'react';
import { Utensils } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="bg-slate-900 text-slate-400 py-12 border-t border-slate-800 mt-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-6">
        
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-orange-600 flex items-center justify-center text-white font-bold">
            <Utensils className="w-4 h-4" />
          </div>
          <span className="text-white font-bold text-lg">QuickEats</span>
          <span className="text-xs text-slate-500">| Fast & Fresh Food Delivery</span>
        </div>

        <p className="text-xs text-slate-500">
          © {new Date().getFullYear()} QuickEats Ordering System. All rights reserved.
        </p>
      </div>
    </footer>
  );
};

export default Footer;
