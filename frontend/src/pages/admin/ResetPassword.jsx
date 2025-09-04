import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { toast, Toaster } from "react-hot-toast";
import "./forget.css"; // new css file

const ResetPassword = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // extract token from query params
  const token = new URLSearchParams(location.search).get("token");

  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (!newPassword) {
      toast.error("Password cannot be empty");
      return;
    }

    setLoading(true);
    try {
      const res = await axios.post(
        "http://localhost:8080/admin/reset-password",
        { token, newPassword }
      );
      toast.success(res.data);
      setTimeout(() => navigate("/admin-login"), 2000);
    } catch (err) {
      toast.error(err.response?.data || "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="reset-container">
      <Toaster />
      <form onSubmit={handleResetPassword} className="reset-form">
        <h2 className="reset-title">Reset Password</h2>
        <input
          type="password"
          placeholder="Enter new password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          className="reset-input"
        />
        <button
          type="submit"
          disabled={loading}
          className="reset-button"
        >
          {loading ? "Resetting..." : "Reset Password"}
        </button>
      </form>
    </div>
  );
};

export default ResetPassword;
