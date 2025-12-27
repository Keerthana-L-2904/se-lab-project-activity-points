import React, { useEffect, useState, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import "./student.css";
import ActivityModal from "../../components/ActivityModal/ActivityModal";
import axiosInstance from "../../utils/axiosConfig";

const Tracking = () => {
  const { user } = useContext(AuthContext);
  const [trackingRequests, setTrackingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [status, setStatus] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  

  // Pagination
  const [page, setPage] = useState(1);
  const itemsPerPage = 5; // adjust as needed

  const getSid = (email) => {
    if (!email) return null;
    const username = email.split("@")[0];
    const parts = username.split("_");
    return parts.length > 1 ? parts[1].toUpperCase() : username.toUpperCase();
  };

      const sid = getSid(user?.sid);


        useEffect(() => {
          if (!sid) return;

          const fetchTrackingRequests = async () => {
            try {
              const response = await axiosInstance.get("/requests/student");
              setTrackingRequests(response.data);
            } catch (error) {
              const message =
                error.response?.data?.message || "Failed to fetch data";
              setError(message);
            } finally {
              setLoading(false);
            }
          };

          fetchTrackingRequests();
        }, [sid]);


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

    if (hasDecisionA && hasDecisionB) {
      return new Date(b.decisionDate) - new Date(a.decisionDate);
    }
    if (!hasDecisionA && !hasDecisionB) {
      return new Date(b.date) - new Date(a.date);
    }
    if (!hasDecisionA) return -1;
    if (!hasDecisionB) return 1;

    return 0;
  };

  // 🔹 Filter + Sort
  const filteredRequests = trackingRequests
    .filter((request) =>
      request.status.toLowerCase().includes(status.toLowerCase())
    )
    .filter((request) =>
      request.activityName.toLowerCase().includes(searchQuery.toLowerCase())
    )
    .sort(compareRequests);

  // 🔹 Pagination
  const paginatedRequests = filteredRequests.slice(
    (page - 1) * itemsPerPage,
    page * itemsPerPage
  );

  const totalPages = Math.ceil(filteredRequests.length / itemsPerPage);

  return (
    <div className="content">
      <div className="tracking">
        <h1 id="heading">Tracking Requests</h1>

        {loading && <p>Loading...</p>}
        {error && <p className="error">{error}</p>}

        {!loading && !error && trackingRequests.length > 0 ? (
          <div className="tracking-section">
            {/* 🔹 Search + Status Filter */}
            <div className="tracking-header">
              <div className="search">
                <label style={{ fontSize: "16px" }}>Search by name:</label>
                <input
                  type="text"
                  placeholder="Enter activity name"
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value);
                    setPage(1); // reset to page 1 on new search
                  }}
                />
              </div>

              <div className="status-filter">
                <div
                  className={`status-all ${status === "" ? "active" : ""}`}
                  onClick={() => {
                    setStatus("");
                    setPage(1);
                  }}
                >
                  All
                </div>
                <div
                  className={`status-pending ${
                    status === "Pending" ? "active" : ""
                  }`}
                  onClick={() => {
                    setStatus("Pending");
                    setPage(1);
                  }}
                >
                  Pending
                </div>
                <div
                  className={`status-approved ${
                    status === "Approved" ? "active" : ""
                  }`}
                  onClick={() => {
                    setStatus("Approved");
                    setPage(1);
                  }}
                >
                  Approved
                </div>
                <div
                  className={`status-rejected ${
                    status === "Rejected" ? "active" : ""
                  }`}
                  onClick={() => {
                    setStatus("Rejected");
                    setPage(1);
                  }}
                >
                  Rejected
                </div>
              </div>
            </div>

            {/* 🔹 Requests List */}
            {paginatedRequests.map((request) => (
              <div
                key={request.rid}
                className="tracking-items"
                onClick={() => {
                  setSelectedRequest(request);
                  setIsOpen(true);
                }}
              >
                <div
                  className="tracking-item-header"
                  style={{ display: "flex", justifyContent: "space-between" }}
                >
                  <h3 style={{ cursor: "pointer" }}>
                    {request.activityName || "No Title"}
                  </h3>
                  <span className={getStatusClass(request.status)}>
                    {request.status}
                  </span>
                </div>
                <div
                  className="dates"
                  style={{ display: "flex", flexDirection: "column" }}
                >
                  <span>
                    Request date: {new Date(request.date).toLocaleString()}
                  </span>
                  {request.status !== "Pending" && (
                    <span>
                      Decision date:{" "}
                      {new Date(request.decisionDate).toLocaleString()}
                    </span>
                  )}
                </div>
              </div>
            ))}

            {/* 🔹 Pagination */}
            {totalPages > 1 && (
              <div className="pagination" style={{ display: "flex", justifyContent: "center", marginTop: "20px"}}>
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    className={page === i + 1 ? "active" : ""}
                    onClick={() => setPage(i + 1)}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
            )}
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
