import React, { useEffect, useState } from "react";
import "./activities.css";
import { FaStar } from "react-icons/fa";

const Activities = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const getHeaders = {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    }
  }
  useEffect(() => {
    fetch("http://localhost:8080/api/activities", {
      method: 'GET', headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    }) // Ensure backend is running
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch activities");
        }
        return response.json();
      })
      .then((data) => {
        setActivities(data);
        setLoading(false);
      })
      .catch((error) => {
        setError(error.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading activities...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
    <div className="activities-container">
      <h2 style={{ textAlign: "center", fontSize: "32px", fontWeight: "bold", textTransform: "uppercase" }}>
        Available Activities
      </h2>
      <div className="activities-list">
        {activities.map((activity) => (
          <div key={activity.actID} className="activity-card">
            <div className="activity-details">
              <h3 style={{textTransform:"uppercase"}}>{activity.name}</h3>
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
    </div>
  );
};
<svg>
  <rect className="my-class" fillRule="evenodd" stopColor="red" />
</svg>


export default Activities;
