
import React, { useEffect, useState } from "react"; 
import { Link } from "react-router-dom"; 
import "./Dashboard.css";
import axios from "axios";
import { toast, Toaster } from "react-hot-toast"; 

const FaDashboard = () => {
    const [faData, setFaData] = useState([]);
    const [studentCount, setStudentCount] = useState(0);
    const [pendingRequestsCount, setPendingRequestsCount] = useState(0);
    const [departments, setDepartments] = useState({});
    const [students, setStudents] = useState([]);
    const storedFAID = localStorage.getItem("FAID") || localStorage.getItem("faid");
    const storedEmail = localStorage.getItem("email");
    const token=localStorage.getItem("token");
    
    useEffect(() => {
        const userData = JSON.parse(localStorage.getItem("user"));
        if (userData && userData.faid) {
            localStorage.setItem("faid", userData.faid);
        }
    }, []);

useEffect(() => {
    const userData = localStorage.getItem("user");
    if (userData) {
        const parsedUser = JSON.parse(userData);
        if (parsedUser.email) {
            localStorage.setItem("email", parsedUser.email);
           // console.log("✅ Email stored in localStorage:", parsedUser.email);
        }
    } else {

        console.warn("⚠️ No user data found in localStorage!");
    }
}, []);


    useEffect(() => {
        //console.log("ℹ️ Stored Email:", storedEmail); // Debugging Step 1
        if (storedEmail) {
            fetchPendingRequests(storedEmail);
        }
    }, [storedEmail]);
    

    const fetchPendingRequests = async (email) => {
    if (!email) {
      //  console.log("⚠️ Email is missing, API call not made!");
        return;
    }
    //console.log("📡 Calling API with email:", email); // Debugging Step 2

    try {
        const response = await axios.get(`/api/fa/details?email=${email}`,{
            headers:{
                "Authorization": `Bearer ${token}`,
            }
        });
        //console.log("✅ API Response:", response.data); // Debugging Step 3

        if (response.status === 200 && Array.isArray(response.data)) {
            const pendingRequests = response.data.length; // Fix: Get the length of the array
            let c=0;
            for(let i=0;i<pendingRequests;i++){
                if(response.data[i].status=="Pending") c++;
            }
           // console.log(c);
            setPendingRequestsCount(c);
        } else {
            console.warn("⚠️ Unexpected API response format:", response.data);
        }
    } catch (error) {
        toast.error("❌ Error fetching pending requests: " + error.message);
        console.error("❌ Error fetching pending requests:", error);
    }
};

    useEffect(() => {
        fetch("/api/fa/dashboard",{
            headers:{"Authorization": `Bearer ${token}`},
        })
            .then(response => response.json())
            .then(data => {
                setFaData(data);
                const dids = [...new Set(data.map(fa => fa.did).filter(Boolean))];
                if (dids.length === 0) return;

                Promise.all(dids.map(did =>
                    fetch(`/api/fa/departments/${did}`,{
                       headers:{ "Authorization": `Bearer ${token}`},
                    })
                        .then(response => response.json())
                        .then(dep => ({ did, depName: dep.name || "Unknown" })) 
                        .catch(() => ({ did, depName: "Unknown" }))
                )).then(results => {
                    const depMap = results.reduce((acc, { did, depName }) => {
                        acc[did] = depName;
                        return acc;
                    }, {});
                    setDepartments(depMap);
                });

            })
            .catch(error => toast.error("❌ Error fetching FA data: " + error.message));
    }, []);
   
    useEffect(() => {
        if (!storedFAID) return;
    
        fetch(`http://localhost:8080/api/fa/student-list/${storedFAID}`,
            {headers:{
                "Authorization": `Bearer ${token}`,
            }}
        )
            .then(response => response.json())
            .then(data => {
                setStudentCount(data.length); // Set total student count
                setStudents(data.slice(-2)); // Display only last 2 students
            })
            .catch(error => toast.error("❌ Error fetching student list: " + error.message));
    }, [storedFAID]);
    
    
    
    return (
        <div className="dashboard-container">
            <Toaster />
            <h1 className="dashboard-title">FACULTY ADVISOR DASHBOARD</h1>

            {faData.length > 0 && (
                <div className="student-info">
                    {faData
                        .filter(fa => Number(fa.faid) === Number(storedFAID)) 
                        .map(fa => (
                            <div key={fa.FAID}>
                                <h3>Welcome back, {fa.name}!</h3>
                                <p>Department: {departments[fa.did] || "Unknown"} | Email: {fa.email || storedEmail}  | Roll-Number: {fa.faid} </p>
                            </div>
                        ))
                    }
                </div>
            )}

            <div className="points-section">
                <div className="progress-box">
                    <h2>{studentCount}</h2> {/* Updated with actual count */}
                    <p>Number of Students under FAship</p>
                </div>
                <div className="progress-box">
                <h2>{pendingRequestsCount}</h2> {/* 🔹 Updated with actual count */}
                    <p>Number of Pending Approvals</p>
                </div>
            </div>
            <div className="activity-header">
                <h2 className="activity-title">Student List</h2>
                <Link to="/fa/student-list" className="all-btn">See All</Link>
            </div>

            <table className="activity-table">
                <thead>
                    <tr>
                        <th>Student Name</th>
                        <th>Student ID</th>
                        <th>Total Activity Points</th>
                    </tr>
                </thead>
                <tbody>
                    {students.length > 0 ? (
                        students.map(student => (
                            <tr key={student.sid}>
                                <td style={{ textTransform: "uppercase" }}>{student.name}</td>
                                <td>{student.sid}</td>
                                <td>{student.activityPoints}</td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="3">No recent students found</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default FaDashboard;
