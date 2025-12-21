import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import "./studelist.css"; // Ensure this CSS exists
import { toast, Toaster } from "react-hot-toast"; 
import axios from "axios";

const StudentDetails = () => {
  const { sid } = useParams();
  const [student, setStudent] = useState(null);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const token = localStorage.getItem("token");

  // pagination states
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5; // change to 10 if you want more per page

  const openProof = async (sid, actId) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) throw new Error("Not authenticated");
  
      const url = `http://localhost:8080/api/fa/requests/${sid}/${actId}/proof`;
  
      const resp = await axios.get(url, {
        responseType: "blob", // important!
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
  
      // create blob and open
      const contentType = resp.headers["content-type"] || "application/octet-stream";
      const blob = new Blob([resp.data], { type: contentType });
      const blobUrl = URL.createObjectURL(blob);
  
      // open in new tab
      window.open(blobUrl, "_blank", "noopener,noreferrer");
  
      // optionally revoke after some time
      setTimeout(() => URL.revokeObjectURL(blobUrl), 1000 * 60);
    } catch (err) {
      console.error("Failed to open proof:", err);
      toast.error("Proof not uploaded");
    }
  };

  useEffect(() => {
    const fetchStudentDetails = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/fa/student-details/${sid}`,{
         headers:{ "Authorization": `Bearer ${token}`},
        });
        if (!response.ok) throw new Error("Failed to fetch student details.");

        const data = await response.json();
       
        if (data.error) throw new Error(data.error);

        setStudent(data.student);
        setActivities(data.activities || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchStudentDetails();
  }, [sid]);

  if (loading) return <p>Loading student details...</p>;
  if (error) return <p className="error-message">Error: {error}</p>;
  if (!student) return <p>No student details found.</p>;

  // pagination logic
  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const currentActivities = activities.slice(indexOfFirst, indexOfLast);
  const totalPages = Math.ceil(activities.length / itemsPerPage);
   console.log(student)
  const handleNext = () => {
    if (currentPage < totalPages) setCurrentPage(currentPage + 1);
  };

  const handlePrev = () => {
    if (currentPage > 1) setCurrentPage(currentPage - 1);
  };

  return (
    <div>
      <Toaster />
      <h2 style={{ textAlign: "center", marginLeft: "5%", color: "black", marginTop: "5%" }}>
        STUDENT DETAILS
      </h2>

      <div className="student-details-container">
        <div className="student-card">
          <div className="studentt-info">
            <div className="profile-pic">
              <span className="profile-icon">👤</span>
            </div>
            <p style={{ color: "black" }}>Name: {student.name}</p>
            <p style={{ color: "black" }}>Roll-No: {student.sid}</p>
          </div>

          <div className="points-summary">
            <div className="point-box">
              <h4 style={{ fontSize: "40px", color: "#6f42c1" }}>{student.deptPoints}</h4>
              <p>Total Department Points</p>
            </div>
            <div className="point-box">
              <h4 style={{ fontSize: "40px", color: "#6f42c1" }}>{student.institutePoints}</h4>
              <p>Total Institutional Points</p>
            </div>
            <div className="point-box">
              <h4 style={{ fontSize: "40px", color: "#6f42c1" }}>{student.otherPoints}</h4>
              <p>Other Points</p>
            </div>
            <div className="point-box">
              <h4 style={{ fontSize: "40px", color: "#6f42c1" }}>{student.deptPoints+student.institutePoints+student.otherPoints}</h4>
              <p>Total Activity Points</p>
            </div>
          </div>
        </div>

        <h3>List of Activities</h3>
        <table className="activities-table">
          <thead>
            <tr>
              <th>Activity Name</th>
              <th>Points</th>
              <th>Proof</th>
            </tr>
          </thead>
          <tbody>
            {currentActivities.length > 0 ? (
              currentActivities.map((activity, index) => (
                <tr key={index}>
                  <td style={{ textTransform: "uppercase", textAlign: "center" }}>
                    {activity.title}
                  </td>
                  <td style={{ textAlign: "center" }}>{activity.points}</td>
                  <td style={{ textAlign: "center" }}>
                  <button onClick={() => openProof(sid, activity.actID)}>
                  View Proof
                  </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3">No activities found.</td>
              </tr>
            )}
          </tbody>
        </table>

        {/* pagination controls */}
        {activities.length > itemsPerPage && (
          <div className="pagination-controls">
            <button onClick={handlePrev} disabled={currentPage === 1}>
              ⬅ Prev
            </button>
            <span>
              Page {currentPage} of {totalPages}
            </span>
            <button onClick={handleNext} disabled={currentPage === totalPages}>
              Next ➡
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default StudentDetails;
