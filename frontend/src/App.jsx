import { Routes, Route, useNavigate,Navigate, useLocation } from "react-router-dom";
import { useContext,useRef,useEffect,useState } from "react";
import { AuthContext } from "./context/AuthContext"; 
import AdminProtectedRoute from "./components/Login/AdminProtectedRoute";
import AdminLogin from "./pages/login/AdminLogin";
import AdminRegister from "./pages/login/AdminRegister";
import './utils/axiosConfig'; 
import AdminLayout from "./layouts/AdminLayout";
import StudentLayout from "./layouts/StudentLayout";
import FaLayout from "./layouts/FaLayout"

import AdminDashboard from "./pages/admin/AdminDashboard";
import ManageUsers from "./pages/admin/ManageUsers";
import ManageActivities from "./pages/admin/ManageActivities";
import ForgotPassword from "./pages/admin/ForgotPassword";
import ResetPassword from "./pages/admin/ResetPassword";

import StudentDashboard from "./pages/student/StudentDashboard";
import ActivityHistory from "./pages/student/ActivityHistory";
import Activities from "./pages/student/Activities";
import Announcements from "./pages/student/Announcements";
import RequestForm from "./pages/student/RequestForm";
import Tracking from "./pages/student/Tracking";
import AnnouncementDetail from "./pages/student/AnnouncementDetail";
import LoginPage from "./pages/login/Login";

import FaDashboard from "./pages/fa/FaDashboard"
import Approvals from "./pages/fa/Approvals";
import StudentDetails from "./pages/fa/StudentDetails"
import StudentList from "./pages/fa/StudentList";
import Announcements_fa from "./pages/fa/Announcements";
import AnnouncementDetail_fa from "./pages/fa/AnnouncementDetail";
import Unauthorized from "./pages/Unauthorized/Unauthorized";
import NotFound from "./pages/NotFound/NotFound"

import RoleProtectedRoute from "./routes/RoleProtectedRoute";

  
  
function App() {
    return (
            <Routes>
                <Route path="/admin/login" element={<AdminLogin />} />
                <Route path="/admin/register" element={<AdminRegister />} />
                <Route path="/admin/forgot-password" element={<ForgotPassword />} />
                <Route path="/reset-password" element={<ResetPassword />} />
                {/* Admin Routes */}
		<Route path="/" element={<Navigate to="/login" replace />} />
		<Route path="/unauthorized" element={<Unauthorized />} />
		<Route path="*" element={<NotFound />} />
                <Route
                path="/admin/*"
                element={
                    <AdminProtectedRoute> {/*  use this instead */}
                    <AdminLayout />
                    </AdminProtectedRoute>
                }
                >
                <Route index element={<AdminDashboard />} />
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="users" element={<ManageUsers />} />
                <Route path="activities" element={<ManageActivities />} />
                </Route>

                {/* Student Routes */}
                <Route
                    path="/student/*"
                    element={
                        <RoleProtectedRoute role="student">
                            <StudentLayout />
                        </RoleProtectedRoute>
                    }
                >
                    <Route index element={<StudentDashboard />} />
                    <Route path="dashboard" element={<StudentDashboard />} />
                    <Route path="tracking" element={<Tracking />} />
                    <Route path="activity-history" element={<ActivityHistory />} />
                    <Route path="request-form" element={<RequestForm />} />
                    <Route path="activities" element={<Activities />} />
                    <Route path="announcements" element={<Announcements />} />
                    <Route path="announcements/:id" element={<AnnouncementDetail />} />
                </Route>
                
                <Route
                    path="/fa/*"
                    element={
                        <RoleProtectedRoute role="fa">
                            <FaLayout />
                        </RoleProtectedRoute>
                    }
                >
                    <Route index element={<FaDashboard />} />
                    <Route path="dashboard" element={<FaDashboard />} />
                    <Route path="approvals" element={<Approvals />} />
                    <Route path="new-announcement" element={<Announcements />} />
                    <Route path="request-form" element={<RequestForm />} />
                    <Route path="announcements" element={<Announcements_fa />} />
                    <Route path="announcements/:id" element={<AnnouncementDetail_fa />} />
                    <Route path="student-list" element={<StudentList />} />
                    <Route path="student-details/:sid" element={<StudentDetails />} />
                </Route>

                {/* Login Route */}
                <Route path="/login" element={<LoginPage />} />
            </Routes>
        // </Router>
    );
}

export default App;
