import { Navigate } from "react-router-dom";

const RoleProtectedRoute = ({ role, children }) => {
  const user = JSON.parse(localStorage.getItem("user"));
  const admin = JSON.parse(localStorage.getItem("admin"));

  // Admin should NEVER access student/fa routes
  if (admin) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Not logged in as user
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Logged in but wrong role
  if (user.role !== role) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Correct role
  return children;
};

export default RoleProtectedRoute;
