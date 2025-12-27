import React, { useState } from "react";
import axiosInstance from "../../utils/axiosConfig";
import { useNavigate } from "react-router-dom";
import ReCAPTCHA from "react-google-recaptcha";
import "./admin_R.css";
import { toast, Toaster } from "react-hot-toast";

const AdminRegister = () => {
  const [admin, setAdmin] = useState({ name: "", email: "", password: "" });
  const [requiresCaptcha, setRequiresCaptcha] = useState(false);
  const [captchaToken, setCaptchaToken] = useState(null);
  const navigate = useNavigate();

  const RECAPTCHA_SITE_KEY = import.meta.env.VITE_RECAPTCHA_SITE_KEY;

  const showWarning = (message) => {
    toast(message, {
      icon: "⚠️",
      style: {
        background: "#fff3cd",
        color: "#856404",
        border: "1px solid #ffc107",
      },
      duration: 4000,
    });
  };

  const handleChange = (e) => {
    setAdmin({ ...admin, [e.target.name]: e.target.value });
  };

  // ✅ Logout function
  const logoutAndRedirect = async () => {
    try {
      // Call backend logout endpoint to clear cookies
      await axiosInstance.post("/admin/logout");
      
      // Clear localStorage (if you're storing user info)
      localStorage.removeItem('user');
      
      // Clear CSRF token from cookie
      document.cookie = "csrfToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
      
      // Redirect to login page after a short delay
      setTimeout(() => {
        navigate("/admin/login");
      }, 1500);
      
    } catch (error) {
      setTimeout(() => {
        navigate("/admin/login");
      }, 1500);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();

    if (requiresCaptcha && !captchaToken) {
      toast.error("Please complete the CAPTCHA verification");
      return;
    }

    const passwordRegex = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{12,}$/;
    if (!passwordRegex.test(admin.password)) {
      toast.error("Password must be at least 12 characters long and include: 1 uppercase, 1 lowercase, 1 number, and 1 special character (@#$%!)");
      return;
    }

    try {
      let url = "/admin/register";
      if (captchaToken) {
        url += `?captchaToken=${encodeURIComponent(captchaToken)}`;
      }

      await axiosInstance.post(url, admin);

      toast.success("Admin registered successfully! Logging out...");
      setRequiresCaptcha(false);
      setCaptchaToken(null);
      
      // Reset form
      setAdmin({ name: "", email: "", password: "" });
      
      // ✅ Logout and redirect to login page
      await logoutAndRedirect();
      
    } catch (err) {   
      if (err.response) {
        const status = err.response.status;
        const data = err.response.data;

        if (status === 401) {
          toast.error("Please log in as an admin first to create new accounts");
          setTimeout(() => {
            navigate("/admin/login");
          }, 1500);
          return;
        }

        if (status === 403) {
          toast.error("Access denied. Only admins can create new accounts.");
          setTimeout(() => {
            navigate("/admin/dashboard");
          }, 1500);
          return;
        }

        if (status === 409) {
          toast.error("Email already registered");
          
          if (data.requiresCaptcha) {
            setRequiresCaptcha(true);
            showWarning("Multiple failed attempts. CAPTCHA required.");
          }
        } else if (status === 400) {
          if (data.requiresCaptcha) {
            toast.error("CAPTCHA verification failed");
            setCaptchaToken(null);
            setRequiresCaptcha(true);
          } else {
            toast.error(data.message || "Error while registering");
          }
        } else {
          toast.error("Error while registering");
        }
      } else {
        toast.error("Server unavailable");
      }
    }
  };

  const onCaptchaChange = (token) => {
    setCaptchaToken(token);
    toast.success("CAPTCHA verified!");
  };

  const onCaptchaExpired = () => {
    setCaptchaToken(null);
    showWarning("CAPTCHA expired. Please verify again.");
  };

  return (
    <div className="admin-register-container">
      <Toaster position="top-right" />
      
      {requiresCaptcha && (
        <div className="alert alert-warning" style={{
          background: "#fff3cd",
          border: "1px solid #ffc107",
          borderRadius: "8px",
          padding: "12px",
          marginBottom: "20px",
          color: "#856404"
        }}>
          <strong>🔒 Security Check Required</strong>
          <p style={{ margin: "5px 0 0 0", fontSize: "14px" }}>
            Multiple failed attempts detected. Please complete CAPTCHA verification.
          </p>
        </div>
      )}

      <h2>Create Admin Account</h2>
      
      <form className="admin-register-form" onSubmit={handleRegister}>
        <div className="form-in-content">
          <input
            type="text"
            name="name"
            placeholder="Name"
            value={admin.name}
            onChange={handleChange}
            required
            style={{ width: "90%" }}
          />
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={admin.email}
            onChange={handleChange}
            required
          />
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={admin.password}
            onChange={handleChange}
            required
          />
          <span
            style={{
              display: "block",
              fontSize: "12px",
              color: "#d1d5db",
              marginBottom: "14px",
              lineHeight: "1.4",
              textAlign: "left",
              maxWidth: "350px",
              padding: "2px"
            }}
          >
            Password must have more than <b>12 characters</b> and include at least one
            <b> uppercase letter</b>, <b>lowercase letter</b>, <b>number</b>, and
            <b> special character</b> (@#$%!).
            <br />
            <i>Example: StrongPassword@1</i>
          </span>

          {requiresCaptcha && (
            <div className="captcha-container" style={{ 
              marginBottom: "20px",
              display: "flex",
              justifyContent: "center"
            }}>
              <ReCAPTCHA
                sitekey={RECAPTCHA_SITE_KEY}
                onChange={onCaptchaChange}
                onExpired={onCaptchaExpired}
                theme="light"
              />
            </div>
          )}

          <button type="submit" disabled={requiresCaptcha && !captchaToken}>
            {requiresCaptcha && !captchaToken ? "Complete CAPTCHA" : "Register"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default AdminRegister;