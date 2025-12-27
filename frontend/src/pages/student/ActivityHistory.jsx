import React, { useEffect, useState } from "react";
import { FaStar } from "react-icons/fa";
import "./activities.css";
import { toast, Toaster } from "react-hot-toast";
import axiosInstance from "../../utils/axiosConfig";

const ActivityHistory = () => {
    const [activities, setActivities] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    
    const itemsPerPage = 5;

    const studentID = JSON.parse(localStorage.getItem("user"))?.sid;

    useEffect(() => {
        const fetchActivities = async () => {
            try {
                if (!studentID) {
                    setLoading(false);
                    return;
                }

                // ✅ FIXED: Axios doesn't use .ok or .json()
                const response = await axiosInstance.get("/student/activity");

                // ✅ FIXED: Access response.data directly (axios parses JSON automatically)
                if (!response.data) {
                    setActivities([]);
                    setLoading(false);
                    return;
                }

                // ✅ FIXED: Handle array response properly
                const activityArray = Array.isArray(response.data) 
                    ? response.data 
                    : [response.data];

                setActivities(
                    activityArray.map(a => ({
                        ...a,
                        ...a.activity  // merge nested fields into the main object
                    }))
                );
            } catch (error) {
                toast.error("Error fetching activities: " + (error.response?.data?.message || error.message));
            } finally {
                setLoading(false);
            }
        };

        fetchActivities();
    }, [studentID]);

    if (loading) return <p>Loading activities...</p>;

    // Filter out pending activities before pagination
    const filteredActivities = activities.filter(activity => activity.status !== 'pending');

    // Paginated data
    const paginatedActivities = filteredActivities.slice(
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
                    paginatedActivities.map((activity, index) => (
                        <div key={index} className="activity-card">
                            <div className="activity-details">
                                <h3 style={{ textTransform: "uppercase" }}>
                                    {activity.title || activity.name || "No title available"}
                                </h3>
                                <p>Date: {activity.date}</p>
                                <p>
                                    Activity type: {activity.activityType || activity.type || "No type available"}
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
            {filteredActivities.length > itemsPerPage && (
                <div className="pagination" style={{ display: "flex", justifyContent: "center", marginTop: "20px"}}>
                    {Array.from(
                        { length: Math.ceil(filteredActivities.length / itemsPerPage) },
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