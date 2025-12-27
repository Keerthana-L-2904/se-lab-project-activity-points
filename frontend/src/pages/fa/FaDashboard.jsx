
import React, { useEffect, useState } from "react"; 
import { Link } from "react-router-dom"; 
import "./Dashboard.css";
import axiosInstance from "../../utils/axiosConfig";
import { toast, Toaster } from "react-hot-toast"; 

const FaDashboard = () => {
    const [faData, setFaData] = useState([]);
    const [studentCount, setStudentCount] = useState(0);
    const [pendingRequestsCount, setPendingRequestsCount] = useState(0);
    const [departments, setDepartments] = useState({});
    const [students, setStudents] = useState([]);
    const storedFAID = localStorage.getItem("FAID") || localStorage.getItem("faid");
    const storedEmail = localStorage.getItem("email");
    
    
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
        }
    } else {
         toast.error("No user data found ");
    }
}, []);


    useEffect(() => {
        if (storedEmail) {
            fetchPendingRequests(storedEmail);
        }
    }, [storedEmail]);
    

    const fetchPendingRequests = async (email) => {
    if (!email) {
        return;
    }
    try {
        const response = await axiosInstance.get("/fa/details", {
            params: { email }
            });

        if (response.status === 200 && Array.isArray(response.data)) {
            const pendingRequests = response.data.length; 
            let c=0;
            for(let i=0;i<pendingRequests;i++){
                if(response.data[i].status=="Pending") c++;
            }
            setPendingRequestsCount(c);
        } else {
            console.warn("⚠️ Unexpected API response format:", response.data);
        }
    } catch (error) {
        toast.error("❌ Error fetching pending requests: " + error.message);
    }
};

        useEffect(() => {
        const loadDashboard = async () => {
            try {
            const { data } = await axiosInstance.get("/fa/dashboard");
            setFaData(data);

            const dids = [...new Set(
                data.map(fa => fa.department?.did || fa.department?.DID).filter(Boolean)
            )];

            if (!dids.length) return;

            const results = await Promise.all(
                dids.map(async (did) => {
                try {
                    const res = await axiosInstance.get(`/fa/departments/${did}`);
                    return { did, depName: res.data.name || "Unknown" };
                } catch {
                    return { did, depName: "Unknown" };
                }
                })
            );

            const depMap = results.reduce((acc, { did, depName }) => {
                acc[did] = depName;
                return acc;
            }, {});

            setDepartments(depMap);
            } catch (err) {
            console.error(err);
            }
        };

        loadDashboard();
        }, []);

   
         useEffect(() => {
         if (!storedFAID) return;

            const fetchStudents = async () => {
                try {
                const { data } = await axiosInstance.get("/fa/student-list");

                setStudentCount(data.length);   // total students
                setStudents(data.slice(-2));    // last 2 students
                } catch (error) {
                const message =
                    error.response?.data?.message ||
                    "Error fetching student list";
                toast.error("❌ " + message);
                }
            };

            fetchStudents();
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
                                <p>Department: {departments[fa.department?.did || fa.department?.DID] || "Unknown"} | Email: {fa.email || storedEmail}  | Roll-Number: {fa.faid} </p>
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
                <Link to="/student-list" className="all-btn">See All</Link>
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
