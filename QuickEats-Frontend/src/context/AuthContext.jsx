import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import { setInMemoryToken } from '../api/axiosInstance';
import { API_BASE_URL } from '../config/apiConfig';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('quickeats_token') || null);
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('quickeats_user');
    try {
      return savedUser ? JSON.parse(savedUser) : null;
    } catch (e) {
      return null;
    }
  });

  const [initializing, setInitializing] = useState(true);

  // Silent Re-Authentication on Page Reload / App Launch using Refresh Token
  useEffect(() => {
    const silentRefresh = async () => {
      const storedRefreshToken = localStorage.getItem('quickeats_refresh_token');
      if (storedRefreshToken) {
        try {
          const res = await axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken: storedRefreshToken }, { timeout: 90000 });
          const newAccessToken = res.data.accessToken || res.data.token;
          const newRefreshToken = res.data.refreshToken;
          const userData = res.data.user || user;

          if (newAccessToken) {
            setToken(newAccessToken);
            setInMemoryToken(newAccessToken);
            setUser(userData);
            localStorage.setItem('quickeats_token', newAccessToken);
            if (newRefreshToken) {
              localStorage.setItem('quickeats_refresh_token', newRefreshToken);
            }
            if (userData) {
              localStorage.setItem('quickeats_user', JSON.stringify(userData));
            }
          }
        } catch (e) {
          console.warn('Silent re-authentication refresh failed:', e);
          // Clear invalid tokens
          setToken(null);
          setInMemoryToken(null);
          setUser(null);
          localStorage.removeItem('quickeats_token');
          localStorage.removeItem('quickeats_refresh_token');
          localStorage.removeItem('quickeats_user');
        }
      } else {
        // No refresh token saved, start completely clean
        setToken(null);
        setInMemoryToken(null);
        setUser(null);
      }
      setInitializing(false);
    };

    silentRefresh();
  }, []);

  const login = (authData) => {
    const accessToken = authData.accessToken || authData.token;
    const refreshToken = authData.refreshToken;
    const userData = authData.user;

    setToken(accessToken);
    setInMemoryToken(accessToken);
    setUser(userData);

    if (accessToken) {
      localStorage.setItem('quickeats_token', accessToken);
    }
    if (refreshToken) {
      localStorage.setItem('quickeats_refresh_token', refreshToken);
    }
    if (userData) {
      localStorage.setItem('quickeats_user', JSON.stringify(userData));
    }
  };

  const logout = async () => {
    const storedRefreshToken = localStorage.getItem('quickeats_refresh_token');
    if (storedRefreshToken) {
      try {
        await axios.post(`${API_BASE_URL}/api/auth/logout`, { refreshToken: storedRefreshToken }, { timeout: 90000 });
      } catch (e) {
        console.warn('Logout endpoint call error:', e);
      }
    }

    setToken(null);
    setInMemoryToken(null);
    setUser(null);

    localStorage.removeItem('quickeats_token');
    localStorage.removeItem('quickeats_refresh_token');
    localStorage.removeItem('quickeats_user');
    localStorage.removeItem('quickeats_cart');
    localStorage.removeItem('quickeats_cart_restaurant');
  };

  return (
    <AuthContext.Provider value={{ token, user, isAuthenticated: !!token, login, logout, initializing }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
