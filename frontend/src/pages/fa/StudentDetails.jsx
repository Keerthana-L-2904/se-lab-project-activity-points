import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import "./studelist.css"; // Ensure this CSS exists

const StudentDetails = () => {
  const { sid } = useParams();
  const [student, setStudent] = useState(null);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStudentDetails = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/fa/student-details/${sid}`);
        if (!response.ok) throw new Error("Failed to fetch student details.");

        const data = await response.json();
        if (data.error) throw new Error(data.error);

        setStudent(data.student);
        setActivities(data.activities || []); // Ensure activities is always an array
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

  return (
    <div>
      <h2 style={{ textAlign: "center", marginLeft: "5%", color: "black", marginTop: "5%" }}>STUDENT DETAILS</h2>

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
              <h4 style={{ fontSize: "40px", color: "#6f42c1" }}>{student.deptPoints + student.institutePoints}</h4>
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
            {activities.length > 0 ? (
              activities.map((activity, index) => (
                <tr key={index}>
                  <td style={{textTransform:"uppercase",textAlign:"center"}}>{activity.title}</td>
                  <td style={{textAlign:"center"}}>{activity.points}</td>
                  <td style={{textAlign:"center"}}><a href={activity.link} target="_blank" rel="noopener noreferrer">View Proof</a></td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3">No activities found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default StudentDetails;
