import axios from 'axios';
import { API_BASE_URL } from '../config/apiConfig';

let inMemoryToken = null;

export const setInMemoryToken = (token) => {
  inMemoryToken = token;
};

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 45000,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = inMemoryToken || localStorage.getItem('quickeats_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    const fullUrl = (config.baseURL || '') + (config.url || '');
    console.log(`[Axios Outgoing Request] ${config.method?.toUpperCase()} ${fullUrl}`);
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Retry once on cold-start timeout or network connectivity glitch
    if (
      originalRequest &&
      !originalRequest._retryColdStart &&
      (error.code === 'ECONNABORTED' || !error.response || error.response?.status === 503)
    ) {
      originalRequest._retryColdStart = true;
      console.warn('[Axios Interceptor] Cold-start or network glitch detected. Retrying request once...');
      await new Promise((res) => setTimeout(res, 2000));
      return axiosInstance(originalRequest);
    }

    // Normalize custom error message for UI consumption
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      error.customMessage = 'Server taking longer than expected to respond (cold start). Please retry in a moment.';
    } else if (!error.response) {
      error.customMessage = 'Network error or backend server unreachable. Please check your connection or try again.';
    } else if (error.response?.data?.message) {
      error.customMessage = error.response.data.message;
    } else {
      error.customMessage = error.message || 'An unexpected error occurred.';
    }

    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          })
          .catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('quickeats_refresh_token');

      if (refreshToken) {
        try {
          const res = await axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken });
          const newAccessToken = res.data.accessToken || res.data.token;
          const newRefreshToken = res.data.refreshToken;

          if (newAccessToken) {
            setInMemoryToken(newAccessToken);
            localStorage.setItem('quickeats_token', newAccessToken);
            if (newRefreshToken) {
              localStorage.setItem('quickeats_refresh_token', newRefreshToken);
            }
            axiosInstance.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

            processQueue(null, newAccessToken);
            return axiosInstance(originalRequest);
          }
        } catch (refreshErr) {
          processQueue(refreshErr, null);
          localStorage.removeItem('quickeats_token');
          localStorage.removeItem('quickeats_refresh_token');
          localStorage.removeItem('quickeats_user');
          if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
            window.location.href = '/login';
          }
          return Promise.reject(refreshErr);
        } finally {
          isRefreshing = false;
        }
      } else {
        localStorage.removeItem('quickeats_token');
        localStorage.removeItem('quickeats_user');
        if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
