import React, { useEffect, useState, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import "./student.css";
<<<<<<< HEAD
import ActivityModal from "../../components/ActivityModal/ActivityModal";
=======
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b

const Tracking = () => {
  const { user } = useContext(AuthContext); // Get user details from AuthContext
  const [trackingRequests, setTrackingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
<<<<<<< HEAD
  const [searchQuery, setSearchQuery] = useState('');
  const [status, setStatus] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  
=======
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b

  // Updated function to extract student ID from email in uppercase
  const getSid = (email) => {
    if (!email) return null;
    const username = email.split("@")[0];
    const parts = username.split("_");
    return parts.length > 1 ? parts[1].toUpperCase() : username.toUpperCase();
  };

  const sid = getSid(user.sid); // Extract student ID in uppercase

<<<<<<< HEAD
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
=======
 useEffect(() => {
  if (!sid) return;

  fetch(`http://localhost:8080/api/${sid}/tracking`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch tracking data");
      }
      return response.json();
    })
    .then((data) => {
      // Save both requests and activities
      setTrackingRequests([
        ...data.requests.map(r => ({ ...r, type: "request" })),
        ...data.activities.map(a => ({ ...a, type: "activity" }))
      ]);
      setLoading(false);
    })
    .catch((error) => {
      setError(error.message);
      setLoading(false);
    });
}, [sid]);

>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b

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
<<<<<<< HEAD
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
=======
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b

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

<<<<<<< HEAD
 

=======
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b
  return (
    <div className="content">
      <div className="tracking">
        <h1 id="heading">Tracking Requests</h1>

        {loading && <p>Loading...</p>}
        {error && <p className="error">{error}</p>}
<<<<<<< HEAD
        
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
=======

        {!loading && !error && trackingRequests.length > 0 ? (
        <div className="tracking-section">
          {trackingRequests.map((item, index) => (
            <div key={index} className="tracking-items">
              <h3>
                {item.type === "request" ? item.activityName : item.title}
              </h3>
              <span className={getStatusClass(item.type === "request" ? item.status : item.validated)}>
                {item.type === "request" ? item.status : item.validated}
              </span>
            </div>
          ))}
        </div>

>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b
        ) : (
          <p>No tracking requests found.</p>
        )}

<<<<<<< HEAD
        {/* Modal */}
        <ActivityModal
        open={isOpen}
        onClose={() => setIsOpen(false)}
        activity={selectedRequest}
      />
=======
        {selectedRequest && (
          <div className="request-details">
            <h2>Request Details</h2>
            <p><strong>ID:</strong> {selectedRequest.rid}</p>
            <p><strong>Activity Name:</strong> {selectedRequest.activityName || "N/A"}</p>
            <p><strong>Description:</strong> {selectedRequest.description || "No description"}</p>
            <p><strong>Status:</strong> {selectedRequest.status}</p>
            <p><strong>Activity Date:</strong> {selectedRequest.activityDate || "N/A"}</p>
            <button onClick={() => setSelectedRequest(null)}>Close</button>
          </div>
        )}
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b
      </div>
    </div>
  );
};

export default Tracking;