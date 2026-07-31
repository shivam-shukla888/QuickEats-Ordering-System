import React from 'react';

const LoadingSpinner = ({ message = 'Loading...', subMessage = null }) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 min-h-[300px] text-center">
      <div className="w-12 h-12 border-4 border-orange-200 border-t-orange-600 rounded-full animate-spin"></div>
      <p className="mt-4 text-sm font-semibold text-slate-700">{message}</p>
      {subMessage && (
        <div className="mt-3 p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs font-medium text-amber-800 max-w-md animate-fade-in shadow-sm">
          ☕ {subMessage}
        </div>
      )}
    </div>
  );
};

export default LoadingSpinner;
