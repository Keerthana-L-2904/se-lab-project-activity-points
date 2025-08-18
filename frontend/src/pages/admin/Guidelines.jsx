import React, { useState, useEffect } from "react";
import axios from "axios"; // Import axios for API calls
import "./Guidelines.css";

const Guidelines = () => {
  const [showPopup, setShowPopup] = useState(false);
  const [inputText, setInputText] = useState({ body: "" });
  const [guidelines, setGuidelines] = useState([]);
  const [editGuideline, setEditGuideline] = useState(null); // Track which guideline is being edited

  // 📌 Fetch Guidelines from Backend
  const fetchData = async () => {
    try {
      const response = await axios.get("/api/admin/guidelines");
      if (response.status === 200) {
        setGuidelines(response.data); // Store guidelines in state
      } else {
        alert("Error loading guidelines!");
      }
    } catch (error) {
      console.error("Error fetching guidelines", error);
      alert("Failed to fetch guidelines!");
    }
  };

  // 📌 Load guidelines on page load
  useEffect(() => {
    fetchData();
  }, []);

  // 📌 Show Add Guideline Popup
  const handleCreateClick = () => {
    setInputText({ body: "" });
    setEditGuideline(null);
    setShowPopup(true);
  };

  // 📌 Show Edit Guideline Popup
  const handleEditClick = (guideline) => {
    setInputText({ body: guideline.body });
    setEditGuideline(guideline);
    setShowPopup(true);
  };

  // 📌 Close Popup
  const handleCloseClick = () => {
    setShowPopup(false);
    setInputText({ body: "" });
    setEditGuideline(null);
  };

  // 📌 Save (Create or Update) Guideline
  const handleSaveClick = async () => {
    if (!inputText.body.trim()) {
      alert("Guideline cannot be empty!");
      return;
    }

    try {
      if (editGuideline) {
        // 📝 Update Existing Guideline
        const response = await axios.put(`/api/admin/guidelines/${editGuideline.gid}`, inputText);
        if (response.status === 200) {
          fetchData(); // Refresh List
          setShowPopup(false);
        } else {
          alert("Error updating guideline!");
        }
      } else {
        // ➕ Create New Guideline
        const response = await axios.post("/api/admin/guidelines", inputText);
        if (response.status === 200) {
          fetchData(); // Refresh List
          setShowPopup(false);
        } else {
          alert("Error adding guideline!");
        }
      }
    } catch (error) {
      console.error("Error saving guideline", error);
      alert("Failed to save guideline!");
    }
  };

  // 📌 Delete Guideline
  const handleDelete = async (id) => {
    try {
      const response = await axios.delete(`/api/admin/guidelines/${id}`);
      if (response.status === 200 ||  response.status === 204) {
        fetchData(); // Refresh List
      } else {
        alert("Error deleting guideline!");
      }
    } catch (error) {
      console.error("Error deleting guideline", error);
      alert("Failed to delete guideline!");
    }
  };

  return (
    <div className="content">
      <div className="container-fluid">
        <div className="header">
          <h1 className="guidelines-title">Activity Points Guidelines</h1>
        </div>

        <div className="card">
          <div className="card-body">
            <ul className="guidelines-list">
              {guidelines.map((guideline, index) => (
                <li className="guideline-item" key={guideline.gid}>
                  <span>{index + 1}. {guideline.body}</span>
                  <div className="icon-group">
                    <i className="bi bi-pencil-fill edit-icon" onClick={() => handleEditClick(guideline)}></i>
                    <i className="bi bi-trash3 delete-icon" onClick={() => handleDelete(guideline.gid)}></i>
                  </div>
                </li>
              ))}
            </ul>
            <div className="alert alert-info mt-4">
              <i className="bi bi-info-circle me-2"></i>
              Note: Make sure to follow all guidelines strictly to ensure smooth processing of your activity points.
            </div>
          </div>
        </div>

        {/* Create Button */}
        <div className="create-button-container">
          <button className="activity-guideline-button" onClick={handleCreateClick}>
            <i className="bi bi-plus-circle"></i>
          </button>
        </div>

        {/* Popup (Shared for Add/Edit) */}
        {showPopup && (
          <div className="popup-overlay">
            <div className="popup-box">
              <h4>{editGuideline ? "Edit Guideline" : "Enter a New Guideline"}</h4>
              <input 
                type="text" 
                value={inputText.body} 
                onChange={(e) => setInputText({ body: e.target.value })}
              />
              <div className="popup-buttons">
                <button className="save-button" onClick={handleSaveClick}>
                  {editGuideline ? "Update" : "Save"}
                </button>
                <button className="close-button" onClick={handleCloseClick}>Close</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Guidelines;
