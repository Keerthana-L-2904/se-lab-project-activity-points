import React, { useState, useEffect } from "react";
import { Link, useParams } from "react-router-dom";
import axiosInstance from "../../utils/axiosConfig";
import { toast, Toaster } from "react-hot-toast";

const formatDate = (dateString) => {
  const dateObj = new Date(dateString);
  dateObj.setMinutes(dateObj.getMinutes() + dateObj.getTimezoneOffset()); // Convert to UTC
  return `${dateObj.getDate().toString().padStart(2, "0")}-${(
    dateObj.getMonth() + 1
  )
    .toString()
    .padStart(2, "0")}-${dateObj.getFullYear()}`;
};

const AnnouncementDetail = () => {
  const [announcement, setAnnouncement] = useState(null);
  const [loading, setLoading] = useState(true);
  const { id } = useParams();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const storedUser = localStorage.getItem("user");
        if (!storedUser) {
          toast.error("User not found. Please log in.");
          setLoading(false);
          return;
        }

        const user = JSON.parse(storedUser);
        if (!user?.sid) {
          toast.error("Invalid student ID.");
          setLoading(false);
          return;
        }

        // ✅ FIXED: Axios automatically handles JSON parsing
        const response = await axiosInstance.get(
          `/student/announcements/${id}`
        );

        // ✅ FIXED: Access data from response.data
        setAnnouncement(response.data);
      } catch (error) {
        const errorMessage = error.response?.data?.message || error.message;
        toast.error("Failed to fetch announcement: " + errorMessage);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  if (loading) {
    return (
      <div className="content">
        <Toaster />
        <div>Loading announcement...</div>
      </div>
    );
  }

  if (!announcement) {
    return (
      <div className="content">
        <Toaster />
        <div>Error: Announcement not found!</div>
        <Link to="/student/announcements">
          <button className="btn">Back to Announcements</button>
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div className="content">
        <Toaster />
        <div className="announcement-content">
          <div className="announcement-title-time">
            <h1>{announcement.title}</h1>
            <span>
              {new Date(announcement.date).toLocaleDateString("en-GB")},{" "}
              {announcement.time}
            </span>
          </div>
          <div className="announcement-body">{announcement.body}</div>
          <Link to="/student/announcements">
            <button className="btn">Close</button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AnnouncementDetail;