import React, { useState } from "react";
import axiosInstance from "../../utils/axiosConfig";
import { toast, Toaster } from "react-hot-toast";
import "./forget.css"; // new css file

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    if (!email) {
      toast.error("Please enter your email");
      return;
    }

    setLoading(true);
    try {
      const res = await axiosInstance.post(
        "http://localhost:8080/admin/forgot-password",
        null,
        { params: { email } }
      );
      toast.success(res.data);
    } catch (err) {
      toast.error(err.response?.data || "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="forgot-container">
      <Toaster />
      <form onSubmit={handleForgotPassword} className="forgot-form">
        <h2 className="forgot-title">Forgot Password</h2>
        <input
          type="email"
          placeholder="Enter your registered email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="forgot-input"
        />
        <button
          type="submit"
          disabled={loading}
          className="forgot-button"
        >
          {loading ? "Sending..." : "Send Reset Link"}
        </button>
      </form>
    </div>
  );
};

export default ForgotPassword;
