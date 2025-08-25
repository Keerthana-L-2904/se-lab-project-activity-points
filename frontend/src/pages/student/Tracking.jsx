import React, { useEffect, useState, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import "./student.css";
import ActivityModal from "../../components/ActivityModal/ActivityModal";

const Tracking = () => {
  const { user } = useContext(AuthContext); // Get user details from AuthContext
  const [trackingRequests, setTrackingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [status, setStatus] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  

  // Updated function to extract student ID from email in uppercase
  const getSid = (email) => {
    if (!email) return null;
    const username = email.split("@")[0];
    const parts = username.split("_");
    return parts.length > 1 ? parts[1].toUpperCase() : username.toUpperCase();
  };

  const sid = getSid(user.sid); // Extract student ID in uppercase

  useEffect(() => {
    if (!sid) return;

    fetch(`http://localhost:8080/requests/student/${sid}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch data");
        }
        return response.json();
      })
      .then((data) => {
        setTrackingRequests(data);
        setLoading(false);
      })
      .catch((error) => {
        setError(error.message);
        setLoading(false);
      });
  }, [sid]); // Re-fetch when sid changes

  const getStatusClass = (status) => {
    switch (status) {
      case "Approved":
        return "approved";
      case "Rejected":
        return "rejected";
      default:
        return "pending";
    }
  };
  const compareRequests = (a, b) => {
    const hasDecisionA = !a.decisionDate;
    const hasDecisionB = !b.decisionDate;
  
    // Case 1: both have decision dates → sort by decisionDate (desc)
    if (hasDecisionA && hasDecisionB) {
      return new Date(b.decisionDate) - new Date(a.decisionDate);
    }
  
    // Case 2: neither has decisionDate (both pending) → sort by request date (desc)
    if (!hasDecisionA && !hasDecisionB) {
      return new Date(b.date) - new Date(a.date);
    }
  
    // Case 3: one is decided, other is pending → pending requests first
    if (!hasDecisionA) return -1;
    if (!hasDecisionB) return 1;
  
    return 0;
  };

  const fetchRequestById = (id) => {
    fetch(`http://localhost:8080/requests/${id}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Request not found");
        }
        return response.json();
      })
      .then((data) => {
        setSelectedRequest(data);
      })
      .catch((error) => {
        console.error(error);
        setSelectedRequest(null);
      });
  };

 

  return (
    <div className="content">
      <div className="tracking">
        <h1 id="heading">Tracking Requests</h1>

        {loading && <p>Loading...</p>}
        {error && <p className="error">{error}</p>}
        
        {!loading && !error && trackingRequests.length > 0 ? (
          <div className="tracking-section">
            <div className="tracking-header">
                <div className="search">
                <label style={{fontSize:"16px"}}>Search by name:</label>
                <input
                  type="text"
                  placeholder="Enter activity name"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                </div>
                <div className="status-filter">
                      <div
                        className={`status-pending ${status === "Pending" ? "active" : ""}`}
                        onClick={() => setStatus(status === "Pending" ? "" : "Pending")}
                      >
                        Pending
                      </div>
                      <div
                        className={`status-approved ${status === "Approved" ? "active" : ""}`}
                        onClick={() => setStatus(status === "Approved" ? "" : "Approved")}
                      >
                        Approved
                      </div>
                      <div
                        className={`status-rejected ${status === "Rejected" ? "active" : ""}`}
                        onClick={() => setStatus(status === "Rejected" ? "" : "Rejected")}
                      >
                        Rejected
                      </div>
                    </div>
                
              </div>
             
            {trackingRequests.filter(request => request.status.toLowerCase().includes(status.toLowerCase())).filter(request => request.activityName.toLowerCase().includes(searchQuery.toLowerCase())).sort(compareRequests).map((request) => (
              <div key={request.rid} className="tracking-items" onClick={() => {
                fetchRequestById(request.rid)
                setSelectedRequest(request); // set activity data

                setIsOpen(true); // open modal
              }}>
                <div className="tracking-item-header"  style={{ display: "flex", justifyContent:"space-between" }}>
                <h3  style={{ cursor: "pointer" }}>
                  {request.activityName || "No Title"}
                </h3>
                <span className={getStatusClass(request.status)}>{request.status}</span>
                </div>
                <div className="dates" style={{ display: "flex", flexDirection: "column" }}>
                <span>Request date:{new Date(request.date).toLocaleString()}</span>
                <span>Decision date:{new Date(request.decisionDate).toLocaleString()}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p>No tracking requests found.</p>
        )}

        {/* Modal */}
        <ActivityModal
        open={isOpen}
        onClose={() => setIsOpen(false)}
        activity={selectedRequest}
      />
      </div>
    </div>
  );
};

export default Tracking;