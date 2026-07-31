import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { loginUser, registerUser } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import { Utensils, Mail, Lock, ArrowRight, AlertCircle, Eye, EyeOff, Sparkles, Zap, Coffee } from 'lucide-react';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isMounted, setIsMounted] = useState(false);
  const [windowWidth, setWindowWidth] = useState(typeof window !== 'undefined' ? window.innerWidth : 1024);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  useEffect(() => {
    setIsMounted(true);
    const handleResize = () => setWindowWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    let interval;
    if (submitting) {
      setElapsedSeconds(0);
      interval = setInterval(() => {
        setElapsedSeconds((prev) => prev + 1);
      }, 1000);
    } else {
      setElapsedSeconds(0);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [submitting]);

  const isMobile = windowWidth < 640;
  const isTablet = windowWidth >= 640 && windowWidth < 1024;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const authData = await loginUser({ email, password });
      login(authData);
      navigate(from, { replace: true });
    } catch (err) {
      if (err.customMessage) {
        setError(err.customMessage);
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status) {
        setError(`Login failed (status ${err.response.status}). Please try again.`);
      } else {
        setError('Unable to connect to server. Please check your internet connection and try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDemoLogin = async (demoEmail, demoPassword, role = 'CUSTOMER', name = 'Demo User') => {
    setEmail(demoEmail);
    setPassword(demoPassword);
    setSubmitting(true);
    setError('');

    try {
      let authData;
      try {
        authData = await loginUser({ email: demoEmail, password: demoPassword });
      } catch (loginErr) {
        authData = await registerUser({
          name,
          email: demoEmail,
          password: demoPassword,
          role,
          address: '123 Main Street',
          phone: '9876543210',
        });
      }
      login(authData);
      navigate(from, { replace: true });
    } catch (err) {
      if (err.customMessage) {
        setError(err.customMessage);
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status) {
        setError(`Authentication failed (status ${err.response.status}). Please try again.`);
      } else {
        setError('Direct authentication failed. Please check server connection.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 80px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: isMobile ? '16px' : isTablet ? '32px' : '48px',
        position: 'relative',
      }}
    >
      {/* Ambient background blobs */}
      <div
        style={{
          position: 'fixed',
          inset: 0,
          overflow: 'hidden',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      >
        <div
          style={{
            position: 'absolute',
            top: '-20%',
            right: '-10%',
            width: isMobile ? '300px' : '500px',
            height: isMobile ? '300px' : '500px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(234,88,12,0.08) 0%, transparent 70%)',
            filter: 'blur(60px)',
          }}
        />
        <div
          style={{
            position: 'absolute',
            bottom: '-15%',
            left: '-10%',
            width: isMobile ? '250px' : '400px',
            height: isMobile ? '250px' : '400px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(251,146,60,0.06) 0%, transparent 70%)',
            filter: 'blur(60px)',
          }}
        />
      </div>

      {/* Login Card */}
      <div
        style={{
          position: 'relative',
          zIndex: 1,
          width: '100%',
          maxWidth: isMobile ? '100%' : isTablet ? '440px' : '420px',
          background: 'rgba(255,255,255,0.85)',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          borderRadius: isMobile ? '20px' : '28px',
          border: '1px solid rgba(226,232,240,0.8)',
          boxShadow: '0 25px 50px -12px rgba(0,0,0,0.08), 0 0 0 1px rgba(255,255,255,0.5) inset',
          padding: isMobile ? '28px 20px' : isTablet ? '40px 36px' : '44px 40px',
          opacity: isMounted ? 1 : 0,
          transform: isMounted ? 'translateY(0)' : 'translateY(20px)',
          transition: 'opacity 0.5s ease-out, transform 0.5s ease-out',
        }}
      >
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: isMobile ? '24px' : '32px' }}>
          <div
            style={{
              width: isMobile ? '52px' : '60px',
              height: isMobile ? '52px' : '60px',
              borderRadius: '18px',
              background: 'linear-gradient(135deg, #ea580c 0%, #f97316 50%, #fb923c 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 16px',
              boxShadow: '0 8px 24px rgba(234,88,12,0.3), 0 0 0 4px rgba(234,88,12,0.08)',
              position: 'relative',
            }}
          >
            <Utensils style={{ width: isMobile ? '24px' : '28px', height: isMobile ? '24px' : '28px', color: 'white' }} />
            <Sparkles
              style={{
                position: 'absolute',
                top: '-6px',
                right: '-6px',
                width: '16px',
                height: '16px',
                color: '#f97316',
              }}
            />
          </div>
          <h1
            style={{
              fontSize: isMobile ? '22px' : '26px',
              fontWeight: 900,
              color: '#0f172a',
              letterSpacing: '-0.025em',
              marginBottom: '6px',
              fontFamily: "'Plus Jakarta Sans', 'Inter', sans-serif",
            }}
          >
            Welcome back
          </h1>
          <p
            style={{
              fontSize: isMobile ? '12px' : '13px',
              color: '#64748b',
              fontWeight: 500,
            }}
          >
            Sign in to your QuickEats account
          </p>
        </div>

        {/* Error Banner */}
        {error && (
          <div
            style={{
              padding: '12px 14px',
              background: 'linear-gradient(135deg, #fef2f2, #fff1f2)',
              border: '1px solid #fecaca',
              borderRadius: '14px',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '10px',
              marginBottom: '20px',
              animation: 'shake 0.4s ease-in-out',
            }}
          >
            <AlertCircle style={{ width: '16px', height: '16px', color: '#dc2626', flexShrink: 0, marginTop: '1px' }} />
            <span style={{ fontSize: '12px', fontWeight: 600, color: '#b91c1c', lineHeight: 1.5 }}>{error}</span>
          </div>
        )}

        {/* Direct Authentication Quick Buttons */}
        <div style={{ marginBottom: '20px' }}>
          <div style={{ fontSize: '11px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: '8px', textAlign: 'center' }}>
            ⚡ Direct Quick Login
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <button
              type="button"
              onClick={() => handleDemoLogin('user@quickeats.com', 'password123', 'CUSTOMER', 'Demo Customer')}
              disabled={submitting}
              style={{
                padding: '10px 14px',
                background: '#f8fafc',
                border: '1.5px solid #e2e8f0',
                borderRadius: '12px',
                fontSize: '13px',
                fontWeight: 700,
                color: '#334155',
                cursor: submitting ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                transition: 'all 0.2s',
              }}
              onMouseEnter={(e) => {
                if (!submitting) {
                  e.target.style.borderColor = '#ea580c';
                  e.target.style.color = '#ea580c';
                  e.target.style.background = '#fff7ed';
                }
              }}
              onMouseLeave={(e) => {
                if (!submitting) {
                  e.target.style.borderColor = '#e2e8f0';
                  e.target.style.color = '#334155';
                  e.target.style.background = '#f8fafc';
                }
              }}
            >
              <Zap style={{ width: '14px', height: '14px', color: '#ea580c' }} />
              Customer Access
            </button>
            <button
              type="button"
              onClick={() => handleDemoLogin('admin@quickeats.com', 'admin123', 'ADMIN', 'Demo Admin')}
              disabled={submitting}
              style={{
                padding: '10px 14px',
                background: '#f8fafc',
                border: '1.5px solid #e2e8f0',
                borderRadius: '12px',
                fontSize: '13px',
                fontWeight: 700,
                color: '#334155',
                cursor: submitting ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                transition: 'all 0.2s',
              }}
              onMouseEnter={(e) => {
                if (!submitting) {
                  e.target.style.borderColor = '#ea580c';
                  e.target.style.color = '#ea580c';
                  e.target.style.background = '#fff7ed';
                }
              }}
              onMouseLeave={(e) => {
                if (!submitting) {
                  e.target.style.borderColor = '#e2e8f0';
                  e.target.style.color = '#334155';
                  e.target.style.background = '#f8fafc';
                }
              }}
            >
              <Zap style={{ width: '14px', height: '14px', color: '#ea580c' }} />
              Admin Access
            </button>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Email Field */}
          <div style={{ marginBottom: '16px' }}>
            <label
              style={{
                display: 'block',
                fontSize: '11px',
                fontWeight: 700,
                color: '#475569',
                textTransform: 'uppercase',
                letterSpacing: '0.08em',
                marginBottom: '8px',
              }}
            >
              Email Address
            </label>
            <div style={{ position: 'relative' }}>
              <Mail
                style={{
                  width: '18px',
                  height: '18px',
                  color: '#94a3b8',
                  position: 'absolute',
                  left: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  pointerEvents: 'none',
                }}
              />
              <input
                id="login-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                style={{
                  width: '100%',
                  paddingLeft: '44px',
                  paddingRight: '16px',
                  paddingTop: isMobile ? '14px' : '13px',
                  paddingBottom: isMobile ? '14px' : '13px',
                  background: '#f8fafc',
                  border: '1.5px solid #e2e8f0',
                  borderRadius: '14px',
                  fontSize: isMobile ? '15px' : '14px',
                  color: '#0f172a',
                  outline: 'none',
                  transition: 'border-color 0.2s, box-shadow 0.2s',
                  boxSizing: 'border-box',
                }}
                onFocus={(e) => {
                  e.target.style.borderColor = '#f97316';
                  e.target.style.boxShadow = '0 0 0 3px rgba(249,115,22,0.1)';
                  e.target.style.background = '#ffffff';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = '#e2e8f0';
                  e.target.style.boxShadow = 'none';
                  e.target.style.background = '#f8fafc';
                }}
              />
            </div>
          </div>

          {/* Password Field */}
          <div style={{ marginBottom: '24px' }}>
            <label
              style={{
                display: 'block',
                fontSize: '11px',
                fontWeight: 700,
                color: '#475569',
                textTransform: 'uppercase',
                letterSpacing: '0.08em',
                marginBottom: '8px',
              }}
            >
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <Lock
                style={{
                  width: '18px',
                  height: '18px',
                  color: '#94a3b8',
                  position: 'absolute',
                  left: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  pointerEvents: 'none',
                }}
              />
              <input
                id="login-password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                style={{
                  width: '100%',
                  paddingLeft: '44px',
                  paddingRight: '48px',
                  paddingTop: isMobile ? '14px' : '13px',
                  paddingBottom: isMobile ? '14px' : '13px',
                  background: '#f8fafc',
                  border: '1.5px solid #e2e8f0',
                  borderRadius: '14px',
                  fontSize: isMobile ? '15px' : '14px',
                  color: '#0f172a',
                  outline: 'none',
                  transition: 'border-color 0.2s, box-shadow 0.2s',
                  boxSizing: 'border-box',
                }}
                onFocus={(e) => {
                  e.target.style.borderColor = '#f97316';
                  e.target.style.boxShadow = '0 0 0 3px rgba(249,115,22,0.1)';
                  e.target.style.background = '#ffffff';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = '#e2e8f0';
                  e.target.style.boxShadow = 'none';
                  e.target.style.background = '#f8fafc';
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#94a3b8',
                }}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? (
                  <EyeOff style={{ width: '18px', height: '18px' }} />
                ) : (
                  <Eye style={{ width: '18px', height: '18px' }} />
                )}
              </button>
            </div>
          </div>

          {/* Cold Start Warning Banner (shows after ~18s delay if Render backend is sleeping) */}
          {submitting && elapsedSeconds >= 18 && (
            <div
              style={{
                padding: '12px 14px',
                background: 'linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%)',
                border: '1.5px solid #fde68a',
                borderRadius: '14px',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px',
                marginBottom: '16px',
                boxShadow: '0 4px 12px rgba(217,119,6,0.1)',
              }}
            >
              <Coffee style={{ width: '18px', height: '18px', color: '#d97706', flexShrink: 0, marginTop: '2px' }} />
              <div style={{ fontSize: '12px', color: '#92400e', lineHeight: 1.45, fontWeight: 500 }}>
                <strong style={{ display: 'block', marginBottom: '2px', color: '#78350f' }}>☕ Server is spinning up... ({elapsedSeconds}s)</strong>
                Render's free tier backend sleeps after 15m of inactivity. Wake-up takes 30–90 seconds on first request. Thank you for your patience!
              </div>
            </div>
          )}

          {/* Submit Button */}
          <button
            id="login-submit"
            type="submit"
            disabled={submitting}
            style={{
              width: '100%',
              padding: isMobile ? '15px' : '14px',
              background: submitting
                ? 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)'
                : 'linear-gradient(135deg, #ea580c 0%, #f97316 50%, #fb923c 100%)',
              color: 'white',
              fontWeight: 800,
              fontSize: isMobile ? '14px' : '13px',
              borderRadius: '14px',
              border: 'none',
              cursor: submitting ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              boxShadow: submitting ? 'none' : '0 8px 24px rgba(234,88,12,0.25), 0 2px 8px rgba(234,88,12,0.15)',
              transition: 'all 0.2s ease',
              opacity: submitting ? 0.85 : 1,
              letterSpacing: '-0.01em',
              fontFamily: "'Plus Jakarta Sans', 'Inter', sans-serif",
            }}
            onMouseEnter={(e) => {
              if (!submitting) {
                e.target.style.transform = 'translateY(-1px)';
                e.target.style.boxShadow = '0 12px 32px rgba(234,88,12,0.35), 0 4px 12px rgba(234,88,12,0.2)';
              }
            }}
            onMouseLeave={(e) => {
              e.target.style.transform = 'translateY(0)';
              if (!submitting) {
                e.target.style.boxShadow = '0 8px 24px rgba(234,88,12,0.25), 0 2px 8px rgba(234,88,12,0.15)';
              }
            }}
          >
            {submitting ? (
              <>
                <svg
                  style={{ width: '18px', height: '18px', animation: 'spin 1s linear infinite', flexShrink: 0 }}
                  viewBox="0 0 24 24"
                  fill="none"
                >
                  <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" strokeDasharray="60" strokeLinecap="round" opacity="0.3" />
                  <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
                </svg>
                <span style={{ textAlign: 'center' }}>
                  {elapsedSeconds >= 18
                    ? `Waking up free-tier server (${elapsedSeconds}s)...`
                    : `Connecting to server, this may take up to a minute...`}
                </span>
              </>
            ) : (
              <>
                <span>Sign In</span>
                <ArrowRight style={{ width: '18px', height: '18px' }} />
              </>
            )}
          </button>
        </form>

        {/* Divider & Register Link */}
        <div
          style={{
            textAlign: 'center',
            paddingTop: '24px',
            marginTop: '24px',
            borderTop: '1px solid #f1f5f9',
          }}
        >
          <p style={{ fontSize: '13px', color: '#64748b' }}>
            Don't have an account?{' '}
            <Link
              to="/register"
              style={{
                fontWeight: 700,
                color: '#ea580c',
                textDecoration: 'none',
                transition: 'color 0.2s',
              }}
              onMouseEnter={(e) => (e.target.style.textDecoration = 'underline')}
              onMouseLeave={(e) => (e.target.style.textDecoration = 'none')}
            >
              Register here
            </Link>
          </p>
        </div>
      </div>

      {/* Keyframe animations injected via style tag */}
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          20% { transform: translateX(-6px); }
          40% { transform: translateX(6px); }
          60% { transform: translateX(-4px); }
          80% { transform: translateX(4px); }
        }
      `}</style>
    </div>
  );
};

export default LoginPage;
