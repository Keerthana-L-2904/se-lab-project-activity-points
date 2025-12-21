import React, { useState, useEffect } from 'react';
import './navbar.css';

const NavBar = () => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const storedUser = localStorage.getItem("user");
        const storedAdmin = localStorage.getItem("admin");
        const token = localStorage.getItem("token");

        let userData = null;

        // 🧩 Case 1: Student or FA
        if (storedUser) {
          try {
            userData = JSON.parse(storedUser);
          } catch (err) {
            console.error("Invalid JSON in 'user' localStorage:", err);
            localStorage.removeItem("user");
            return;
          }

          //console.log("Parsed User Data:", userData);

          if (userData.role === "student") {
            setRole("student");
            const response = await fetch(`http://localhost:8080/api/student/${userData.sid}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            const data = await response.json();
            setUser(data);
          } else if (userData.role === "fa") {
            setRole("fa");
            const response = await fetch(`http://localhost:8080/api/fa/${userData.faid}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            const data = await response.json();
            setUser(data);
          }
        }

        // 🧩 Case 2: Admin
        else if (storedAdmin) {
          try {
            const parsedAdmin = JSON.parse(storedAdmin);
         // console.log("Admin Data:", parsedAdmin);

            // store token if it exists inside admin object
            if (parsedAdmin.token) {
              localStorage.setItem("token", parsedAdmin.token);
            }

            setRole("admin");
            setUser(parsedAdmin.admin || parsedAdmin);
          } catch (err) {
            console.error("Invalid JSON in 'admin' localStorage:", err);
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