// src/components/AdminProtectedRoute.js
import React, { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AdminAuthContext } from "../../context/AdminAuthContext";

const AdminProtectedRoute = ({ children }) => {
  const { admin } = useContext(AdminAuthContext);

  if (!admin) {
    return <Navigate to="/admin/login" replace />;
  }

  return children;
};

export default AdminProtectedRoute;
