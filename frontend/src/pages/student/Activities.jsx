import React, { useEffect, useState } from "react";
import "./activities.css";
import { FaStar } from "react-icons/fa";
import { toast, Toaster } from "react-hot-toast";

const Activities = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Pagination states
  const [page, setPage] = useState(1);
  const itemsPerPage = 5; // You can adjust this (e.g. 5, 10 per page)

  useEffect(() => {
    fetch("http://localhost:8080/api/activities", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch activities");
        }
        console.log("Fetched activities:", response);
        return response.json();
      })
      .then((data) => {
        console.log("Fetched activities:", data);
        setActivities(data);
        setLoading(false);
      })
      .catch((error) => {
        setError(error.message);
        toast.error("Error fetching activities: " + error.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading activities...</p>;
  if (error) return <p>Error: {error}</p>;

  // Paginated activities
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
        Available Activities
      </h2>

      <div className="activities-list">
        {paginatedActivities.map((activity) => (
          <div key={activity.actID} className="activity-card">
            <div className="activity-details">
              <h3 style={{ textTransform: "uppercase" }}>{activity.name}</h3>
              <p>Date: {new Date(activity.date).toDateString()}</p>
              {activity.DID && <p>Department ID: {activity.DID}</p>}
              <p>Activity Type: {activity.type}</p>
              <p>Mandatory: {activity.mandatory ? "Yes" : "No"}</p>
            </div>
            <div className="activity-points">
              <span>{activity.points}</span>
              <FaStar className="star-icon" />
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
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
    </div>
  );
};

export default Activities;
