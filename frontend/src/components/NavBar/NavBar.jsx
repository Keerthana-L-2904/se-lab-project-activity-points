import React, { useState, useEffect } from 'react';
import './navbar.css';
import axiosInstance from "../../utils/axiosConfig";

const NavBar = () => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const storedUser = localStorage.getItem("user");
        const storedAdmin = localStorage.getItem("admin");

        let userData = null;

        // 🧩 Case 1: Student or FA
        if (storedUser) {
          try {
            userData = JSON.parse(storedUser);
          } catch (err) {
            localStorage.removeItem("user");
            return;
          }


          if (userData.role === "student") {
            setRole("student");
            const res = await axiosInstance.get("/student");
            setUser(res.data);
            
            } else if (userData.role === "fa") {
              setRole("fa");
              const res = await axiosInstance.get("/fa");
              setUser(res.data);
            }

        }

        // 🧩 Case 2: Admin
        else if (storedAdmin) {
          try {
            const parsedAdmin = JSON.parse(storedAdmin);
            setRole("admin");
            setUser(parsedAdmin.admin || parsedAdmin);
          } catch (err) {
            localStorage.removeItem("admin");
          }
        }

        // 🧩 Case 3: No stored data at all
        else {
          console.error("No user or admin data in localStorage.");
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
        {/* Add your nav buttons or dropdowns here */}
      </div>
    </div>
  );
};

export default NavBar;