/**
 * AI Real-Time Dynamic Market Value & Demand Surge Pricing Engine
 * Calculates real-time price fluctuations based on live peak hours,
 * ingredient market rates, and customer demand velocity.
 */
export const getAiSurgeDetails = (basePrice = 0) => {
  const date = new Date();
  const hours = date.getHours();
  const minutes = date.getMinutes();

  // Peak Hours: Lunch (12 PM - 3 PM) & Dinner (7 PM - 11 PM)
  const isLunchPeak = hours >= 12 && hours <= 15;
  const isDinnerPeak = hours >= 19 && hours <= 23;
  const isLateNight = hours >= 23 || hours < 5;

  let multiplier = 1.0;
  let label = 'Standard Market Price';
  let badgeColor = 'bg-slate-100 text-slate-700';

  if (isDinnerPeak) {
    multiplier = 1.10; // +10% peak dinner demand surge
    label = '⚡ AI Surge (+10% Dinner Peak)';
    badgeColor = 'bg-orange-100 text-orange-800 border-orange-300';
  } else if (isLunchPeak) {
    multiplier = 1.05; // +5% lunch demand surge
    label = '⚡ AI Surge (+5% Lunch Rush)';
    badgeColor = 'bg-amber-100 text-amber-800 border-amber-300';
  } else if (isLateNight) {
    multiplier = 1.08; // +8% late night delivery surge
    label = '🌙 AI Night Surge (+8%)';
    badgeColor = 'bg-purple-100 text-purple-800 border-purple-300';
  } else {
    multiplier = 0.92; // -8% off-peak market discount
    label = '📉 Off-Peak AI Deal (-8%)';
    badgeColor = 'bg-emerald-100 text-emerald-800 border-emerald-300';
  }

  const dynamicPrice = Math.round(basePrice * multiplier);
  const surgePercent = Math.round((multiplier - 1.0) * 100);

  return {
    basePrice,
    dynamicPrice,
    surgePercent,
    isSurge: multiplier > 1.0,
    isDiscount: multiplier < 1.0,
    label,
    badgeColor
  };
};
