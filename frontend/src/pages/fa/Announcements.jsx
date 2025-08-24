import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
// import "./student.css";

const Announcements = () => {
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [newAnnouncement, setNewAnnouncement] = useState({
  title: "",
  body: "",
});


const handleCreateAnnouncement = async () => {
  try {
    const storedUser = localStorage.getItem("user");
    if (!storedUser) {
      console.error("No user data in localStorage.");
      setError("User not found. Please log in.");
      return;
    }

    const user = JSON.parse(storedUser);
    if (!user?.faid) {
      console.error("FA ID missing in user data:", user);
      setError("Invalid FA ID.");
      return;
    }

    const response = await fetch("http://localhost:8080/api/fa/announcements", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        title: newAnnouncement.title,
        body: newAnnouncement.body,
        date: new Date().toISOString(),
        time: new Date().toTimeString().split(' ')[0],
        faid:parseInt(user.faid)
      })
    });

    if (!response.ok) throw new Error("Failed to create announcement");

    alert("Announcement created!");
    setShowModal(false);
    setNewAnnouncement({ title: "", body: "" });
    fetchAnnouncements(); // refresh the list
  } catch (err) {
    alert("Error: " + err.message);
  }
};



  const fetchAnnouncements = async () => {
    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) {
        console.error("No user data in localStorage.");
        setError("User not found. Please log in.");
        return;
      }

      const user = JSON.parse(storedUser);
      if (!user?.faid) {
        console.error("FA ID missing in user data:", user);
        setError("Invalid FA ID.");
        return;
      }

      const response = await fetch(`http://localhost:8080/api/fa/${user.faid}/announcements`);
      if (!response.ok) throw new Error("Failed to fetch announcements.");

      const data = await response.json();
      setAnnouncements(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnnouncements();
  }, []);

  return (
    <div className="content">
      <h1>Announcements</h1>

      {loading && <p>Loading announcements...</p>}
      {error && <p className="error-message">Error: {error}</p>}

      {!loading && !error && announcements.length === 0 && <p>No announcements available.</p>}

      <div className="announcements">
        {announcements.map((announcement) => (
          <Link to={`/fa/announcements/${announcement.aid}`} key={announcement.aid} className="announcement">

            <div className="announcement-details">
              <h3>{announcement.title}</h3>
              <p>{announcement.body}</p>
            </div>
            <span>{new Date(announcement.date).toLocaleDateString("en-GB")} | {announcement.time}</span>
          </Link>
        ))}
      </div>
      <div 
  className="create-announcement"
  style={{
    position: "fixed",
    bottom: "30px",
    right: "30px",
    zIndex: 100,
    backgroundColor: "#925FE2",
    color: "white",
    borderRadius: "50%",
    width: "60px",
    height: "60px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    cursor: "pointer",
    boxShadow: "0 4px 12px rgba(0,0,0,0.2)",
    fontSize: "24px"
  }}
  onClick={() => setShowModal(true)}
>
  <i className="bi bi-pencil-fill"></i>
</div>

      {showModal && (
  <div style={{
    position: "fixed",
    top: 0,
    left: 0,
    width: "100vw",
    height: "100vh",
    backgroundColor: "rgba(0, 0, 0, 0.5)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 200
  }}>
    <div style={{
      backgroundColor: "white",
      padding: "20px",
      borderRadius: "10px",
      width: "90%",
      maxWidth: "500px"
    }}>
      <h2>New Announcement</h2>
      <input 
        type="text" 
        placeholder="Title" 
        value={newAnnouncement.title}
        onChange={(e) => setNewAnnouncement({ ...newAnnouncement, title: e.target.value })}
        style={{ width: "100%", marginBottom: "10px", padding: "8px" }}
      />
      <textarea 
        placeholder="Body" 
        value={newAnnouncement.body}
        onChange={(e) => setNewAnnouncement({ ...newAnnouncement, body: e.target.value })}
        rows={4}
        style={{ width: "100%", marginBottom: "10px", padding: "8px" }}
      />
      <div style={{ display: "flex", justifyContent: "flex-end" }}>
        <button onClick={() => setShowModal(false)} style={{ marginRight: "10px" }}>Cancel</button>
        <button onClick={handleCreateAnnouncement}>Create</button>
      </div>
    </div>
  </div>
)}

      
    </div>
  );
};

export default Announcements;
