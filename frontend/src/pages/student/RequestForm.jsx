import React, { useState, useEffect, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import axiosInstance from "../../utils/axiosConfig";
import "./request.css";
import upload_area from "../../assets/upload_area.png";
import { toast, Toaster } from "react-hot-toast";

const RequestForm = () => {
  
  const { user } = useContext(AuthContext);
  const [activities, setActivities] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    category: "",
    activity: "",
    activityName: "",
    date: "",
    organization: "",
    location: "",
    description: "",
    proof: null,
    isCustomActivity: false,
    points: "",  
  });
  const [errors, setErrors] = useState({});

  // 🔹 Fetch activities
  useEffect(() => {
    const fetchActivityData = async () => {
      try {
        const response = await axiosInstance.get(
          "/student/manage-activities"
        );

        if (response.status === 200) {
          const today = new Date();
          const updatedActivities = response.data.map((activity) => {
            const startDate = new Date(activity.start_date);
            const endDate = new Date(activity.end_date);

            let status = "Upcoming";
            if (today >= startDate && today <= endDate) {
              status = "Ongoing";
            } else if (today > endDate) {
              status = "Completed";
            }
            return { ...activity, status };
          });

          setActivities(updatedActivities);
        } else {
          toast.error("Error loading activities!");
        }
      } catch (error) {
        toast.error("Failed to fetch activities!");
      }
    };

    fetchActivityData();
  }, []);

  // 🔹 Generic field change
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleActivitySelect = (e) => {
    const selectedActivityName = e.target.value;
    setFormData((prev) => ({ ...prev, activity: selectedActivityName }));

    const selectedActivity = activities.find(
      (act) => act.name === selectedActivityName
    );

    if (selectedActivity) {
      setFormData((prev) => ({
        ...prev,
        activity: selectedActivity.name,
        activityName: selectedActivity.name,
        date: selectedActivity.start_date
          ? new Date(selectedActivity.start_date).toISOString().split("T")[0]
          : "",
        description: selectedActivity.description || "",
        category:
          selectedActivity.type === "Other"
            ? "Other"
            : selectedActivity.type || "",
        organization: "NITC",
        location: selectedActivity.location || "Institute Campus",
        points: selectedActivity.points || "",
        isCustomActivity: false,
      }));
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.type !== "application/pdf") {
        toast.error("Only PDF files are allowed!");
        return;
      }
      if (file.size > 1024 * 1024) {
        toast.error("File too large! Please upload under 1 MB.");
        return;
      }
      setFormData({ ...formData, proof: file });
      toast.success("File uploaded successfully!");
    }
  };

  const validateForm = () => {
    let errors = {};
    if (!formData.isCustomActivity && !formData.activity.trim())
      errors.activity = "Activity selection is required";
    if (!formData.proof) errors.proof = "Proof document is required";

    if (formData.isCustomActivity) {
      if (!formData.category) errors.category = "Activity category is required";
      if (!formData.activityName) errors.activityName = "Activity name is required";
      if (!formData.date) errors.date = "Date is required";
      if (!formData.organization) errors.organization = "Organization name is required";
      if (!formData.location) errors.location = "Location is required";
      if (!formData.description) errors.description = "Description is required";
    }
    return errors;
  };

  // 🔹 Submit - FIXED
  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validateForm();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length === 0) {
      try {
        const sid = user.sid;
        const formPayload = new FormData();

        setIsSubmitting(true);
        
        // ✅ Append all form fields
        formPayload.append("sid", sid); // Use actual user sid, not hardcoded
        formPayload.append("date", new Date().toISOString());
        formPayload.append("status", "Pending");
        formPayload.append("decisionDate", new Date().toISOString());
        formPayload.append(
          "activityName",
          formData.isCustomActivity ? formData.activityName : formData.activityName
        );
        formPayload.append("description", formData.description);
        formPayload.append("activityDate", formData.date);
        formPayload.append("type", formData.category);
        formPayload.append("organization", formData.organization);
        formPayload.append("location", formData.location);
        formPayload.append("points", formData.points);

        // ✅ Append the file
        if (formData.proof) {
          formPayload.append("proof", formData.proof);
        }

        // ✅ FIXED: Use axios with proper headers
        const response = await axiosInstance.post("/requests", formPayload, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        // ✅ FIXED: Axios uses response.status, not response.ok
        if (response.status === 200 || response.status === 201) {
          toast.success("Request Submitted Successfully!");
          
          // Reset form
          setFormData({
            category: "",
            activity: "",
            activityName: "",
            date: "",
            organization: "",
            location: "",
            description: "",
            proof: null,
            isCustomActivity: false,
            points: "",
          });
          
          // Reset file input
          const fileInput = document.getElementById('file');
          if (fileInput) fileInput.value = '';
          
        } else {
          const errorMsg = response.data?.message || "Unknown error";
          toast.error(`Failed to submit request: ${errorMsg}`);
        }
        
      } catch (error) {
        const errorMsg = error.response?.data?.message || 
                        error.response?.data || 
                        error.message || 
                        "An error occurred while submitting the request";
        
        toast.error(errorMsg);
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  return (
    <div className="request-page">
      <Toaster />
      <main className="request-form-container">
        <h2 className="title" style={{textTransform:"uppercase"}}>Request for Points</h2>

        <form onSubmit={handleSubmit}>
          {/* ACTIVITY SELECTION */}
          {!formData.isCustomActivity && (
            <div className="form-group">
              <select
                name="activity"
                value={formData.activity}
                onChange={handleActivitySelect}
              >
                <option value="">Select an activity</option>
                {activities.map((activity) => (
                  <option key={activity.name}>{activity.name}</option>
                ))}
              </select>
              {errors.activity && <span className="error">{errors.activity}</span>}
            </div>
          )}

          {/* SWITCH TO NOT LISTED */}
          <div className="url-and-not-listed">
            {!formData.isCustomActivity ? (
            <button
              type="button"
              className="not-listed-btn"
              onClick={() =>
                setFormData({
                  category: "",
                  activity: "",
                  activityName: "",
                  date: "",
                  organization: "",
                  location: "",
                  description: "",
                  proof: null,
                  isCustomActivity: true,
                  points: "",
                })
              }
            >
              Not Listed?
            </button>
          ) : (
            <button
              type="button"
              className="close-btn"
              onClick={() =>
                setFormData({
                  category: "",
                  activity: "",
                  activityName: "",
                  date: "",
                  organization: "",
                  location: "",
                  description: "",
                  proof: null,
                  isCustomActivity: false,
                  points: "",
                })
              }
            >
              Back to Listed
            </button>
          )}
          </div>

        {/* CATEGORY */}
        <div className="form-group">
          <p>Activity Category:</p> 
          <div className="radio-group">
            <label>
              <input
                type="radio"
                name="category"
                value="Institute"
                checked={formData.category === "Institute"}
                onChange={handleChange}
                disabled={!formData.isCustomActivity}
              />
              Institutional
            </label>
            <label>
              <input
                type="radio"
                name="category"
                value="Department"
                checked={formData.category === "Department"}
                onChange={handleChange}
                disabled={!formData.isCustomActivity}
              />
              Departmental
            </label>
            <label>
              <input
                type="radio"
                name="category"
                value="Other"
                checked={formData.category === "Other"}
                onChange={handleChange}
                disabled={!formData.isCustomActivity}
              />
              Other
            </label>
          </div>
          {errors.category && <span className="error">{errors.category}</span>}
        </div>

          {/* ACTIVITY NAME (only when custom) */}
          {formData.isCustomActivity && (
            <div>
              <p>Activity Name:</p>
              <input
                type="text"
                name="activityName"
                placeholder="Enter activity name"
                value={formData.activityName}
                onChange={handleChange}
              />
              {errors.activityName && (
                <span className="error">{errors.activityName}</span>
              )}
            </div>
          )}
          
          {/* DATE */}
          <div>
            <p>Activity Date:</p>
            <input
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              max={new Date().toISOString().split("T")[0]}
              required={true}
            />
            {errors.date && <span className="error">{errors.date}</span>}
          </div>
          
          {/* POINTS */}
          <input
            type="number"
            name="points"
            placeholder="Points"
            value={formData.points}
            onChange={handleChange}
            disabled={!formData.isCustomActivity}
          />

          {/* LOCATION */}
          <div>
            <p>Location:</p>
            <input
              type="text"
              name="location"
              placeholder="Location"
              value={formData.location}
              onChange={handleChange}
            />
            {errors.location && <span className="error">{errors.location}</span>}
          </div>

          {/* ORGANIZATION */}
          <div>
            <p>Organization:</p>
            <input
              type="text"
              name="organization"
              placeholder="Name of Organisation"
              value={formData.organization}
              onChange={handleChange}
            />
            {errors.organization && (
              <span className="error">{errors.organization}</span>
            )}
          </div>

          {/* DESCRIPTION */}
          <div>
            <p>Description:</p>
            <textarea
              name="description"
              placeholder="Description of the event"
              value={formData.description}
              onChange={handleChange}
            />
            {errors.description && (
              <span className="error">{errors.description}</span>
            )}
          </div>

          {/* FILE UPLOAD */}
          <div className="form-group">
            <p>Upload File:</p>
            <label htmlFor="file">
              <img
                src={upload_area}
                alt="upload-area"
                style={{ cursor: "pointer" }}
              />
            </label>
            <input
              type="file"
              name="proof"
              id="file"
              accept=".pdf"
              onChange={handleFileChange}
              hidden
            />
            {formData.proof && <p>📎 {formData.proof.name}</p>}
            {errors.proof && <span className="error">{errors.proof}</span>}
          </div>

          <button type="submit" className="submit-btn" disabled={isSubmitting}>
            {isSubmitting ? "Submitting..." : "Submit"}
          </button>
        </form>
      </main>
    </div>
  );
};

export default RequestForm;