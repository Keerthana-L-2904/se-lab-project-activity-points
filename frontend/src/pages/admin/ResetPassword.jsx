import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axiosInstance from "../../utils/axiosConfig";
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
    const passwordRegex = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{13,}$/;
    if (!passwordRegex.test(newPassword)) {
              toast.error("Password must have more than 12 characters long and include: 1 uppercase, 1 lowercase, 1 number, and 1 special character (@#$%!)");
              return; // Stop the function from proceeding to the API call
          }

    setLoading(true);
    try {
      const res = await axiosInstance.post(
        "/admin/reset-password",
        { token, newPassword }
      );
      toast.success(res.data);
      setTimeout(() => navigate("/admin/login"), 2000);
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
          pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{12,}"
          onChange={(e) => setNewPassword(e.target.value)}
          className="reset-input"
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
              padding:"2px"
            }}
          >
          Password must have more than <b>12 characters</b> and include at least one
          <b> uppercase letter</b>, <b>lowercase letter</b>, <b>number</b>, and
          <b> special character</b> (@#$%!).
          <br />
          <i>Example: StrongPassword@1</i>
        </span>
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
