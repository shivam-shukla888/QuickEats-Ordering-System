import React from 'react';

const LoadingSpinner = ({ message = 'Loading...' }) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 min-h-[300px]">
      <div className="w-12 h-12 border-4 border-orange-200 border-t-orange-600 rounded-full animate-spin"></div>
      <p className="mt-4 text-sm font-medium text-slate-500">{message}</p>
    </div>
  );
};

export default LoadingSpinner;
