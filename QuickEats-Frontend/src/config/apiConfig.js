// Centralized API Base URL configuration for QuickEats Frontend
const rawUrl = import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL || 'https://quickeats-ordering-system.onrender.com';

/**
 * Sanitizes API Base URL:
 * 1. Fixes single-slash typos like "https:/domain.com" -> "https://domain.com"
 * 2. Strips trailing slashes to prevent double slashes in requests (e.g. "//api/auth/login")
 */
const sanitizeUrl = (url) => {
  if (!url) return 'https://quickeats-ordering-system.onrender.com';
  let cleaned = url.trim();
  // Fix malformed protocol like "https:/quickeats..." -> "https://quickeats..."
  if (cleaned.startsWith('https:/') && !cleaned.startsWith('https://')) {
    cleaned = cleaned.replace('https:/', 'https://');
  } else if (cleaned.startsWith('http:/') && !cleaned.startsWith('http://')) {
    cleaned = cleaned.replace('http:/', 'http://');
  }
  // Strip trailing slashes
  cleaned = cleaned.replace(/\/+$/, '');
  return cleaned;
};

export const API_BASE_URL = sanitizeUrl(rawUrl);
console.log('[API Config] Resolved API_BASE_URL:', API_BASE_URL);
