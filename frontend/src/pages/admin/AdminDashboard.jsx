import React,{useState,useEffect} from "react";
import { Link } from 'react-router-dom'
import "./AdminDashboard.css";
import { FaUserGraduate, FaUserTie } from "react-icons/fa";
import { FiSettings } from "react-icons/fi";
import axios from "axios"

const AdminDashboard = () => {
  const [stats,setStats]=useState({
    students_count:0,
    activities_count:0,
    fa_count:0
  })

  useEffect(()=>{
    fetchData()
  },[])


  const fetchData= async ()=>{
    try {
      const response = await axios.get("/api/admin/dashboard-details");
      console.log(response.data)
      if (response.status === 200) {
        setStats(response.data);
      } else {
        alert('Error loading dashboard details!');
      }
    } catch (error) {
      console.error('Error fetching dashboard details', error);
      alert('Failed to fetch dashboard details!');
    }
  }

  return (
    <div className="content">
      <main className="main-content">
        <header>
        <h1 style={{fontSize: "40px",
        fontWeight: "bold",
        textTransform: "uppercase"}}> Admin Dashboard</h1>
        </header>

        {/* Stats Section */}
        <section className="stats">
          <div className="stat-card">
            <FaUserGraduate className="icon" />
            <h3>Total Students</h3>
            <p>{stats.students_count}</p>
          </div>
          <div className="stat-card">
            <FaUserTie className="icon" />
            <h3>Total Faculty Advisors</h3>
            <p>{stats.fa_count}</p>
          </div>
          
        </section>

       <section className="activities">
        <Link to="/admin/users" className="link">
        <h2 style={{fontSize: "25px",textTransform: "uppercase",fontWeight:"bold"}}>User Management</h2>
          <div className="activity-card">
            <h3 style={{fontSize: "20px",textTransform: "uppercase"}}>Total Users:<br></br>{stats.fa_count+stats.students_count}</h3>
            <span>Add/Edit/Delete Users</span>
          </div>
          </Link>
        
          
        </section>
        {/* Activity Management */}
        <section className="activities">
        <Link to="/admin/activities" className="link">
        <h2 style={{fontSize: "25px",textTransform: "uppercase",fontWeight:"bold"}}>Activity Management</h2>
          <div className="activity-card">
            <h3 style={{fontSize: "20px",textTransform: "uppercase"}}>Total Activities:<br></br>{stats.activities_count}</h3>
            <span>Add/Edit/Delete Activities</span>
          </div>
          </Link>
        
          
        </section>
        <section className="activities">
        <Link to="/admin/activities" className="link">
        <h2 style={{fontSize: "25px",textTransform: "uppercase",fontWeight:"bold"}}>Guidelines</h2>
          <div className="activity-card">
            <h3 style={{fontSize: "20px",textTransform: "uppercase"}}>Guidelines</h3>
            <span>Add/Edit/Delete Guidelines</span>
          </div>
          </Link>
        
          
        </section>
        

        

      </main>
    </div>
  );
};

export default AdminDashboard;
