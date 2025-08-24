import React, { useEffect, useState } from "react";
import { FaStar } from "react-icons/fa";
import "./activities.css";
const ActivityHistory = () => {
    const [activities, setActivities] = useState([]);
    const [loading, setLoading] = useState(true);

    const studentID = JSON.parse(localStorage.getItem("user"))?.sid; // Ensure correct localStorage structure

    useEffect(() => {
        const fetchActivities = async () => {
            try {
                if (!studentID) {
                    console.warn("Student ID is missing.");
                    return;
                }

                console.log("Fetching activities for student ID:", studentID);

                const response = await fetch(`http://localhost:8080/api/student/${studentID}/activities`);
                if (!response.ok) throw new Error(`Failed to fetch activities: ${response.statusText}`);

                const data = await response.json();
                console.log("Fetched activity data:", data);

                setActivities(data);
            } catch (error) {
                console.error("Error fetching activities:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchActivities();
    }, [studentID]);

    if (loading) return <p>Loading activities...</p>;

    return (
        <div className="activities-container">
            <h2 style={{ textAlign: "center", fontSize: "32px", fontWeight: "bold", textTransform: "uppercase" }}>
                Activity History
            </h2>
            <div className="activities-list">
                {activities.length > 0 ? (
                    activities.map((activity, index) => (
                        <div key={index} className="activity-card">
                            <div className="activity-details">
                                <h3 style= {{textTransform: "uppercase"}}>{activity.title || "No title available"}</h3>
                                <p>Date: {activity.date}</p>
                                <p>Activity type: {activity.activityType || "No type available"}</p>
                            </div>
                            <div className="activity-points">
                                <span>{activity.points}</span>
                                <FaStar className="star-icon" />
                            </div>
                        </div>
                    ))
                ) : (
                    <p style={{ textAlign: "center", fontSize: "18px" }}>No activities available.</p>
                )}
            </div>
        </div>
    );
};

export default ActivityHistory;
