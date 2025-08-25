import React, { useState, useEffect, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import axios from "axios";
import "./request.css";
import upload_area from "../../assets/upload_area.png";

const RequestForm = () => {
  const { user, loading } = useContext(AuthContext);
  const [activities, setActivities] = useState([]);
  const [formData, setFormData] = useState({
    category: "",
    activity: "",
    activityName: "",
    date: "",
    organization: "",
    location: "",
    description: "",
    pastUrl: "",
    isCustomActivity: false,
    "select FA": []
  });
  const [faculties, setFaculties] = useState([]);
  const [errors, setErrors] = useState({});
  const [image,setimage]=useState(false);

  const fetchActivityData = async () => {
    try {
      const response = await axios.get("/api/admin/manage-activities");
      if (response.status === 200) {
        const today = new Date();
        const updatedActivities = response.data.map(activity => {
          const startDate = new Date(activity.start_date); // Make sure your backend sends a start_date field
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
        alert('Error loading activities!');
      }
    } catch (error) {
      console.error('Error fetching activities', error);
      alert('Failed to fetch activities!');
    }
  };
  

  // Fetch FA data from API
  useEffect(() => {
    const fetchFAData = async () => {
      try {
        const response = await axios.get("/api/admin/manage-users/fa");
        if (response.status === 200) {
          setFaculties(response.data);
        }
      } catch (error) {
        console.error('Error fetching FA data:', error);
        alert('Failed to load faculty advisors');
      }
    };
    fetchFAData();
    fetchActivityData();
  }, []);



  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const resetCustomActivity = () => {
    setFormData({
      ...formData,
      isCustomActivity: false,
      activityName: "",
      date: "",
      organization: "",
      location: "",
      description: "",
      pastUrl: ""
    });
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
  
    if (file) {
      if (file.type !== "application/pdf") {
        alert("Only PDF files are allowed!");
        return;
      }
  
      // if valid, you can preview filename or send it to backend
      console.log("Selected file:", file.name);
    }
  };
  

  const validateForm = () => {
    let errors = {};
    if (!formData.category) errors.category = "Activity category is required";
    if (!formData.isCustomActivity && !formData.activity.trim())
      errors.activity = "Activity selection is required";
    if (formData.isCustomActivity) {
      if (!formData.activityName) errors.activityName = "Activity name is required";
      if (!formData.date) errors.date = "Date is required";
      if (!formData.organization) errors.organization = "Organization name is required";
      if (!formData.location) errors.location = "Location is required";
      if (!formData.description) errors.description = "Description is required";
    }
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validateForm();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length === 0) {
      try {
        const sid = user.sid;
        const faIds = formData["select FA"].map(v => parseInt(v, 10));
        // console.log(user.sid)
        const payload = {
          sid: sid,
          date: new Date(),
          status: "Pending",
          link: formData.pastUrl,
          decisionDate: new Date(),
          activityName: formData.isCustomActivity ? formData.activityName : formData.activity,
          description: formData.description,
          activityDate: formData.date,
          type: formData.category,
          pastUrl: formData.pastUrl,
          faIds: faIds,
           
        };

        const response = await fetch("http://localhost:8080/requests", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        if (response.ok) {
          alert("Request Submitted Successfully!");
          setFormData({
            category: "",
            activity: "",
            activityName: "",
            date: "",
            organization: "",
            location: "",
            description: "",
            pastUrl: "",
            isCustomActivity: false,
            "select FA": []
          });
        } else {
          const errorData = await response.json();
          alert(`Failed to submit request: ${errorData.message || "Unknown error"}`);
        }
      } catch (error) {
        console.error("Error submitting request:", error);
        alert("An error occurred while submitting the request.");
      }
    }
  };

  return (
    <div className="request-page">
      <main className="request-form-container">
          <h2 className="title" >Request for Points</h2>
        <div>
          <p>Activity Category:</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
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

          
            
           

          <div className="form-group">
            {!formData.isCustomActivity ? (
              <>
                <select
                  name="activity"
                  value={formData.activity}
                  onChange={handleChange}
                >
                  <option value="">Select an activity</option>
                  {activities.map(activity => (<option key={activity.name} >{activity.name}</option>)
                    )}
                </select>
                {errors.activity && <span className="error">{errors.activity}</span>}
                <label htmlFor='file'>
              <img src={image==false?upload_area:image} alt="upload-area"   style={{cursor:"pointer"}}/>
            </label>
            <input 
  type="file" 
  id="file" 
  accept=".pdf"    // restrict only PDFs
  onChange={handleFileChange}
  hidden 
  required 
/>
                <div className="url-and-not-listed">
               
                  <button
                    type="button"
                    className="not-listed-btn"
                    onClick={() => setFormData({ ...formData, isCustomActivity: true })}
                  >
                    Not Listed?
                  </button>
                </div>
              </>
            ) : (
              <>
                <div>
                  <input
                    type="text"
                    name="activityName"
                    placeholder="Enter activity name"
                    value={formData.activityName}
                    onChange={handleChange}
                  />
                  {errors.activityName && <span className="error">{errors.activityName}</span>}
                </div>
                <div>
                  <input
                    type="date"
                    name="date"
                    value={formData.date}
                    onChange={handleChange}
                  />
                  {errors.date && <span className="error">{errors.date}</span>}
                </div>
              </>
            )}
          </div>

          {formData.isCustomActivity && (
            <div className="form-row">
              <div className="form-group">
                <div>
                  <input
                    type="text"
                    name="location"
                    placeholder="Location"
                    value={formData.location}
                    onChange={handleChange}
                  />
                  {errors.location && <span className="error">{errors.location}</span>}
                </div>
                <div>
                  <input
                    type="text"
                    name="organization"
                    placeholder="Name of Organisation"
                    value={formData.organization}
                    onChange={handleChange}
                  />
                  {errors.organization && <span className="error">{errors.organization}</span>}
                </div>
              </div>
              <div className="form-description">
                <div>
                  <textarea
                    name="description"
                    placeholder="Description of the event"
                    value={formData.description}
                    onChange={handleChange}
                  />
                  {errors.description && <span className="error">{errors.description}</span>}
                </div>
              </div>
              <label htmlFor='file'>
              <img src={upload_area} alt="upload-area"  style={{cursor:"pointer"}}/>
            </label>
            <input 
  type="file" 
  id="file" 
  accept=".pdf"    // restrict only PDFs
  onChange={handleFileChange}
  hidden 
  required 
/>

              <div className="close">
                <button type="button" className="close-btn" onClick={resetCustomActivity}>
                  Close
                </button>
              </div>
            </div>
          )}

          <button type="submit" className="submit-btn">
            Submit
          </button>
        </form>
      </main>
    </div>
  );
};

export default RequestForm;