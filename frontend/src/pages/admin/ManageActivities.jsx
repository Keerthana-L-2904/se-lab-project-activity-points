import React, { useState, useEffect } from 'react';
import './admin.css';
import axiosInstance from "../../utils/axiosConfig";
import UploadStatusModal from '../../components/UploadStatusModal/UploadStatusModal';
import { toast, Toaster } from "react-hot-toast"; 

const ActivityManagement = () => {
  const [activities, setActivities] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isAddModalOpen, setAddModalOpen] = useState(false);
  const [editActivity, setEditActivity] = useState(null);
  
  // 🔹 Filters
  const [typeFilter, setTypeFilter] = useState('');
  const [mandatoryFilter, setMandatoryFilter] = useState('');
  const [deptFilter, setDeptFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [newActivity, setNewActivity] = useState({
    name: '',
    type: '',
    mandatory: '',
    did: '',
    description: '',   
    date: '',
    end_date: '',
    points: '',
  });

  const [attendanceUploaded, setAttendanceUploaded] = useState({});
  const [uploadstatus, setuploadstatus] = useState(null);

  // 🔹 Pagination
  const [activityPage, setActivityPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    fetchData();
    getDeptData();
  }, [mandatoryFilter, typeFilter]); 

  const handleAttendance = (actid) => {
    setuploadstatus({actid, isOpen: true, onClose: () => setuploadstatus(null)});
  };

  const fetchData = async () => {
    try {
      const params = {};
      if (mandatoryFilter) params.mandatory = mandatoryFilter;
      const response = await axiosInstance.get(
        "/admin/manage-activities",
        { params }
      );

      if (response.status === 200) {
        const today = new Date();
        const uploadedMap = {};
        const updatedActivities = response.data.map(activity => {
          const startDate = new Date(activity.date || activity.start_date);
          const endDate = new Date(activity.end_date);
          let status = "Upcoming";
          if (today >= startDate && today <= endDate) {
            status = "Ongoing";
          } else if (today > endDate) {
            status = "Completed";
          }
          uploadedMap[activity.actID] = !!activity.isuploaded;
          return { ...activity, status };
        });
        setAttendanceUploaded(uploadedMap);
        setActivities(updatedActivities);
      } else {
        toast.error('Error loading activities!');
      }
    } catch (error) {
      toast.error('Failed to fetch activities!');
    }
  };

  const getDeptData = async () => {
    try {
      const response = await axiosInstance.get("/admin/get-departments");

      if (response.status === 200) {
        setDepartments(response.data);
      } else {
        console.log('Error loading departments!');
      }
    } catch (error) {
      console.error('Error fetching departments', error);
    }
  };

  // 🔹 Filtering
  const filteredActivities = activities
    .filter(activity =>
      activity.name.toLowerCase().includes(searchQuery.toLowerCase())
    )
    .filter(activity =>
      typeFilter ? activity.type.toLowerCase().includes(typeFilter.toLowerCase()) : true
    )
    .filter(activity => {
      if (!deptFilter) return true;
      const dept = departments.find(d => 
        String(d.did) === String(activity.DID || activity.did)
      );
      const deptName = dept ? dept.name : "N/A";
      return deptName.toLowerCase().includes(deptFilter.toLowerCase());
    })
    .filter(activity =>
      statusFilter ? activity.status.toLowerCase().includes(statusFilter.toLowerCase()) : true
    )
    .sort((a, b) => new Date(b.date) - new Date(a.date));

  // 🔹 Paginate
  const startIndex = (activityPage - 1) * itemsPerPage;
  const paginatedActivities = filteredActivities.slice(startIndex, startIndex + itemsPerPage);

  const handleAddActivity = async () => {
    try {
      const response = await axiosInstance.post("/admin/manage-activities", newActivity);

      if (response.status === 200) {
        toast.success("Activity added successfully!");
        fetchData();
        setAddModalOpen(false);
        setNewActivity({
          name: '',
          type: '',
          mandatory: '',
          did: '',
          description: '',
          date: '',
          end_date: '',
          points: '',
        });
      } else {
        toast.error("Error adding activity!");
      }
    } catch (error) {
      toast.error("Failed to add activity!");
    }
  };

  const handleUpdate = async () => {
    try {
      const response = await axiosInstance.put(
        `/admin/manage-activities/${editActivity.actID}`,
        editActivity
      );

      if (response.status === 200) {
        toast.success("Activity updated successfully!");
        fetchData();
        setIsEditModalOpen(false);
      } else {
        toast.error("Error updating activity!");
      }
    } catch (error) {
      toast.error("Failed to update activity!");
    }
  };

  const handleDelete = async (id) => {
    try {
          const response = await axiosInstance.delete(
          `/admin/manage-activities/${id}`
        );

      if (response.status === 200) {
        toast.success("Activity deleted successfully!");
        fetchData();
      } else {
        toast.error("Error deleting activity!");
      }
    } catch (error) {
      toast.error("Failed to delete activity!");
    }
  };

  const handleEdit = (activity) => {
    setEditActivity(activity);
    setIsEditModalOpen(true);
  };

  // 🔹 Bulk Upload/Delete Refs
  const fileUploadRef = React.useRef(null);
  const fileDeleteRef = React.useRef(null);

  const handleBulkUploadClick = () => fileUploadRef.current.click();
  const handleBulkDeleteClick = () => fileDeleteRef.current.click();

  const handleBulkUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
  
    const formData = new FormData();
    formData.append("file", file);
  
    try {
      const response = await axiosInstance.post(
      "/admin/bulk-upload-activities",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

  
      toast.success(response.data);
      fetchData();
  
    } catch (err) {
      let errorMessage = "Failed to upload activities ❌";
  
      if (err.response) {
        errorMessage =
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data?.message || "Failed to upload activities";
  
      } else if (err.request) {
        errorMessage = "Server not responding. Please try again later.";
  
      } else {
        errorMessage = err.message;
      }
  
      toast.error(errorMessage);
  
    } finally {
      event.target.value = null;
    }
  };
  
  const handleBulkDelete = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
  
    const formData = new FormData();
    formData.append("file", file);
  
    try {
      const response = await axiosInstance.post(
      "/admin/bulk-delete-activities",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

      toast.success(response.data);
      fetchData();
  
    } catch (err) {
      let errorMessage = "Failed to delete activities ❌";
  
      if (err.response) {
        errorMessage =
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data?.message || "Failed to delete activities";
  
      } else if (err.request) {
        errorMessage = "Server not responding. Please try again later.";
  
      } else {
        errorMessage = err.message;
      }
  
      toast.error(errorMessage);
  
    } finally {
      event.target.value = null;
    }
  };
  
  
  return (
    <div className="content"> <Toaster />
      <div className="header">
        <h1>Activity Management</h1>
        <div className="search-add">
          <button className="Add" onClick={() => setAddModalOpen(true)}>Add Activity</button>

          {/* Bulk Upload */}
          <button className="Add" onClick={handleBulkUploadClick}>
            Add Activity in Bulk
          </button>
          <input
            type="file"
            accept=".xlsx,.xls"
            ref={fileUploadRef}
            style={{ display: "none" }}
            onChange={handleBulkUpload}
          />

          {/* Bulk Delete */}
          <button className="Add" onClick={handleBulkDeleteClick}>
            Delete Activity in Bulk
          </button>
          <input
            type="file"
            accept=".xlsx,.xls"
            ref={fileDeleteRef}
            style={{ display: "none" }}
            onChange={handleBulkDelete}
          />
        </div>
      </div>

      <div className="body">
        <table className="styled-table">
          <thead>
            <tr>
              <th>Activity Name
                <div className="fil-sec">
                  <input
                    type="text"
                    placeholder="Enter activity name"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
              </th>
              <th>Type
                <div className="fil-sec">
                  <input
                    type="text"
                    placeholder="Enter Type"
                    value={typeFilter}
                    onChange={(e) => setTypeFilter(e.target.value)}
                  />
                </div>
              </th>
              <th>Mandatory
                <div className="fil-sec">
                  <input
                    type="text"
                    placeholder="Enter Mandatory"
                    value={mandatoryFilter}
                    onChange={(e) => setMandatoryFilter(e.target.value)}
                  />
                </div>
              </th>
              <th>Dept
                <div className="fil-sec">
                  <input
                    type="text"
                    placeholder="Enter Dept name"
                    value={deptFilter}
                    onChange={(e) => setDeptFilter(e.target.value)}
                  />
                </div>
              </th>
              <th>Description</th>
              <th>Start date</th>
              <th>End date</th>
              <th>Points</th>
              <th>Status
                <div className="fil-sec">
                  <input
                    type="text"
                    placeholder="Enter Status"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                  />
                </div>
              </th>
              <th>Attendance list</th>
              <th>Actions</th>
            </tr>
          </thead>
        <tbody>
        {paginatedActivities.map(activity => {
          const dept = departments.find(d => String(d.did) === String(activity.DID || activity.did));

          return (
            <tr key={activity.actID}>
              <td>{activity.name}</td>
              <td>{activity.type}</td>
              <td>{activity.mandatory === 1 ? "Yes" : "No"}</td>
              <td>{dept ? dept.name : "N/A"}</td>
              <td>{activity.description && activity.description.length > 20 ? `${activity.description.substring(0, 20)}...` : activity.description}</td>
              <td>{new Date(activity.date).toLocaleDateString('en-GB')}</td>
              <td>{new Date(activity.end_date).toLocaleDateString('en-GB')}</td>
              <td>{activity.points}</td>
              <td>{activity.status}</td>
              <td>
                {activity.status === "Completed" ? (
                  activity.isuploaded ? (
                    <span style={{ color: 'green', fontWeight: 'bold' }}>Uploaded</span>
                  ) : (
                    <button onClick={() => handleAttendance(activity.actID)}>
                      Upload Attendance
                    </button>
                  )
                ) : "N/A"}
              </td>
              <td>
                <i className="bi bi-pencil-fill" onClick={() => handleEdit(activity)}></i>
                <i className="bi bi-trash-fill" onClick={() => handleDelete(activity.actID)}></i>
              </td>
            </tr>
          );
        })}
          </tbody>
        </table>

        {/* 🔹 Pagination */}
        <div className="pagination" style={{ display: "flex", justifyContent: "center", marginTop: "20px"}}>
          {Array.from({ length: Math.ceil(filteredActivities.length / itemsPerPage) }, (_, i) => (
            <button
              key={i}
              className={activityPage === i + 1 ? "active" : ""}
              onClick={() => setActivityPage(i + 1)}
            >
              {i + 1}
            </button>
          ))}
        </div>
      </div>

      {/* Edit Modal */}
      {isEditModalOpen && (
        <div className="modal-overlay" onClick={() => setIsEditModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Edit Activity</h2>
            
            <label>Name:</label>
            <input type="text" value={editActivity.name} onChange={(e) => setEditActivity({...editActivity, name: e.target.value})} />
            
            <label>Type:</label>
            <select value={editActivity.type} onChange={(e) => setEditActivity({...editActivity, type: e.target.value})}>
              <option value="">Select Type</option>
              <option value="Institute">Institute</option>
              <option value="Department">Department</option>
              <option value="Other">Other</option>
            </select>
            <label>Points:</label>
              <input type="text" value={editActivity.points} onChange={(e) => setEditActivity({...editActivity, points: e.target.value})} />
            
            <label>Start Date:</label>
            <input type="date" value={editActivity.date} onChange={(e) => setEditActivity({...editActivity, date: e.target.value})} />
            
            <label>End Date:</label>
            <input type="date" value={editActivity.end_date || ""} onChange={(e) => setEditActivity({...editActivity, end_date: e.target.value})} />
            
            <label>Department:</label>
            <select value={editActivity.did } onChange={(e) => setEditActivity({...editActivity, did: e.target.value})}>
              <option value="">Select Department</option>
              {departments.map(dept => (
                <option key={dept.did} value={dept.did}>{dept.name}</option>
              ))}
            </select>
            
            <label>Mandatory:</label>
            <input type="text" value={editActivity.mandatory} onChange={(e) => setEditActivity({...editActivity, mandatory: e.target.value})} />
            
            <label>Description:</label>
            <input type="text" value={editActivity.description} onChange={(e) => setEditActivity({...editActivity, description: e.target.value})} />
            <button onClick={handleUpdate}>Submit</button>
          </div>
        </div>
      )}

      {uploadstatus && (
        <UploadStatusModal 
          actid={uploadstatus.actid}
          isOpen={uploadstatus.isOpen}
          onClose={uploadstatus.onClose}
          onSuccess={() => {
            // Refresh your data here
            fetchData();
            getDeptData(); // or whatever function fetches your activity list
          }}
        />
      )}
    
      {/* Add Modal */}
      {isAddModalOpen && (
        <div className="modal-overlay" onClick={() => setAddModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Add New Activity</h2>
            
            <label>Name:</label>
            <input type="text" value={newActivity.name} onChange={(e) => setNewActivity({...newActivity, name: e.target.value})} />
            
            <label>Type:</label>
            <select value={newActivity.type} onChange={(e) => setNewActivity({...newActivity, type: e.target.value})}>
              <option value="">Select Type</option>
              <option value="Institute">Institute</option>
              <option value="Department">Department</option>
              <option value="Other">Other</option>
            </select>
            
            <label>Date:</label>
            <input type="date" value={newActivity.date} onChange={(e) => setNewActivity({...newActivity, date: e.target.value})} />
            
            <label>End Date:</label>
            <input type="date" value={newActivity.end_date} onChange={(e) => setNewActivity({...newActivity, end_date: e.target.value})} />
            
            <label>Mandatory:</label>
            <select value={newActivity.mandatory} onChange={(e) => setNewActivity({...newActivity, mandatory: e.target.value})}>
              <option value="">Select value</option>
              <option key={1} value={1}>Yes</option>
              <option key={0} value={0}>No</option>
            </select>
            
            <label>Description:</label>
            <input type="text" value={newActivity.description} onChange={(e) => setNewActivity({...newActivity, description: e.target.value})} />
            
            <label>Department:</label>
            <select value={newActivity.did} onChange={(e) => setNewActivity({...newActivity, did: e.target.value})}>
              <option value="">Select Department</option>
              {departments.map(dept => (
                <option key={dept.did} value={dept.did}>{dept.name}</option>
              ))}
            </select>
            
            <label>Points:</label>
            <input type="text" value={newActivity.points} onChange={(e) => setNewActivity({...newActivity, points: e.target.value})} />
            <button onClick={handleAddActivity}>Add</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ActivityManagement;
