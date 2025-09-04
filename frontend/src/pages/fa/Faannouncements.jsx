import React from "react";

const Announcement = () => {
  return (
    <div style={styles.announcementContainer}>
      <div style={styles.header}>
        <h2>Announcement</h2>
      </div>
      <div style={styles.announcementBox}>
        {[1, 2, 3].map((item, index) => (
          <div style={styles.announcementItem} key={index}>
            <div style={styles.announcementContent}>
              <strong>Title</strong>
              <p>pdf uploaded</p>
            </div>
            <span style={styles.time}>9:41 AM</span>
          </div>
        ))}
      </div>
      <button style={styles.editButton}>✎</button>
    </div>
  );
};

const styles = {
  announcementContainer: {
    padding: "20px",
    backgroundColor: "#f5f0ff",
    borderRadius: "10px",
    width: "80%",
    margin: "auto",
  },
  header: {
    fontSize: "18px",
    fontWeight: "bold",
    marginBottom: "10px",
  },
  announcementBox: {
    backgroundColor: "#e6d6ff",
    padding: "15px",
    borderRadius: "10px",
  },
  announcementItem: {
    backgroundColor: "#d8c4ff",
    padding: "15px",
    borderRadius: "5px",
    marginBottom: "10px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    width: "100%",
  },
  announcementContent: {
    display: "flex",
    flexDirection: "column",
  },
  time: {
    fontSize: "14px",
    color: "#666",
  },
  editButton: {
    backgroundColor: "#7d5fff",
    color: "white",
    border: "none",
    padding: "10px 14px",
    borderRadius: "5px",
    cursor: "pointer",
    marginTop: "10px",
  },
};

export default Announcement;
