import React, { useState, useEffect } from 'react';
import { getRestaurants, getCuisines, getRestaurantsByCuisine } from '../api/restaurantApi';
import { searchMenuItemsAi } from '../api/searchApi';
import { useCart } from '../context/CartContext';
import RestaurantCard from '../components/RestaurantCard';
import LoadingSpinner from '../components/LoadingSpinner';
import { Search, Utensils, ChevronLeft, ChevronRight, SlidersHorizontal, Sparkles, X, Plus, Flame, Leaf } from 'lucide-react';

const HomePage = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [dishResults, setDishResults] = useState([]);
  const [cuisines, setCuisines] = useState([]);
  const [selectedCuisine, setSelectedCuisine] = useState('');
  
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [isAiSearch, setIsAiSearch] = useState(false);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Pagination state
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 8;

  const { addToCart, deliveryLocation } = useCart();

  // Re-fetch restaurants when delivery location changes
  useEffect(() => {
    setPage(0);
    fetchRestaurantsList();
  }, [deliveryLocation]);

  // Debounce search query input by 500ms
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(searchQuery);
      setPage(0);
    }, 500);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  // Load initial cuisines list
  useEffect(() => {
    fetchCuisinesList();
  }, []);

  // Fetch data whenever page, cuisine, or debounced search query changes
  useEffect(() => {
    if (debouncedQuery.trim()) {
      setIsAiSearch(true);
      fetchAiSearchResults();
    } else {
      setIsAiSearch(false);
      fetchRestaurantsList();
    }
  }, [page, selectedCuisine, debouncedQuery]);

  const fetchCuisinesList = async () => {
    try {
      const data = await getCuisines();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setCuisines(list);
    } catch (err) {
      console.error('Failed to load cuisines:', err);
      setCuisines([]);
    }
  };

  const fetchRestaurantsList = async () => {
    setLoading(true);
    setError('');
    try {
      let pageData;
      if (selectedCuisine) {
        pageData = await getRestaurantsByCuisine(selectedCuisine, page, pageSize);
      } else {
        pageData = await getRestaurants(page, pageSize);
      }
      const list = Array.isArray(pageData) ? pageData : (pageData?.content || []);
      setRestaurants(list);
      setTotalPages(pageData?.totalPages || 0);
      setTotalElements(pageData?.totalElements || list.length);
    } catch (err) {
      setError('Unable to connect to QuickEats servers right now. Please verify your connection or try again.');
      setRestaurants([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchAiSearchResults = async () => {
    setLoading(true);
    setError('');
    try {
      const pageData = await searchMenuItemsAi(debouncedQuery.trim(), page, pageSize);
      const list = Array.isArray(pageData) ? pageData : (pageData?.content || []);
      setDishResults(list);
      setTotalPages(pageData?.totalPages || 0);
      setTotalElements(pageData?.totalElements || list.length);
    } catch (err) {
      setError('QuickEats AI assistant is currently updating. Please rephrase your search query.');
      setDishResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    setDebouncedQuery('');
    setDishResults([]);
  };

  return (
    <div className="space-y-10 pb-12">
      
      {/* Hero Section with AI Search */}
      <section className="relative rounded-3xl bg-gradient-to-r from-slate-900 via-emerald-950 to-slate-900 text-white p-8 sm:p-14 overflow-hidden shadow-xl border border-slate-800">
        <div className="absolute -right-20 -bottom-20 w-96 h-96 bg-emerald-600/20 rounded-full blur-3xl pointer-events-none" />
        
        <div className="max-w-2xl space-y-6 relative z-10">
          <div className="inline-flex items-center gap-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-bold px-3.5 py-1.5 rounded-full">
            <Sparkles className="w-3.5 h-3.5" />
            <span>AI-Powered Culinary Search</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight leading-none text-white font-outfit">
            Craving something? <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-teal-200">Ask QuickEats AI.</span>
          </h1>

          <p className="text-slate-300 text-sm sm:text-base font-medium">
            Try searching naturally like <span className="text-emerald-300 font-semibold">"spicy chinese under 300"</span>, <span className="text-emerald-300 font-semibold">"pure veg paneer"</span>, or <span className="text-emerald-300 font-semibold">"light healthy salad"</span>!
          </p>

          {/* AI Search Form */}
          <div className="relative max-w-xl pt-2">
            <div className="relative flex items-center">
              <Search className="w-5 h-5 text-orange-400 absolute left-4 pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder='Search dishes (e.g. "spicy chicken biryani under 15")...'
                className="w-full pl-12 pr-12 py-3.5 bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl text-white placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 shadow-inner"
              />
              {searchQuery && (
                <button
                  onClick={handleClearSearch}
                  className="absolute right-4 text-slate-400 hover:text-white transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Cuisine Filter Pills (visible when not AI searching) */}
      {!isAiSearch && cuisines.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <SlidersHorizontal className="w-4 h-4 text-orange-600" />
              Browse by Cuisine
            </h2>
            {selectedCuisine && (
              <button
                onClick={() => setSelectedCuisine('')}
                className="text-xs font-semibold text-orange-600 hover:underline"
              >
                Clear Filter
              </button>
            )}
          </div>

          <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
            <button
              onClick={() => handleCuisineSelect('')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shrink-0 ${
                selectedCuisine === ''
                  ? 'bg-slate-900 text-white shadow-md'
                  : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              All Cuisines
            </button>
            {Array.isArray(cuisines) && cuisines.map((cuisine) => (
              <button
                key={cuisine}
                onClick={() => handleCuisineSelect(cuisine)}
                className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shrink-0 ${
                  selectedCuisine === cuisine
                    ? 'bg-orange-600 text-white shadow-md shadow-orange-600/20'
                    : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                {cuisine}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Search Header Banner if AI Search is Active */}
      {isAiSearch && (
        <div className="p-4 bg-orange-50 border border-orange-200 rounded-2xl flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs font-bold text-orange-900">
            <Sparkles className="w-4 h-4 text-orange-600 animate-pulse" />
            <span>Showing AI search results for: <span className="text-orange-700 italic">"{debouncedQuery}"</span></span>
          </div>
          <button
            onClick={handleClearSearch}
            className="text-xs font-bold text-orange-700 hover:text-orange-900 underline"
          >
            Clear Search
          </button>
        </div>
      )}

      {/* Content Section: Dish Cards (AI Search) OR Restaurant Cards */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-black text-slate-900 tracking-tight">
            {isAiSearch
              ? 'Dishes Found'
              : selectedCuisine
              ? `${selectedCuisine} Restaurants`
              : 'Featured Restaurants'}
          </h2>
          <span className="text-xs font-semibold text-slate-500">
            Showing {isAiSearch ? dishResults.length : restaurants.length} of {totalElements} items
          </span>
        </div>

        {/* Loading / Error States */}
        {loading ? (
          <LoadingSpinner message={isAiSearch ? 'AI is interpreting your craving...' : 'Fetching delicious spots...'} />
        ) : error ? (
          <div className="p-8 bg-red-50 border border-red-200 rounded-3xl text-center text-red-700 space-y-2">
            <p className="font-bold">{error}</p>
            <button
              onClick={isAiSearch ? fetchAiSearchResults : fetchRestaurantsList}
              className="px-4 py-2 bg-red-600 text-white text-xs font-bold rounded-xl hover:bg-red-700"
            >
              Retry
            </button>
          </div>
        ) : isAiSearch ? (
          /* Dish Cards Grid for AI Search */
          dishResults.length === 0 ? (
            <div className="text-center py-16 bg-white border border-slate-200 rounded-3xl">
              <Utensils className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <h3 className="text-base font-bold text-slate-700">No dishes matched your AI search</h3>
              <p className="text-xs text-slate-400 mt-1">Try rephrasing your search or checking price filters.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {dishResults.map((dish) => (
                <div key={dish.id} className="bg-white rounded-2xl border border-slate-200/80 p-5 flex flex-col justify-between shadow-sm hover:shadow-xl transition-all">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      {dish.isVeg ? (
                        <span className="bg-emerald-50 text-emerald-700 border border-emerald-200 font-bold text-[10px] px-2 py-0.5 rounded-md flex items-center gap-1">
                          <Leaf className="w-3 h-3 text-emerald-600" /> Veg
                        </span>
                      ) : (
                        <span className="bg-red-50 text-red-700 border border-red-200 font-bold text-[10px] px-2 py-0.5 rounded-md">
                          Non-Veg
                        </span>
                      )}

                      {dish.spiceLevel && (
                        <span className="bg-amber-50 text-amber-700 border border-amber-200 font-bold text-[10px] px-2 py-0.5 rounded-md flex items-center gap-1">
                          <Flame className="w-3 h-3 text-orange-500" /> {dish.spiceLevel}
                        </span>
                      )}
                    </div>

                    <h3 className="font-bold text-base text-slate-900 leading-snug">{dish.itemName}</h3>
                    <p className="text-xs text-slate-400 font-medium">from <span className="text-slate-700 font-bold">{dish.restaurantName}</span> ({dish.cuisineType})</p>
                    {dish.description && <p className="text-xs text-slate-500 line-clamp-2 mt-1">{dish.description}</p>}
                  </div>

                  <div className="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between">
                    <span className="text-lg font-black text-orange-600">₹{dish.price?.toFixed(2)}</span>
                    <button
                      onClick={() => addToCart({
                        menuId: dish.id,
                        itemName: dish.itemName,
                        price: dish.price
                      }, { id: dish.restaurantId, name: dish.restaurantName })}
                      className="px-3.5 py-2 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-md shadow-orange-600/20 flex items-center gap-1 transition-all"
                    >
                      <Plus className="w-4 h-4" /> Add
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )
        ) : (
          /* Restaurant Cards Grid */
          restaurants.length === 0 ? (
            <div className="text-center py-16 bg-white border border-slate-200 rounded-3xl">
              <Utensils className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <h3 className="text-base font-bold text-slate-700">No restaurants found</h3>
              <p className="text-xs text-slate-400 mt-1">Try clearing your filters.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {restaurants.map((restaurant) => (
                <RestaurantCard key={restaurant.id} restaurant={restaurant} />
              ))}
            </div>
          )
        )}

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-4 pt-6">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-2.5 bg-white border border-slate-200 rounded-xl font-semibold text-xs text-slate-700 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed shadow-sm"
            >
              <ChevronLeft className="w-5 h-5" />
            </button>
            <span className="text-xs font-bold text-slate-600">
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="p-2.5 bg-white border border-slate-200 rounded-xl font-semibold text-xs text-slate-700 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed shadow-sm"
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        )}
      </div>

    </div>
  );
};

export default HomePage;
