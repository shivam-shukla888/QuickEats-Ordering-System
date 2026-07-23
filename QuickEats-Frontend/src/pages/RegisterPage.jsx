import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api/authApi';
import { useAuth } from '../context/AuthContext';
import { Utensils, User as UserIcon, Mail, Lock, Shield, ArrowRight, AlertCircle, Eye, EyeOff, Sparkles } from 'lucide-react';

const RegisterPage = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('CUSTOMER');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isMounted, setIsMounted] = useState(false);
  const [windowWidth, setWindowWidth] = useState(typeof window !== 'undefined' ? window.innerWidth : 1024);

  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    setIsMounted(true);
    const handleResize = () => setWindowWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const isMobile = windowWidth < 640;
  const isTablet = windowWidth >= 640 && windowWidth < 1024;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name || !email || !password) {
      setError('Please fill in all required fields.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const authData = await registerUser({ name, email, password, role });
      // AuthController returns AuthResponseDTO with tokens, auto-login the user
      if (authData.accessToken || authData.token) {
        login(authData);
        navigate('/', { replace: true });
      } else {
        navigate('/login', { state: { registered: true } });
      }
    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status) {
        setError(`Registration failed (status ${err.response.status}). Please try again.`);
      } else {
        setError('Unable to connect to server. Please check your internet connection and try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const inputStyle = {
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
  };

  const handleFocus = (e) => {
    e.target.style.borderColor = '#f97316';
    e.target.style.boxShadow = '0 0 0 3px rgba(249,115,22,0.1)';
    e.target.style.background = '#ffffff';
  };

  const handleBlur = (e) => {
    e.target.style.borderColor = '#e2e8f0';
    e.target.style.boxShadow = 'none';
    e.target.style.background = '#f8fafc';
  };

  const labelStyle = {
    display: 'block',
    fontSize: '11px',
    fontWeight: 700,
    color: '#475569',
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    marginBottom: '8px',
  };

  const iconStyle = {
    width: '18px',
    height: '18px',
    color: '#94a3b8',
    position: 'absolute',
    left: '14px',
    top: '50%',
    transform: 'translateY(-50%)',
    pointerEvents: 'none',
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
            left: '-10%',
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
            right: '-10%',
            width: isMobile ? '250px' : '400px',
            height: isMobile ? '250px' : '400px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(251,146,60,0.06) 0%, transparent 70%)',
            filter: 'blur(60px)',
          }}
        />
      </div>

      {/* Register Card */}
      <div
        style={{
          position: 'relative',
          zIndex: 1,
          width: '100%',
          maxWidth: isMobile ? '100%' : isTablet ? '460px' : '440px',
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
        <div style={{ textAlign: 'center', marginBottom: isMobile ? '24px' : '28px' }}>
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
            Create an account
          </h1>
          <p
            style={{
              fontSize: isMobile ? '12px' : '13px',
              color: '#64748b',
              fontWeight: 500,
            }}
          >
            Join QuickEats to order delicious meals
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

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Name Field */}
          <div style={{ marginBottom: '14px' }}>
            <label style={labelStyle}>Full Name</label>
            <div style={{ position: 'relative' }}>
              <UserIcon style={iconStyle} />
              <input
                id="register-name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="John Doe"
                required
                style={inputStyle}
                onFocus={handleFocus}
                onBlur={handleBlur}
              />
            </div>
          </div>

          {/* Email Field */}
          <div style={{ marginBottom: '14px' }}>
            <label style={labelStyle}>Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail style={iconStyle} />
              <input
                id="register-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                style={inputStyle}
                onFocus={handleFocus}
                onBlur={handleBlur}
              />
            </div>
          </div>

          {/* Password Field */}
          <div style={{ marginBottom: '14px' }}>
            <label style={labelStyle}>Password</label>
            <div style={{ position: 'relative' }}>
              <Lock style={iconStyle} />
              <input
                id="register-password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Min. 6 characters"
                required
                style={{ ...inputStyle, paddingRight: '48px' }}
                onFocus={handleFocus}
                onBlur={handleBlur}
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

          {/* Role Selector */}
          <div style={{ marginBottom: '24px' }}>
            <label style={labelStyle}>Account Role</label>
            <div style={{ position: 'relative' }}>
              <Shield style={iconStyle} />
              <select
                id="register-role"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                style={{
                  ...inputStyle,
                  appearance: 'none',
                  fontWeight: 500,
                  paddingRight: '40px',
                  cursor: 'pointer',
                }}
                onFocus={handleFocus}
                onBlur={handleBlur}
              >
                <option value="CUSTOMER">Customer (Order Food)</option>
                <option value="RESTAURANT_OWNER">Restaurant Owner (Manage Menu)</option>
              </select>
              {/* Dropdown arrow */}
              <svg
                style={{
                  position: 'absolute',
                  right: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  width: '16px',
                  height: '16px',
                  color: '#94a3b8',
                  pointerEvents: 'none',
                }}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </div>
          </div>

          {/* Submit Button */}
          <button
            id="register-submit"
            type="submit"
            disabled={submitting}
            style={{
              width: '100%',
              padding: isMobile ? '15px' : '14px',
              background: submitting
                ? '#d4d4d8'
                : 'linear-gradient(135deg, #ea580c 0%, #f97316 50%, #fb923c 100%)',
              color: 'white',
              fontWeight: 800,
              fontSize: isMobile ? '15px' : '14px',
              borderRadius: '14px',
              border: 'none',
              cursor: submitting ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              boxShadow: submitting ? 'none' : '0 8px 24px rgba(234,88,12,0.25), 0 2px 8px rgba(234,88,12,0.15)',
              transition: 'all 0.2s ease',
              opacity: submitting ? 0.6 : 1,
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
                  style={{ width: '18px', height: '18px', animation: 'spin 1s linear infinite' }}
                  viewBox="0 0 24 24"
                  fill="none"
                >
                  <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" strokeDasharray="60" strokeLinecap="round" opacity="0.3" />
                  <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
                </svg>
                <span>Creating account...</span>
              </>
            ) : (
              <>
                <span>Create Account</span>
                <ArrowRight style={{ width: '18px', height: '18px' }} />
              </>
            )}
          </button>
        </form>

        {/* Divider & Login Link */}
        <div
          style={{
            textAlign: 'center',
            paddingTop: '24px',
            marginTop: '24px',
            borderTop: '1px solid #f1f5f9',
          }}
        >
          <p style={{ fontSize: '13px', color: '#64748b' }}>
            Already have an account?{' '}
            <Link
              to="/login"
              style={{
                fontWeight: 700,
                color: '#ea580c',
                textDecoration: 'none',
                transition: 'color 0.2s',
              }}
              onMouseEnter={(e) => (e.target.style.textDecoration = 'underline')}
              onMouseLeave={(e) => (e.target.style.textDecoration = 'none')}
            >
              Sign in here
            </Link>
          </p>
        </div>
      </div>

      {/* Keyframe animations */}
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

export default RegisterPage;
