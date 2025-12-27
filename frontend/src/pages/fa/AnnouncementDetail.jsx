import React, { useState, useEffect } from "react";
import { Link, useParams } from "react-router-dom";
import axiosInstance from "../../utils/axiosConfig";
import Announcements from "./Announcements";
import { toast, Toaster } from "react-hot-toast"; 


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
          toast.error("User not found. Please log in.");
          return;
        }

        const user = JSON.parse(storedUser);
        if (!user?.faid) {
          toast.error("Invalid FA ID.");
          return;
        }

        const response = await axiosInstance.get(
        `/fa/${user.faid}/announcements/${id}`
      );

        if (response.status === 200) {
          setAnnouncement(response.data);
        } else {
          toast.error("Error loading announcement!");
        }
      } catch (error) {
        toast.error("Failed to fetch announcement!");
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
  return (
    <div>
      <div className="content"><Toaster />
        <div className="announcement-content">
          <div className="announcement-title-time">
            <h1>{announcement.title}</h1>
            <span>{new Date(announcement.date).toLocaleDateString("en-GB")}, {announcement.time}</span>
          </div>
          <div className="announcement-body">
            {announcement.body}
          </div>
          <Link to="/fa/announcements">
            <button className="btn">Close</button>
          </Link>
        </div>
        
      </div>
    </div>
  );
};

export default AnnouncementDetail;
