import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getRestaurantById } from '../api/restaurantApi';
import { getMenuByRestaurant } from '../api/menuApi';
import { useCart } from '../context/CartContext';
import { getAiSurgeDetails } from '../utils/aiPricingEngine';
import LoadingSpinner from '../components/LoadingSpinner';
import { MapPin, Utensils, Star, Plus, Minus, ArrowLeft, Sparkles, TrendingUp } from 'lucide-react';

const RestaurantDetailPage = () => {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const { cartItems, addToCart, updateQuantity } = useCart();

  useEffect(() => {
    fetchRestaurantAndMenu();
  }, [id]);

  const fetchRestaurantAndMenu = async () => {
    setLoading(true);
    setError('');
    try {
      const [restaurantData, menuData] = await Promise.all([
        getRestaurantById(id),
        getMenuByRestaurant(id)
      ]);
      setRestaurant(restaurantData);
      setMenuItems(menuData || []);
    } catch (err) {
      setError('Failed to load restaurant menu. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getItemCartQuantity = (menuId) => {
    const item = cartItems.find(i => i.menuId === menuId);
    return item ? item.quantity : 0;
  };

  if (loading) return <LoadingSpinner message="Loading menu items with AI real-time dynamic pricing..." />;
  if (error) {
    return (
      <div className="max-w-md mx-auto my-12 p-8 bg-red-50 border border-red-200 rounded-3xl text-center text-red-700">
        <p className="font-bold">{error}</p>
        <Link to="/" className="inline-block mt-4 text-xs font-bold text-slate-800 underline">
          Back to Restaurants
        </Link>
      </div>
    );
  }

  // Get general surge info for banner
  const sampleSurge = getAiSurgeDetails(100);

  return (
    <div className="space-y-8 pb-16">
      
      {/* Back Button */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-orange-600 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to all restaurants</span>
      </Link>

      {/* Restaurant Info Header Card */}
      {restaurant && (
        <div className="bg-gradient-to-r from-slate-900 via-orange-950 to-slate-900 rounded-3xl p-6 sm:p-8 text-white shadow-xl flex flex-col md:flex-row justify-between items-start md:items-center gap-4 sm:gap-6 relative overflow-hidden">
          <div className="space-y-2 sm:space-y-3 relative z-10">
            <span className="bg-orange-500/20 border border-orange-500/30 text-orange-400 font-bold text-xs px-3 py-1 rounded-full inline-flex items-center gap-1.5">
              <Utensils className="w-3.5 h-3.5" />
              {restaurant.cuisineType}
            </span>
            <h1 className="text-2xl sm:text-4xl md:text-5xl font-black tracking-tight">{restaurant.name}</h1>
            <p className="text-slate-300 text-xs sm:text-sm flex items-center gap-2">
              <MapPin className="w-4 h-4 text-orange-400 shrink-0" />
              {restaurant.address}
            </p>
          </div>

          <div className="bg-white/10 backdrop-blur-md border border-white/20 p-3.5 sm:p-4 rounded-2xl flex items-center gap-3 shrink-0">
            <div className="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-amber-400/20 text-amber-300 flex items-center justify-center font-bold">
              <Star className="w-4 h-4 sm:w-5 sm:h-5 fill-amber-300" />
            </div>
            <div>
              <p className="text-base sm:text-lg font-black text-white">4.8 / 5.0</p>
              <p className="text-[10px] text-slate-300 font-medium">Customer Rating</p>
            </div>
          </div>
        </div>
      )}

      {/* AI Dynamic Market Surge Pricing Banner */}
      <div className="p-3.5 sm:p-4 bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 shadow-xs">
        <div className="flex items-center gap-2 text-xs font-bold text-amber-900">
          <Sparkles className="w-4 h-4 text-orange-600 animate-pulse shrink-0" />
          <span>Real-Time AI Market Pricing Engine: <span className="font-extrabold text-orange-700">{sampleSurge.label}</span></span>
        </div>
        <span className="bg-orange-600 text-white text-[10px] font-black px-2.5 py-0.5 rounded-full uppercase flex items-center gap-1 shrink-0">
          <TrendingUp className="w-3 h-3" /> Live Market Rates
        </span>
      </div>

      {/* Menu List Section */}
      <div className="space-y-6">
        <div className="flex items-center justify-between border-b border-slate-200 pb-4">
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">Menu Items</h2>
          <span className="text-xs font-semibold text-slate-500">{menuItems.length} items available</span>
        </div>

        {menuItems.length === 0 ? (
          <div className="text-center py-16 bg-white border border-slate-200 rounded-3xl text-slate-400">
            <Utensils className="w-12 h-12 mx-auto mb-3 text-slate-300" />
            <p className="text-sm font-bold text-slate-600">No menu items added yet</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
            {menuItems.map((menu) => {
              const qty = getItemCartQuantity(menu.id);
              const surge = getAiSurgeDetails(menu.price);

              return (
                <div
                  key={menu.id}
                  className="bg-white p-4 sm:p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all flex justify-between items-center gap-3 sm:gap-4"
                >
                  <div className="space-y-1.5 flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-1.5 sm:gap-2">
                      <h3 className="font-bold text-sm sm:text-base text-slate-900 leading-snug">{menu.itemName}</h3>
                      <span className={`text-[9px] font-black px-2 py-0.5 rounded-md border ${surge.badgeColor}`}>
                        {surge.surgePercent > 0 ? `+${surge.surgePercent}% Surge` : `${surge.surgePercent}% Deal`}
                      </span>
                    </div>

                    {menu.description && (
                      <p className="text-xs text-slate-500 line-clamp-2">{menu.description}</p>
                    )}

                    <div className="flex items-baseline gap-2 mt-2">
                      <span className="text-base font-black text-orange-600">₹{surge.dynamicPrice.toFixed(2)}</span>
                      {surge.basePrice !== surge.dynamicPrice && (
                        <span className="text-xs text-slate-400 line-through font-medium">₹{surge.basePrice.toFixed(2)}</span>
                      )}
                    </div>
                  </div>

                  {/* Add / Quantity Controller */}
                  <div className="shrink-0">
                    {qty === 0 ? (
                      <button
                        onClick={() => addToCart({
                          menuId: menu.id,
                          itemName: menu.itemName,
                          price: surge.dynamicPrice
                        }, { id: restaurant.id, name: restaurant.name })}
                        className="px-4 py-2.5 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-md shadow-orange-600/20 transition-all flex items-center gap-1.5 active:scale-95"
                      >
                        <Plus className="w-4 h-4" />
                        <span>Add</span>
                      </button>
                    ) : (
                      <div className="flex items-center gap-2 bg-orange-50 border border-orange-200 px-3 py-1.5 rounded-xl">
                        <button
                          onClick={() => updateQuantity(menu.id, -1)}
                          className="p-1 text-orange-700 hover:bg-orange-200/60 rounded-lg transition-colors"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <span className="text-xs font-black text-orange-900 w-5 text-center">{qty}</span>
                        <button
                          onClick={() => updateQuantity(menu.id, 1)}
                          className="p-1 text-orange-700 hover:bg-orange-200/60 rounded-lg transition-colors"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Customer Reviews & Ratings Section */}
      <ReviewsSection restaurantId={id} />

    </div>
  );
};

const ReviewsSection = ({ restaurantId }) => {
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState({ averageRating: 4.5, totalReviews: 0 });
  const [eligibleOrders, setEligibleOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState('');
  const [ratingInput, setRatingInput] = useState(5);
  const [commentInput, setCommentInput] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [submitSuccess, setSubmitSuccess] = useState('');

  useEffect(() => {
    fetchReviewsData();
  }, [restaurantId]);

  const fetchReviewsData = async () => {
    try {
      const { getRestaurantReviewsApi, getRestaurantRatingApi } = await import('../api/reviewApi');
      const { getMyOrders } = await import('../api/orderApi');

      const [reviewsData, ratingData] = await Promise.all([
        getRestaurantReviewsApi(restaurantId).catch(() => []),
        getRestaurantRatingApi(restaurantId).catch(() => ({ averageRating: 4.5, totalReviews: 0 }))
      ]);

      setReviews(reviewsData || []);
      setRatingSummary(ratingData || { averageRating: 4.5, totalReviews: 0 });

      // Check for user's delivered orders for this restaurant
      try {
        const myOrders = await getMyOrders();
        const ordersList = Array.isArray(myOrders) ? myOrders : (myOrders.content || []);
        const delivered = ordersList.filter(o =>
          (o.status === 'DELIVERED') &&
          ((o.restaurantId && String(o.restaurantId) === String(restaurantId)) ||
           (o.restaurant && String(o.restaurant.id) === String(restaurantId)))
        );

        // Filter out orders that have already been reviewed
        const reviewedOrderIds = new Set(reviewsData.map(r => r.orderId));
        const unreviewedDelivered = delivered.filter(o => !reviewedOrderIds.has(o.id));

        setEligibleOrders(unreviewedDelivered);
        if (unreviewedDelivered.length > 0) {
          setSelectedOrder(unreviewedDelivered[0].id);
        }
      } catch (err) {
        setEligibleOrders([]);
      }
    } catch (err) {
      console.warn('Reviews section fetch notice:', err);
    }
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!selectedOrder) {
      setSubmitError('Please select a delivered order to review.');
      return;
    }
    setSubmitting(true);
    setSubmitError('');
    setSubmitSuccess('');

    try {
      const { createReviewApi } = await import('../api/reviewApi');
      await createReviewApi({
        orderId: Number(selectedOrder),
        restaurantId: Number(restaurantId),
        rating: ratingInput,
        comment: commentInput
      });

      setSubmitSuccess('Thank you! Your review has been submitted successfully.');
      setCommentInput('');
      fetchReviewsData();
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message || '';
      if (errMsg.toLowerCase().includes('already') || err.response?.status === 409 || err.response?.status === 400) {
        setSubmitError('You have already submitted a review for this order.');
      } else {
        setSubmitError(errMsg || 'Failed to submit review. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 pt-6 border-t border-slate-200">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
            Customer Reviews & Ratings
          </h2>
          <p className="text-xs text-slate-500 font-medium">Verified customer feedback & ratings</p>
        </div>

        <div className="flex items-center gap-3 bg-amber-50 border border-amber-200 px-4 py-2 rounded-2xl w-fit">
          <Star className="w-5 h-5 text-amber-500 fill-amber-400" />
          <div>
            <span className="text-base font-black text-amber-900">{ratingSummary.averageRating || 4.5}</span>
            <span className="text-xs text-amber-700 font-bold ml-1">/ 5.0</span>
            <span className="text-[10px] text-amber-600 font-semibold block">
              ({ratingSummary.totalReviews || reviews.length} reviews)
            </span>
          </div>
        </div>
      </div>

      {/* Review Submission Form (Only shown if user has an unreviewed delivered order) */}
      {eligibleOrders.length > 0 && (
        <div className="bg-gradient-to-r from-orange-50 to-amber-50 border border-orange-200 rounded-3xl p-5 sm:p-6 space-y-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h3 className="font-extrabold text-sm sm:text-base text-orange-950 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-orange-600" />
              Write a Verified Review
            </h3>
            <span className="text-[10px] bg-orange-200 text-orange-900 px-2 py-0.5 rounded-full font-bold">
              Delivered Order
            </span>
          </div>

          {submitError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs font-bold text-red-700">
              {submitError}
            </div>
          )}

          {submitSuccess && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800">
              {submitSuccess}
            </div>
          )}

          <form onSubmit={handleReviewSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Select Delivered Order</label>
                <select
                  value={selectedOrder}
                  onChange={(e) => setSelectedOrder(e.target.value)}
                  className="w-full text-xs font-medium bg-white border border-slate-300 rounded-xl p-2.5 focus:outline-none focus:border-orange-500"
                >
                  {eligibleOrders.map(o => (
                    <option key={o.id} value={o.id}>Order #{o.id} ({o.createdAt ? new Date(o.createdAt).toLocaleDateString() : 'Delivered'})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Rating</label>
                <div className="flex items-center gap-1.5 pt-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      type="button"
                      key={star}
                      onClick={() => setRatingInput(star)}
                      className="p-1 hover:scale-110 transition-transform"
                    >
                      <Star className={`w-6 h-6 ${star <= ratingInput ? 'text-amber-400 fill-amber-400' : 'text-slate-300'}`} />
                    </button>
                  ))}
                  <span className="text-xs font-black text-amber-800 ml-2">{ratingInput} Stars</span>
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Your Review / Feedback</label>
              <textarea
                rows={3}
                value={commentInput}
                onChange={(e) => setCommentInput(e.target.value)}
                placeholder="Share your feedback about the food quality, taste, and delivery experience..."
                className="w-full text-xs bg-white border border-slate-300 rounded-xl p-3 focus:outline-none focus:border-orange-500"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2.5 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center gap-2 disabled:opacity-50"
            >
              {submitting ? 'Submitting...' : 'Submit Verified Review'}
            </button>
          </form>
        </div>
      )}

      {/* Reviews List */}
      {reviews.length === 0 ? (
        <div className="text-center py-10 bg-slate-50 border border-slate-200 rounded-2xl text-slate-400">
          <Star className="w-8 h-8 mx-auto mb-2 text-slate-300" />
          <p className="text-xs font-bold text-slate-500">No reviews yet for this restaurant</p>
        </div>
      ) : (
        <div className="space-y-3">
          {reviews.map((rev) => (
            <div key={rev.id || rev.createdAt} className="bg-white p-4 rounded-2xl border border-slate-200 shadow-2xs space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-full bg-orange-100 text-orange-800 font-black text-xs flex items-center justify-center">
                    {(rev.userName || 'Customer').charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-slate-900">{rev.userName || 'Verified Buyer'}</h4>
                    <span className="text-[10px] text-slate-400">{rev.createdAt ? new Date(rev.createdAt).toLocaleDateString() : 'Recently'}</span>
                  </div>
                </div>

                <div className="flex items-center gap-1 bg-amber-50 px-2 py-1 rounded-lg border border-amber-200">
                  <Star className="w-3.5 h-3.5 text-amber-500 fill-amber-400" />
                  <span className="text-xs font-black text-amber-900">{rev.rating}.0</span>
                </div>
              </div>

              {rev.comment && (
                <p className="text-xs text-slate-600 font-medium pl-9">{rev.comment}</p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RestaurantDetailPage;
