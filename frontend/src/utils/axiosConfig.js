// src/utils/axiosConfig.js
import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

// ✅ Helper function to get cookie value by name
const getCookie = (name) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  return null;
};

// ✅ Helper function to set cookie
const setCookie = (name, value, days = 7) => {
  const expires = new Date();
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Strict`;
};

// ✅ Helper function to delete cookie
const deleteCookie = (name) => {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
};

// ✅ Store CSRF token in cookie
export const setCSRFToken = (token) => {
  if (token) {
    setCookie('csrfToken', token, 7);
  } else {
    deleteCookie('csrfToken');
  }
};

// ✅ Get CSRF token from cookie
export const getCSRFToken = () => {
  return getCookie('csrfToken');
};

// ✅ Helper function to determine correct refresh endpoint
const getRefreshEndpoint = () => {
  try {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const user = JSON.parse(storedUser);
      if (user?.role === 'admin') {
        return 'http://localhost:8080/admin/refresh';
      }
    }
  } catch (e) {
    console.error('Axios Error parsing user from localStorage:', e);
  }
  return 'http://localhost:8080/api/auth/refresh';
};

// ✅ NEW: Check if endpoint should skip token refresh
const shouldSkipTokenRefresh = (url) => {
  if (!url) return false;
  
  const skipEndpoints = [
    '/api/auth/login-student',
    '/api/auth/login-fa',
    '/api/auth/refresh',
    '/api/auth/logout',
    '/admin/login',
    '/admin/refresh'
  ];
  
  return skipEndpoints.some(endpoint => url.includes(endpoint));
};

// ✅ Request Interceptor - FIXED to handle multipart
axiosInstance.interceptors.request.use((config) => {
  // ✅ Only set Content-Type to JSON if not already set (allows multipart to work)
  if (!config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json';
  }
  
  // ✅ Add CSRF token to non-GET requests
  const csrfToken = getCSRFToken();
  if (csrfToken && config.method !== 'get') {
    config.headers['X-CSRF-Token'] = csrfToken;
  }
  
  return config;
}, (error) => {
  return Promise.reject(error);
});

// ✅ Response Interceptor with Queue for Concurrent Requests
axiosInstance.interceptors.response.use(
  (response) => {
    const newCsrfToken = response.headers['x-csrf-token'];
    const currentCsrfToken = getCSRFToken();
    if (newCsrfToken && newCsrfToken !== currentCsrfToken) {
      setCSRFToken(newCsrfToken);
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    // ✅ CRITICAL FIX: Skip token refresh for login/logout endpoints
    if (shouldSkipTokenRefresh(originalRequest?.url)) {
      return Promise.reject(error);
    }

    // Handle 401 - Token expired
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // ✅ Queue requests while refreshing
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => axiosInstance(originalRequest));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      // ✅ Get user info BEFORE attempting refresh
      const storedUser = localStorage.getItem('user');
      let userRole = null;
      try {
        const parsedUser = storedUser ? JSON.parse(storedUser) : null;
        userRole = parsedUser?.role;
      } catch (e) {
        console.error('[Axios] Error parsing user:', e);
      }

      try {
        const refreshEndpoint = getRefreshEndpoint();
        const refreshResponse = await axios.post(
          refreshEndpoint,
          {},
          { withCredentials: true }
        );

        const newCsrfToken = refreshResponse.headers['x-csrf-token'];
        if (newCsrfToken) {
          setCSRFToken(newCsrfToken);
        }

        processQueue(null);
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError);
        
        // Clean up
        localStorage.removeItem('user');
        setCSRFToken(null);

        // Redirect based on user role
        if (userRole === 'admin') {
          window.location.href = '/admin/login';
        } else {
          window.location.href = '/login';
        }
        
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Handle 403 - CSRF failed
    if (error.response?.status === 403 && !originalRequest._csrfRetry) {
      originalRequest._csrfRetry = true;

      try {
        const refreshEndpoint = getRefreshEndpoint();
        const refreshResponse = await axios.post(
          refreshEndpoint,
          {},
          { withCredentials: true }
        );

        const newCsrfToken = refreshResponse.headers['x-csrf-token'];
        if (newCsrfToken) {
          setCSRFToken(newCsrfToken);
          return axiosInstance(originalRequest);
        } else {
          console.error('[Axios] No CSRF token in refresh response');
        }
      } catch (refreshError) {
        console.error('[Axios] Failed to refresh CSRF token:', refreshError);
        
        // If refresh fails, clean up and redirect
        const storedUser = localStorage.getItem('user');
        let userRole = null;
        try {
          const parsedUser = storedUser ? JSON.parse(storedUser) : null;
          userRole = parsedUser?.role;
        } catch (e) {
          console.error('[Axios] Error parsing user:', e);
        }

        localStorage.removeItem('user');
        setCSRFToken(null);

        if (userRole === 'admin') {
          window.location.href = '/admin/login';
        } else {
          window.location.href = '/login';
        }
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;