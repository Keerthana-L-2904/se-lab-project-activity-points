import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axiosInstance from "../../utils/axiosConfig"; // ✅ ADD THIS IMPORT
import "./student.css";
import toast, { Toaster } from "react-hot-toast";

const Announcements = () => {
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAnnouncements = async () => {
    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) {
        setError("User not found. Please log in.");
        setLoading(false);
        return;
      }

      const user = JSON.parse(storedUser);
      if (!user?.sid) {
        setError("Invalid student ID.");
        setLoading(false);
        return;
      }

      // ✅ FIXED: Axios returns data directly in response.data
      const response = await axiosInstance.get(
        `/student/announcements`
      );
      
      // ✅ Axios automatically throws on non-2xx status codes
      setAnnouncements(response.data); // ✅ Use response.data, not response.json()
      
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || "Failed to fetch announcements";
      toast.error("Error fetching announcements: " + errorMessage);
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnnouncements();
  }, []);

  return (
    <div className="content">
      <Toaster />
      <h1>Announcements</h1>
      
      {loading && <p>Loading announcements...</p>}
      {error && <p className="error-message">Error: {error}</p>}
      {!loading && !error && announcements.length === 0 && <p>No announcements available.</p>}
      
      <div className="announcements">
        {announcements.map((announcement) => (
          <Link 
            to={`/student/announcements/${announcement.aid}`}  // ✅ FIXED: Added = sign
            key={announcement.aid} 
            className="announcement"
          >
            <div className="announcement-details">
              <h3>{announcement.title}</h3>
              <p>{announcement.body}</p>
            </div>
            <span>
              {new Date(announcement.date).toLocaleDateString("en-GB")} | {announcement.time}
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Announcements;