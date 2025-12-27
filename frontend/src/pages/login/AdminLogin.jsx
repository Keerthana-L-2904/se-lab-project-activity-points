import React, { useState, useContext } from "react";
import { useNavigate, Link } from "react-router-dom";
import { AdminAuthContext } from "../../context/AdminAuthContext";
import ReCAPTCHA from "react-google-recaptcha";
import "./admin_L.css"; 
import { toast, Toaster } from "react-hot-toast"; 

const AdminLogin = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isRateLimited, setIsRateLimited] = useState(false);
  const [requiresCaptcha, setRequiresCaptcha] = useState(false);
  const [captchaToken, setCaptchaToken] = useState(null);
  const [isLocked, setIsLocked] = useState(false);
  const [lockoutMinutes, setLockoutMinutes] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useContext(AdminAuthContext);

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

  const handleLogin = async (e) => {
    e.preventDefault();
    
    if (isRateLimited) {
      toast.error("Please wait before trying again.");
      return;
    }

    if (requiresCaptcha && !captchaToken) {
      toast.error("Please complete the CAPTCHA verification");
      return;
    }

    const passwordRegex = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{12,}$/;
    if (!passwordRegex.test(password)) {
      toast.error("Password must be at least 12 characters long and include: 1 uppercase, 1 lowercase, 1 number, and 1 special character (@#$%!)");
      return;
    }

    setIsLoading(true);

    try {
      // ✅ Just call login from context - it handles everything!
      await login(email, password, captchaToken);
      
      // Reset states on success
      setRequiresCaptcha(false);
      setCaptchaToken(null);
      setIsLocked(false);
      
      toast.success("Login successful!");
      navigate("/admin/dashboard");

    } catch (err) {
      if (err.response) {
        const status = err.response.status;
        const data = err.response.data;
        const message = data?.message || "An error occurred";

        if (status === 423) {
          setIsLocked(true);
          setLockoutMinutes(data.remainingMinutes || 15);
          toast.error(`Account locked. Try again in ${data.remainingMinutes} minutes.`);
        } else if (status === 429) {
          toast.error(message);
          setIsRateLimited(true);
          setTimeout(() => setIsRateLimited(false), 60000);
        } else if (status === 401) {
          toast.error("Invalid email or password");
          if (data.requiresCaptcha) {
            setRequiresCaptcha(true);
          }
        } else if (status === 400 && data.requiresCaptcha) {
          toast.error("CAPTCHA verification failed. Please try again.");
          setCaptchaToken(null);
          setRequiresCaptcha(true);
        } else {
          toast.error("Login failed");
        }
      } else {
        toast.error("Server unavailable");
      }
    } finally {
      setIsLoading(false);
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
    <div className="admin-login-container">
      <Toaster position="top-right" />
      
      {isLocked && (
        <div className="alert alert-danger">
          <strong>⚠️ Account Locked</strong>
          <p>Too many failed attempts. Please try again in {lockoutMinutes} minutes.</p>
        </div>
      )}

      {requiresCaptcha && (
        <div className="alert alert-warning">
          <strong>🔒 Security Check Required</strong>
          <p>Multiple failed attempts detected. Please complete CAPTCHA verification.</p>
        </div>
      )}

      <h2>Admin Login</h2>
      
      <form className="admin-login-form" onSubmit={handleLogin}>
        <div className="form-inner-content">
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={isLocked || isLoading}
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={isLocked || isLoading}
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

          <button 
            type="submit" 
            disabled={isRateLimited || isLocked || isLoading || (requiresCaptcha && !captchaToken)}
          >
            {isLoading ? "Logging in..." : isRateLimited ? "Please wait..." : isLocked ? "Account Locked" : "Login"}
          </button>
          
          <p>
            <Link to="/admin/forgot-password">Forgot Password?</Link>
          </p>
        </div>
      </form>
    </div>
  );
};

export default AdminLogin;
