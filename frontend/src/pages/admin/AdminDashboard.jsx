<<<<<<< HEAD
import React,{useState,useEffect} from "react";
=======
import React, { useState, useEffect } from "react";
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b
import { Link } from 'react-router-dom'
import "./AdminDashboard.css";
import { FaUserGraduate, FaUserTie } from "react-icons/fa";
import { FiSettings } from "react-icons/fi";
import axios from "axios"

const AdminDashboard = () => {
<<<<<<< HEAD
  const [stats,setStats]=useState({
    students_count:0,
    activities_count:0,
    fa_count:0
  })

  useEffect(()=>{
    fetchData()
  },[])


  const fetchData= async ()=>{
=======
  const [stats, setStats] = useState({
    students_count: 0,
    activities_count: 0,
    fa_count: 0
  })

  useEffect(() => {
    fetchData()
  }, [])


  const fetchData = async () => {
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b
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
<<<<<<< HEAD
        <h1 style={{fontSize: "40px",
        fontWeight: "bold",
        textTransform: "uppercase"}}> Admin Dashboard</h1>
=======
          <h1 style={{
            fontSize: "40px",
            fontWeight: "bold",
            textTransform: "uppercase"
          }}> Admin Dashboard</h1>
>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b
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
<<<<<<< HEAD
          
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
        

        
=======

        </section>

        <section className="activities">
          <Link to="/admin/users" className="link">
            <h2 style={{ fontSize: "25px", textTransform: "uppercase", fontWeight: "bold" }}>User Management</h2>
            <div className="activity-card">
              <h3 style={{ fontSize: "20px", textTransform: "uppercase" }}>Total Users:<br></br>{stats.fa_count + stats.students_count}</h3>
              <span>Add/Edit/Delete Users</span>
            </div>
          </Link>


        </section>
        {/* Activity Management */}
        <section className="activities">
          <Link to="/admin/activities" className="link">
            <h2 style={{ fontSize: "25px", textTransform: "uppercase", fontWeight: "bold" }}>Activity Management</h2>
            <div className="activity-card">
              <h3 style={{ fontSize: "20px", textTransform: "uppercase" }}>Total Activities:<br></br>{stats.activities_count}</h3>
              <span>Add/Edit/Delete Activities</span>
            </div>
          </Link>


        </section>
        <section className="activities">
          <Link to="/admin/activities" className="link">
            <h2 style={{ fontSize: "25px", textTransform: "uppercase", fontWeight: "bold" }}>Guidelines</h2>
            <div className="activity-card">
              <h3 style={{ fontSize: "20px", textTransform: "uppercase" }}>Guidelines</h3>
              <span>Add/Edit/Delete Guidelines</span>
            </div>
          </Link>


        </section>



>>>>>>> 023ad54b02c4663b35eab417a91775faf000254b

      </main>
    </div>
  );
};

export default AdminDashboard;
