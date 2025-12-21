// src/components/ActivityModal/ActivityModal.jsx
import React from "react";
import "./activitymodal.css";

export default function ActivityModal({ open, onClose, activity }) {
  if (!open || !activity) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        {/* Header */}
        <div className={`modal-header status-${activity.status.toLowerCase()}`}>
          <h2>
            {activity.status === "Approved"
              ? "🎉 Approved"
              : activity.status === "Rejected"
              ? "❌ Rejected"
              : "⏳ Pending"}
          </h2>
          
        </div>

        {/* Info Section */}
        <div className="info-section">
        <h3 id="activityname">{activity.activityName}</h3>
        <p className="subtitle">{activity.description?activity.description:"description unavailable"}</p>
          
          <div className="info-tag">
            📅 Submitted {new Date(activity.date).toLocaleString()}
          </div>
          <div className="info-tag">
            🎯 Activity {new Date(activity.activityDate).toLocaleString()}
          </div>
        </div>

        

        {/* Timeline & Decision */}
        <div className="modal-grid">
          <div className="timeline-box">
            <h4>Timeline</h4>
            <ul>
              <li>
                <strong>📥 Request Submitted:</strong>{" "}
                {new Date(activity.date).toLocaleString()}
              </li>
              <li>
                <strong>
                  {activity.status === "Approved" ? "✅ Approved" : "❌ Decision"}
                </strong>{" "}
                {activity.status !== "Pending" && new Date(activity.decisionDate).toLocaleString()}
              </li>
              <li>
                <strong>📌 Activity Date:</strong>{" "}
                {new Date(activity.activityDate).toLocaleString()}
              </li>
            </ul>
          </div>
          <div className="decision-box">
            <h4>Comments</h4>
            <p
              className={
                activity.status === "Approved"
                  ? "approved-text"
                  : activity.status === "Rejected"
                  ? "rejected-text"
                  : "pending-text"
              }
            >
              
              {activity.status === "Rejected"
                ? activity.comments || "Rejected."
                : `${activity.status}.`}
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <button onClick={onClose} className="close-btn">
            Close
          </button>
          
        </div>
      </div>
    </div>
  );
}