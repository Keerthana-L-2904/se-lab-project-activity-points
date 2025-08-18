import { useState, useEffect } from "react";
import "./studlist.css";
import { Link } from "react-router-dom";
export default function StudentList() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const faid = localStorage.getItem("FAID") || localStorage.getItem("faid");

if (!faid) {
    console.error("FAID not found in localStorage");
} else {
    console.log("FAID found:", faid);
}

  useEffect(() => {
    if (!faid) {
      console.error("FAID not found in localStorage");
      setLoading(false);
      return;
    }
    
    fetch(`http://localhost:8080/api/fa/student-list/${faid}`)
      .then((response) => response.json())
      .then((data) => {
        setStudents(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching students:", error);
        setLoading(false);
      });
  }, [faid]);

  if (loading) return <p>Loading...</p>;

  return (
    <div className="list-header">
        <h2 className="list-title">List of Students</h2>
        <table className="list-table">
            <thead>
                <tr>
                    <th>Student Name</th>
                    <th>Roll No.</th>
                    <th>Total Points</th>
                    <th>Detail View</th>
                </tr>
            </thead>
            <tbody>
                {students.length > 0 ? (
                    students.map((student) => (
                        <tr key={student.sid}>
                            <td style={{ textTransform: "uppercase" }}>{student.name}</td>
                            <td>{student.sid}</td>
                            <td>{student.institutePoints + student.deptPoints}</td>
                            <td>
                                <Link 
                                    to={student.sid ? `/fa/student-details/${student.sid}` : "#"} 
                                    onClick={(e) => {
                                    if (!student.sid) {
                                        e.preventDefault();
                                        console.error("Student SID is undefined");
                                    }
                                    }}>
                                    <button className="vieww-btn">View</button>
                                </Link>
                            </td>

                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="4">No students found</td>
                    </tr>
                )}
            </tbody>
        </table>
    </div>
);
}