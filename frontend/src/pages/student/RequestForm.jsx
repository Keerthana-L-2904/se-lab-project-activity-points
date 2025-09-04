import React, { useState, useEffect, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import axios from "axios";
import "./request.css";
import upload_area from "../../assets/upload_area.png";
import { toast, Toaster } from "react-hot-toast";

const RequestForm = () => {
  const { user } = useContext(AuthContext);
  const [activities, setActivities] = useState([]);
  const [isSubmitting,setIsSubmitting] =useState(false);
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
        const response = await axios.get("/api/admin/manage-activities");
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
        console.error("Error fetching activities", error);
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

  // 🔹 When activity selected → autofill other fields
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
        category: selectedActivity.type || "",
        organization: "NITC",
        location: selectedActivity.location || "Institute Campus",
        points: selectedActivity.points || "",   // <-- auto fill points
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

  // 🔹 Validation
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

  // 🔹 Submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validateForm();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length === 0) {
      try {
        const sid = user.sid;
        const formPayload = new FormData();

        setIsSubmitting(true);
        formPayload.append("sid", sid);
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


        if (formData.proof) {
          formPayload.append("proof", formData.proof);
        }

        const response = await fetch("http://localhost:8080/requests", {
          method: "POST",
          body: formPayload,
        });

        if (response.ok) {
          toast.success("Request Submitted Successfully!");
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
          });
        } else {
          const errorData = await response.json();
          toast.error(
            `Failed to submit request: ${errorData.message || "Unknown error"}`
          );
        }
      } catch (error) {
        console.error("Error submitting request:", error);
        toast.error("An error occurred while submitting the request.");
      }
      finally{
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
                onClick={() => setFormData({ ...formData, isCustomActivity: true })}
              >
                Not Listed?
              </button>
            ) : (
              <button
                type="button"
                className="close-btn"
                onClick={() =>
                  setFormData({
                    ...formData,
                    isCustomActivity: false,
                    activityName: "",
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
            disabled={!formData.isCustomActivity} // ⬅ disables only when auto-filled
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

          <button type="submit" className="submit-btn" disabled ={isSubmitting}>
            {isSubmitting ? "Submitting.." :"Submit"}
          </button>
        </form>
      </main>
    </div>
  );
};

export default RequestForm;
