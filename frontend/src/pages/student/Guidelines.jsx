import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './guidelines.css';

const Guidelines = () => {
  const [guidelines, setGuidelines] = useState([]);

  useEffect(() => {
    // Fetch guidelines from backend
    axios.get('http://localhost:8080/api/guidelines') // Adjust URL based on your backend setup
      .then(response => {
        setGuidelines(response.data);
      })
      .catch(error => {
        console.error('Error fetching guidelines:', error);
      });
  }, []);

  return (
    <div className="container-fluid">
      <div className="row">
        <div className="col-md-10 p-4 guidelines-container">
          <div className="guidelines-header">
            <h3>Activity Points Guidelines</h3>
          </div>

          <div className="card shadow-sm">
            <div className="card-body">
              <h5 className="card-title">Important Guidelines for Students</h5>
              
              <div className="guidelines-list">
                {guidelines.length > 0 ? (
                  guidelines.map((guideline, index) => (
                    <div className="guideline-item" key={guideline.gid}>
                      <p>{index + 1}. {guideline.body}</p>
                    </div>
                  ))
                ) : (
                  <p>Loading guidelines...</p>
                )}
              </div>

              <div className="alert-info">
                <i className="bi bi-info-circle me-2"></i>
                Note: Make sure to follow all guidelines strictly to ensure smooth processing of your activity points.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Guidelines;
