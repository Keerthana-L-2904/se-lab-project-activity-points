import React, { createContext, useState, useEffect } from 'react';
import { useGoogleLogin } from '@react-oauth/google';
import axiosInstance, { setCSRFToken } from '../utils/axiosConfig';
import { useNavigate } from 'react-router-dom';

export const AuthContext = createContext();

const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authError, setauthError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = JSON.parse(localStorage.getItem('user'));
    if (storedUser) { 
      setUser(storedUser);
    }
    setLoading(false);
  }, []);

  const loginstudent = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      try {
        const response = await axiosInstance.post('/api/auth/login-student', {
          accessToken: tokenResponse.access_token, 
        });

        if (response.status === 200) {
          const studentDetails = response.data;
          studentDetails.role = "student";
          
          localStorage.setItem('user', JSON.stringify(studentDetails));
          
          const csrfToken = response.headers['x-csrf-token'];
          if (csrfToken) {
            setCSRFToken(csrfToken);
          }
          
          setUser(studentDetails);
          navigate('/student/dashboard');
        } else if (response.status === 429) {
          setauthError("Too many requests, try again after sometime");
        } else {
          setauthError(response.data);
        }
      } catch (error) {
        console.log('🔴 error.response:', error.response);
        
        const message =
          typeof error.response?.data === "string"
            ? error.response.data
            : error.response?.data?.message
            ? error.response.data.message
            : error.message ||
              "Invalid login. You are not authorized.";
        setauthError(message);
      }
    },
    onError: () => {
      setauthError("Google login failed. Please try again.");
    },
  });

  const loginfa = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      try {
        const response = await axiosInstance.post('/api/auth/login-fa', {
          accessToken: tokenResponse.access_token,
        });
        if (response.status === 200) {
          const faDetails = response.data;
          faDetails.role = "fa";
          
          localStorage.setItem('user', JSON.stringify(faDetails));
          
          const csrfToken = response.headers['x-csrf-token'];
          if (csrfToken) {
            setCSRFToken(csrfToken);
          }
          
          setUser(faDetails);
          navigate('/fa/dashboard');
        } else if (response.status === 429) {
          setauthError("Too many requests, try again after sometime");
        } else {
          setauthError(response.data);
        }
      } catch (error) {
        console.log('🔴 error.response:', error.response);
        const message =
          typeof error.response?.data === "string"
            ? error.response.data
            : error.response?.data?.message
            ? error.response.data.message
            : error.message ||
              "Invalid login. You are not authorized.";
        setauthError(message);
      }
    },
    onError: () => {
      setauthError("Google login failed. Please try again.");
    },
  });

  const logout = async () => {
    try {
      await axiosInstance.post('/api/auth/logout');
      
      localStorage.clear();
      setCSRFToken(null);
      setUser(null);
      navigate('/login');
    } catch (error) {
      localStorage.clear();
      setCSRFToken(null);
      setUser(null);
      navigate('/login');
    }
  };

  return (
    <AuthContext.Provider value={{ user, loginstudent, loginfa, logout, loading, authError, setauthError }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;