import React, { useEffect, useState } from "react";
import "./dashboard.css";
import { Link } from "react-router-dom";
import axiosInstance from "../../utils/axiosConfig";

const StudentDashboard = () => {
    const [student, setStudent] = useState(null);
    const [latestActivity, setLatestActivity] = useState(null);
    const [announcements, setAnnouncements] = useState([]);

    const fetchStudentData = async () => {
        try {
            const storedUser = localStorage.getItem("user");

            if (!storedUser) {
                return;
            }

            const user = JSON.parse(storedUser);

            // ✅ Fetch student data
            const studentResponse = await axiosInstance.get("/api/student");
            setStudent(studentResponse.data);

            // ✅ FIXED: Fetch latest activity using axios (not fetch)
            try {
                const activityResponse = await axiosInstance.get("/api/student/latest-activity");
                
                if (activityResponse.data) {
                    setLatestActivity(activityResponse.data);
                } else {
                    console.log("No latest activity available.");
                }
            } catch (activityError) {
                console.log("No latest activity available:", activityError.message);
                setLatestActivity(null);
            }

            // ✅ FIXED: Fetch announcements using axios (not fetch)
            try {
                const announcementResponse = await axiosInstance.get("/api/student/announcements");
                
                if (announcementResponse.data && Array.isArray(announcementResponse.data)) {
                    setAnnouncements(announcementResponse.data.slice(-2));
                } else {
                    setAnnouncements([]);
                }
            } catch (announcementError) {
                setAnnouncements([]);
            }

        } catch (error) {
            console.error("Error fetching student data:", error.response);
        }
    };

    useEffect(() => {
        fetchStudentData();
    }, []);

    return (
        <div className="dashboard-container">
            <h1 className="dashboard-title">STUDENT DASHBOARD</h1>

            {student && (
                <div className="student-info">
                    <h3>Welcome back, {student.name}!</h3>
                    <p>Roll-number: {student.sid} | FA-Name: {student?.faName} | FA-Mail: {student?.faEmail}</p>
                </div>
            )}
           {/* Display Points */}
            <div className="points-section">
                <div className="progress-box">
                    <h2>{student?.deptPoints || 0}</h2>
                    <p>Department Points</p>
                </div>
                <div className="progress-box">
                    <h2>{student?.institutePoints || 0}</h2>
                    <p>Institutional Points</p>
                </div>
                <div className="progress-box">
                    <h2>{student?.otherPoints || 0}</h2>
                    <p>Other Points</p>
                </div>
                <div className="progress-box">
                    <h2>{(student?.deptPoints || 0) + (student?.institutePoints || 0) + (student?.otherPoints || 0)}</h2>
                    <p>Total Activity Points</p>
                </div>
            </div>

            {/* Latest Activity Section */}
            <div className="activity-header">
                <h2 className="activity-title">Activity History</h2>
                <Link to="/student/activity-history" className="see-all-btnn">See All</Link>
            </div>
            <table className="activity-table">
                <thead>
                    <tr>
                        <th>Activity Name</th>
                        <th>Institute or Departmental</th>
                        <th>Activity Points</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    {latestActivity ? (
                        <tr>
                            <td style={{textTransform:"uppercase"}}>{latestActivity?.title || latestActivity?.name || "No title available"}</td>
                            <td>{latestActivity?.activityType || latestActivity?.type || "No type available"}</td>
                            <td>{latestActivity?.points ?? "0"}</td>
                            <td>{latestActivity?.date || "No date available"}</td>
                        </tr>
                    ) : (
                        <tr>
                            <td colSpan="4">No recent activity available</td>
                        </tr>
                    )}
                </tbody>
            </table>

            {/* Announcements Section */}
            <div className="announcement-header">
                <h2 className="announcement-title">Announcements</h2>
                <Link to={`/student/announcements`} className="see-all-btnn">See All</Link>
            </div>
            <table className="announcement-table">
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Body</th>
                        <th>Date</th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
                    {announcements.length > 0 ? (
                        announcements.map((announcement, index) => (
                            <tr key={index}>
                                <td>{announcement.title}</td>
                                <td>{announcement.body}</td>
                                <td>{new Date(announcement.date).toLocaleDateString()}</td>
                                <td>{announcement.time}</td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="4">No announcements available.</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default StudentDashboard;