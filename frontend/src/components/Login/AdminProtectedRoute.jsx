// src/components/AdminProtectedRoute.js
import React, { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AdminAuthContext } from "../../context/AdminAuthContext";

const AdminProtectedRoute = ({ children }) => {
 const admin = JSON.parse(localStorage.getItem("admin"));
  const user = JSON.parse(localStorage.getItem("user"));

  // Logged in as student/fa ? unauthorized
  if (user && !admin) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Not logged in at all
  if (!admin) {
    return <Navigate to="/admin/login" replace />;
  }

 
  return children;
};

export default AdminProtectedRoute;
