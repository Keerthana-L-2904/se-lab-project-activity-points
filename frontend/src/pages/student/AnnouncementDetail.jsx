import React, { useState, useEffect } from "react";
import { Link, useParams } from "react-router-dom";
import axios from "axios";
import Announcements from "./Announcements";


const formatDate = (dateString) => {
  const dateObj = new Date(dateString);
  dateObj.setMinutes(dateObj.getMinutes() + dateObj.getTimezoneOffset()); // Convert to UTC
  return `${dateObj.getDate().toString().padStart(2, '0')}-${(dateObj.getMonth() + 1).toString().padStart(2, '0')}-${dateObj.getFullYear()}`;
};


const AnnouncementDetail = () => {
  const [announcement, setAnnouncement] = useState(null);
  const [loading, setLoading] = useState(true);
  const { id } = useParams(); // Get the announcement ID from the URL

  useEffect(() => {
    const fetchData = async () => {
      try {
        const storedUser = localStorage.getItem("user");
        if (!storedUser) {
          console.error("No user data in localStorage.");
          alert("User not found. Please log in.");
          return;
        }

        const user = JSON.parse(storedUser);
        if (!user?.sid) {
          console.error("Student ID missing in user data:", user);
          alert("Invalid student ID.");
          return;
        }

        const response = await axios.get(`/api/student/${user.sid}/announcements/${id}`);
        if (response.status === 200) {
          setAnnouncement(response.data);
        } else {
          alert("Error loading announcement!");
        }
      } catch (error) {
        console.error("Error fetching announcement", error);
        alert("Failed to fetch announcement!");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]); // Dependency array ensures it runs when `id` changes

  // ✅ Prevent error by checking if `announcement` is null
  if (loading) {
    return <div>Loading announcement...</div>;
  }

  if (!announcement) {
    return <div>Error: Announcement not found!</div>;
  }
  console.log(announcement)
  return (
    <div>
      <div className="content">
        <div className="announcement-content">
          <div className="announcement-title-time">
            <h1>{announcement.title}</h1>
            <span>{new Date(announcement.date).toLocaleDateString("en-GB")}, {announcement.time}</span>
          </div>
          <div className="announcement-body">
            {announcement.body}
          </div>
          <Link to="/student/announcements">
            <button className="btn">Close</button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AnnouncementDetail;
