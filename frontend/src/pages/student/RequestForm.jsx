import React, { useState, useEffect, useContext } from "react";
import { AuthContext } from "../../context/AuthContext";
import axios from "axios";
import "./request.css";
import upload_area from "../../assets/upload_area.png";
import { toast, Toaster } from "react-hot-toast";

const RequestForm = () => {
  const { user, loading } = useContext(AuthContext);
 
  const [formData, setFormData] = useState({
    category: "",
    activity: "",
    activityName: "",
    date: "",
    organization: "",
    location: "",
    description: "",
    proof: null,
    
    "select FA": []
  });

  const [errors, setErrors] = useState({});


  


  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
  
    if (file) {
      if (file.type !== "application/pdf") {
        toast.error("Only PDF files are allowed!")
        return;
      }
      if (file.size >  1024 * 1024) { // 1 MB
        toast.error("File too large! Please upload under 1 MB.");
        return;
  }
      setFormData({ ...formData, proof: file }); // ✅ store file in state
      
      console.log("Selected file:", file.name);
      toast.success('Successfully uploaded!');
    }
  };
  

  const validateForm = () => {
    let errors = {};
    if (!formData.category) errors.category = "Activity category is required";
    
    
      if (!formData.activityName) errors.activityName = "Activity name is required";
      if (!formData.date) errors.date = "Date is required";
      if (!formData.organization) errors.organization = "Organization name is required";
      if (!formData.location) errors.location = "Location is required";
      if (!formData.description) errors.description = "Description is required";
      if (!formData.proof) errors.proof = "Proof document is required";
    console.log(errors);
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
        
        const formPayload = new FormData();
        formPayload.append("sid", sid);
        formPayload.append("date", new Date().toISOString());
        formPayload.append("status", "Pending");
      formPayload.append("decisionDate", new Date().toISOString());
        formPayload.append("activityName", formData.activityName);
        formPayload.append("description", formData.description);
        formPayload.append("activityDate", formData.date);
        formPayload.append("type", formData.category);
        faIds.forEach((id, index) => formPayload.append(`faIds[${index}]`, id));
        
        if (formData.proof) formPayload.append("proof", formData.proof);
       
        const response = await fetch(`http://localhost:8080/requests`, {
          method: "POST",
          body: formPayload, // no JSON, no headers → browser sets multipart
        });
        console.log("Form Payload:");
        for (let pair of formPayload.entries()) {
          console.log(pair[0], pair[1]);
        }
        console.log(response);
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
           
            "select FA": []
          });
        } else {
          
          const errorData = await response.json();
          toast.error(`Failed to submit request: ${errorData.message || "Unknown error"}`);
        }
      } catch (error) {
        console.error("Error submitting request:", error);
        toast.error("An error occurred while submitting the request.");
      }
    }
  };
  

  return (
    <div className="request-page">
     {formData.proof && <Toaster />}
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
            
              <>
                <div>
                  <input
                    type="text"
                    name="activityName"
                    placeholder="Enter activity name"
                    value={formData.activityName}
                    onChange={handleChange}
                    required={true}
                  />
                  {errors.activityName && <span className="error">{errors.activityName}</span>}
                </div>
                <div>
                  <input
                    type="date"
                    name="date"
                    value={formData.date}
                    onChange={handleChange}
                    required={true}
                  />
                  {errors.date && <span className="error">{errors.date}</span>}
                </div>
              </>
            
          </div>

        
            <div className="form-row">
              <div className="form-group">
                <div>
                  <input
                    type="text"
                    name="location"
                    placeholder="Location"
                    value={formData.location}
                    onChange={handleChange}
                    required={true}
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
                    required={true}
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
                    required={true}
                  />
                  {errors.description && <span className="error">{errors.description}</span>}
                </div>
              </div>
              <label htmlFor='file'>
              <img src={upload_area} alt="upload-area"  style={{cursor:"pointer"}}/>
            </label>
            <input 
  type="file" 
  name="proof"
 
  id="file" 
  accept=".pdf"    // restrict only PDFs
  onChange={handleFileChange}
  hidden 
  required 
/>
{formData.proof && (
  <p>📎 {formData.proof.name}</p>
)}


              
            </div>
          

          <button type="submit" className="submit-btn">
            Submit
          </button>
        </form>
      </main>
    </div>
  );
};

export default RequestForm;