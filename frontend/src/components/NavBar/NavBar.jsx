import React, { useState, useEffect } from 'react';
import './navbar.css';

const NavBar = () => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        let storedUser = localStorage.getItem("user");
        let userData = null;
  
        if (!storedUser) {
          // If not a student or FA, try admin
          const storedAdmin = localStorage.getItem("admin");
          if (!storedAdmin) {
            console.error("No user or admin data in localStorage.");
            return;
          }
          userData = JSON.parse(storedAdmin);
          setRole("admin");
          console.log("Admin Data:", userData);
          setUser(userData); // You already have name in login response, no need to fetch again
          return;
        }
  
        userData = JSON.parse(storedUser);
        console.log("Parsed User Data:", userData);
  
        if (userData.role === "student") {
          setRole("student");
          const response = await fetch(`http://localhost:8080/api/student/${userData.sid}`);
          const data = await response.json();
          setUser(data);
        } else if (userData.role === "fa") {
          setRole("fa");
          const response = await fetch(`http://localhost:8080/api/fa/${userData.faid}`);
          const data = await response.json();
          setUser(data);
        }
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    };
  
    fetchUserData();
  }, []);
  

  return (
    <div className="navbar">
      <div className="navbar__left">
        <h3>{user ? user.name : "Loading..."}</h3>
      </div>
      <div className="navbar__right">
        {/* Additional UI elements if needed */}
      </div>
    </div>
  );
};

export default NavBar;