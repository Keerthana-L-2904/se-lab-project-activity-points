import React, { useState, useEffect, useContext } from "react";
import "./approvals.css";
import { AuthContext } from "../../context/AuthContext";
import axios from "axios";
import { toast, Toaster } from "react-hot-toast"; 

const Approvals = () => {
  const { user } = useContext(AuthContext);
  const [requests, setRequests] = useState([]);
  const [faDetails, setFaDetails] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [points, setPoints] = useState({});
  const [studentFAIds, setStudentFAIds] = useState({});
  const [comments, setComments] = useState({});
  const [loading, setLoading] = useState({}); // track loading state per request

  // Fetch FA Details and Requests
  useEffect(() => {
    if (user) {
      fetchfaData(user.email);
    }
  }, [user]);

  useEffect(() => {
    if (requests.length > 0 && faDetails) {
      requests.forEach(req => fetchStudFa(req.sid));
    }
   
  }, [requests, faDetails]);
  

    const fetchfaData = async (email) => {
      if (!email) return;
      try {
        const response = await axios.get(`/api/fa/details?email=${email}`);
        if (response.status === 200) {
          setFaDetails({
            faId: response.data.fa_id,
            name: response.data.name
          });

          setRequests(response.data);

          // initialize points from requests
          const initialPoints = {};
          response.data.forEach(req => {
            initialPoints[req.rid] = req.points; // assuming backend sends "points"
          });
          setPoints(initialPoints);

          console.log(response.data);
        }
      } catch (error) {
        console.error("Error fetching FA details", error);
        toast.error("Error fetching FA details!");
      }
    };


  const fetchStudFa = async (sid) => {
    if (studentFAIds[sid]) return; // Already fetched
  
    try {
      const response = await axios.get(`/api/fa/get-Fa?sid=${sid}`);
      console.log(response.data)
      if (response.status === 200) {
        setStudentFAIds(prev => ({
          ...prev,
          [sid]: response.data
        }));
      }
    } catch (error) {
      console.error("Error fetching student's FA", error);
    }
  };
  

  // Approve Request
  const handleApprove = async (rid, index,sid) => {
    if (loading[rid]) return; // already processing

    setLoading(prev => ({ ...prev, [rid]: true })); // disable immediately
    let enteredPoints = points[rid];
    console.log("hello") 
    try {
      if (studentFAIds[sid]=== user.faid && (!enteredPoints  || enteredPoints <= 0)) {
        enteredPoints=5;
        throw new Error("Points cannot be negative");
      }
      if (studentFAIds[sid]!== user.faid) {
        enteredPoints=5;
      }
    
      const response = await axios.post(`http://localhost:8080/api/fa/approve-request/${rid}?email=${user.email}&points=${enteredPoints}`);
      if (response.status === 200) {
        await fetchfaData(user.email);
        toast.success("Approval successful");
      }
    } catch (error) {
      console.error("Approval error", error);
      toast.error(error.message || error.response?.data || "Approval failed!");
    }finally {
      setLoading(prev => ({ ...prev, [rid]: false })); // re-enable if failed
    }
    
  };

  // Reject Request
  const handleReject = async (rid, index,comment) => {
    try {
      const response = await axios.post(`/api/fa/reject-request/${rid}`,{comment:comment?comment: "No comments"});
      if (response.status === 200) {
        await fetchfaData(user.email);
        toast.success("Rejection successful")
      }else {
        toast.error("Failed to reject!");
    }
    } catch (error) {
      console.error("Rejection error", error);
      toast.error("Rejection failed!");
    }
  };

  return (
    <div className="content"><Toaster />
      <div className="header">
        <h1>Approval Management</h1>
        <div className="search-add">
          <div className="search">
            <label style={{ fontSize: "16px" }}>Search by SID:</label>
            <input
              type="text"
              placeholder="Enter Student ID"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>
      </div>

      <div className="body">
        <table className="styled-table">
          <thead>
            <tr>
              <th>Student ID</th>
              <th>Name</th>
              <th>Activity Name</th>
              <th>Activity Date</th>
              <th>Type</th>
              <th>Proof</th>
              <th>Points</th>
              <th>Comments</th>
              <th>Approve</th>
              <th>Reject</th>
            </tr>
          </thead>
          <tbody>
            {requests
              .filter((req)=>req.status==='Pending')
              .filter((req) => req.sid.includes(searchQuery))
              .map((req, index) => (
                <tr key={req.rid} className={req.validated === "Approved" ? "approved-row" : req.validated === "Rejected" ? "rejected-row" : ""}>
                  <td>{req.sid}</td>
                  <td>{req.name}</td>
                  <td>{req.activity_name}</td>
                  <td>{new Date(req.activity_date).toLocaleDateString("en-GB")}</td>
                  <td>{req.type}</td>
                  <td>
                      {req.proof ? (
                      <a
                        href={`http://localhost:8080/api/fa/requests/${req.rid}/proof`}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        View Proof
                      </a>
                    ) : (
                      "No Proof Available"
                    )}
                  </td>
                  <td>
  {
    
    studentFAIds[req.sid] === user.faid ? (
      <input
        type="text"
        className="points"
        value={points[req.rid] || ""}
        onChange={(e) =>
          setPoints(prev => ({ ...prev, [req.rid]: e.target.value }))
        }
      />
    ) : (
      <span style={{ color: "#aaa" }}>Not Your Student</span>
    )
  }
</td>
<td>
<input
        type="text"
        className="points"
        value={comments[req.rid] || ""}
        onChange={(e) =>
          setComments(prev => ({ ...prev, [req.rid]: e.target.value }))
        }
      />
</td>
      

                  <td>
                  <button
  className="approve-btn"
  onClick={() => handleApprove(req.rid, index, req.sid)}
  disabled={loading[req.rid] || req.validated === "Approved" || req.validated === "Rejected"}
>
  {loading[req.rid] ? "Processing..." : "✔️ Approve"}
</button>
                  </td>
                  <td>
                    <button
                      className="reject-btn"
                      onClick={() => handleReject(req.rid, index,comments[req.rid])}
                      disabled={req.validated === "Rejected"}
                    >
                      ❌ Reject
                    </button>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Approvals;