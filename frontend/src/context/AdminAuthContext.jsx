import React, { createContext, useState, useEffect, useCallback } from 'react';
import axiosInstance from '../utils/axiosConfig';
import { useNavigate } from 'react-router-dom';

export const AdminAuthContext = createContext();

export const AdminAuthProvider = ({ children }) => {
  const [admin, setAdmin] = useState(null);
  const [csrfToken, setCsrfToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const log = useCallback((message) => {
    //console.log(`[AdminAuthProvider] ${message}`);
  }, []);

  // ✅ Restore session on page refresh
  useEffect(() => {
    const restoreSession = async () => {
      try {
        const storedAdmin = localStorage.getItem('admin');
        if (storedAdmin) {
          setAdmin(JSON.parse(storedAdmin));
          
          log('Attempting to refresh token on page load...');
          const response = await axiosInstance.post('/admin/refresh', {}, {
            withCredentials: true
          });
          
          const newCsrfToken = response.headers['x-csrf-token'];
          if (newCsrfToken) {
            setCsrfToken(newCsrfToken);
            log('✅ CSRF token updated from refresh endpoint');
          }
        }
      } catch (error) {
        localStorage.removeItem('admin');
        setCsrfToken(null);
        setAdmin(null);
      } finally {
        setLoading(false);
      }
    };

    restoreSession();
  }, [log]);

  // ✅ UPDATED: Accept captchaToken as optional parameter
  const login = async (email, password, captchaToken = null) => {
    // Build URL with captcha if provided
    let url = '/admin/login';
    if (captchaToken) {
      url += `?captchaToken=${encodeURIComponent(captchaToken)}`;
    }

    const response = await axiosInstance.post(url, { email, password }, {
      withCredentials: true
    });

    // Extract CSRF token from response headers
    const newCsrfToken = response.headers['x-csrf-token'];
    if (newCsrfToken) {
      setCsrfToken(newCsrfToken);
      log('✅ CSRF token extracted from login response header');
    }

    // Store admin data
    localStorage.setItem('admin', JSON.stringify(response.data.admin));
    setAdmin(response.data.admin);
    return response.data;
  };

  const logout = async () => {
    try {
      await axiosInstance.post('/admin/logout', {}, {
        withCredentials: true,
        headers: csrfToken ? { 'X-CSRF-Token': csrfToken } : {}
      });
      log('✅ Logout successful');
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      localStorage.clear();
      setCsrfToken(null);
      setAdmin(null);
      navigate('/admin/login');
    }
  };

  // ✅ Auto-refresh every 5 minutes
  useEffect(() => {
    if (!admin) return;

    const refreshInterval = setInterval(async () => {
      try {
        log('🔄 Refreshing access token...');
        const response = await axiosInstance.post('/admin/refresh', {}, {
          withCredentials: true
        });

        const newCsrfToken = response.headers['x-csrf-token'];
        if (newCsrfToken) {
          setCsrfToken(newCsrfToken);
          log('✅ Access token refreshed, CSRF token updated');
        }
      } catch (error) {
        logout();
      }
    }, 5 * 60 * 1000);

    return () => clearInterval(refreshInterval);
  }, [admin, log]);

  return (
    <AdminAuthContext.Provider 
      value={{ admin, csrfToken, login, logout, loading }}
    >
      {children}
    </AdminAuthContext.Provider>
  );
};

export default AdminAuthProvider;
