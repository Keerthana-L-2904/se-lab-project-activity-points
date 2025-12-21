import React, { useEffect, useState } from "react";
import "./dashboard.css";
import { Link } from "react-router-dom";

const StudentDashboard = () => {
    const [student, setStudent] = useState(null);
    const [latestActivity, setLatestActivity] = useState(null);
    const [announcements, setAnnouncements] = useState([]);

    const fetchStudentData = async () => {
        try {
            const storedUser = localStorage.getItem("user");
            const token = localStorage.getItem("token");

            if (!storedUser) return console.error("No user in localStorage.");

            const user = JSON.parse(storedUser);

            // Fetch student
            const studentResponse = await fetch(`http://localhost:8080/api/student/${user.sid}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            const studentData = await studentResponse.json();
            setStudent(studentData);

            // Fetch latest activity (SAFE)
            const activityResponse = await fetch(
                `http://localhost:8080/api/student/${user.sid}/latest-activity`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            let activityData = null;
            try {
                if (activityResponse.ok) {
                    activityData = await activityResponse.json();
                } else {
                    console.log("No latest activity available.");
                }
            } catch (e) {
                console.log("Error parsing activity JSON");
            }
            setLatestActivity(activityData);

            // Fetch announcements ALWAYS
            const announcementResponse = await fetch(
                `http://localhost:8080/api/student/${user.sid}/announcements`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            let announcementData = [];
            try {
                if (announcementResponse.ok) {
                    announcementData = await announcementResponse.json();
                } else {
                    console.log("Announcements route returned no data");
                }
            } catch (e) {
                console.log("Error parsing announcements JSON");
            }

            setAnnouncements(announcementData.slice(-2));

        } catch (error) {
            console.error("Error fetching student data:", error);
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
                    <p>Roll-number: {student.sid} |FA-Name: {student?.faName} | FA-Mail: {student?.faEmail}</p>
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
                    <h2>{(student?.deptPoints || 0) + (student?.institutePoints || 0)+(student?.otherPoints || 0)}</h2>
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
                            <td style={{textTransform:"uppercase"}}>{latestActivity?.title || "No title available"}</td>
                            <td>{latestActivity?.activityType || "No type available"}</td>
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
