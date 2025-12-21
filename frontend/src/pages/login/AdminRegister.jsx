import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "./admin_R.css"; // ✅ Import the CSS
import { toast, Toaster } from "react-hot-toast"; 

const AdminRegister = () => {
  const [admin, setAdmin] = useState({ name: "", email: "", password: "" });
  const navigate = useNavigate();

  const handleChange = (e) => {
    setAdmin({ ...admin, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
      if (!token) {
      toast.error("Please log in as an admin first ❌");
      return;
    }
    try {
      await axios.post("http://localhost:8080/admin/register", admin, {
      headers: {
        Authorization: `Bearer ${token}`, // ✅ send token
      },
    });
      toast.success("Admin registered successfully!");
      navigate("/admin/login");
    } catch (err) {
      console.error("Registration failed", err);
      toast.error("Error while registering");
    }
  };

  return (
    <div className="admin-register-container"><Toaster />
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
          style={{ width: "90%"}}
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
        <button type="submit">Register</button>
        </div>
      </form>
    </div>
  );
};

export default AdminRegister;
