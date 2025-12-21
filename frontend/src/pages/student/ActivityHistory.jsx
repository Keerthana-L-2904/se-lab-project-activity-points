import React, { useEffect, useState } from "react";
import { FaStar } from "react-icons/fa";
import "./activities.css";
import { toast, Toaster } from "react-hot-toast";

const ActivityHistory = () => {
    const [activities, setActivities] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const token = localStorage.getItem("token");
    const itemsPerPage = 5; // you can change this to show more/less per page

    const studentID = JSON.parse(localStorage.getItem("user"))?.sid;

    useEffect(() => {
        const fetchActivities = async () => {
            try {
                if (!studentID) {
                    console.warn("Student ID is missing.");
                    return;
                }

                console.log("Fetching activities for student ID:", studentID);

                const response = await fetch(
                `http://localhost:8080/student/activity/${studentID}`,
                { headers: { Authorization: `Bearer ${token}` } }
                );

                if (!response.ok)
                    throw new Error(`Failed to fetch activities: ${response.statusText}`);

                const data = await response.json();
                console.log("Fetched activity data:", data);

                setActivities(
                data.map(a => ({
                   ...a,
                  ...a.activity  // merge nested fields into the main object
  }))
                    );
            } catch (error) {
                toast.error("Error fetching activities: " + error.message);
                console.error("Error fetching activities:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchActivities();
    }, [studentID]);

    if (loading) return <p>Loading activities...</p>;

    // Paginated data
    const paginatedActivities = activities.slice(
        (page - 1) * itemsPerPage,
        page * itemsPerPage
    );
    return (
        <div className="activities-container">
            <Toaster />
            <h2
                style={{
                    textAlign: "center",
                    fontSize: "32px",
                    fontWeight: "bold",
                    textTransform: "uppercase",
                }}
            >
                Activity History
            </h2>
                <div className="activities-list">
                    {paginatedActivities.length === 0 ? (
                            <p style={{ textAlign: "center", fontSize: "18px" }}>
                                No activities available.
                            </p>
                    ) : (
                    paginatedActivities
                        .filter(activity => activity.status !== 'pending')
                        .map((activity, index) => (
                            <div key={index} className="activity-card">
                                <div className="activity-details">
                                    <h3 style={{ textTransform: "uppercase" }}>
                                        {activity.title || "No title available"}
                                    </h3>
                                    <p>Date: {activity.date}</p>
                                    <p>
                                        Activity type: {activity.activityType || "No type available"}
                                    </p>
                                </div>
                                <div className="activity-points">
                                    <span>{activity.points}</span>
                                    <FaStar className="star-icon" />
                                </div>
                            </div>
                        ))
                )}
            </div>
            {/* Pagination */}
            {activities.length > itemsPerPage && (
                <div className="pagination" style={{ display: "flex", justifyContent: "center", marginTop: "20px"}}>
                    {Array.from(
                        { length: Math.ceil(activities.length / itemsPerPage) },
                        (_, i) => (
                            <button
                                key={i}
                                className={page === i + 1 ? "active" : ""}
                                onClick={() => setPage(i + 1)}
                            >
                                {i + 1}
                            </button>
                        )
                    )}
                </div>
            )}
        </div>
    );
};

export default ActivityHistory;
