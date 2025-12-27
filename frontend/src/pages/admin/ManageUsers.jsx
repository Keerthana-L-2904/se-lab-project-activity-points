import React, { useState, useEffect } from "react";
import axiosInstance from "../../utils/axiosConfig";
import "./manage.css";
import { PiStudent } from "react-icons/pi";
import { GiTeacher } from "react-icons/gi";
import { toast, Toaster } from "react-hot-toast"; 

const UserManagement = () => {
  const [students, setStudents] = useState([]);
  const [fas, setFas] = useState([]);
  const [filterFaName, setFilterFaName] = useState("");
  const [file, setFile] = useState(null);
  const [faFile, setFaFile] = useState(null);
  const [filterYear, setFilterYear] = useState("");
  const [filterDept, setFilterDept] = useState("");
  const [filterDeptFa, setFilterDeptFa] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingDelete, setLoadingDelete] = useState(false);
  const [page, setPage] = useState(1);       
  const [faPage, setFaPage] = useState(1);   
  const [view, setView] = useState(""); 
  const [editStudent, setEditStudent] = useState(null);
 
  const itemsPerPage = 10;

  // File handlers
const handleStudentFileChange = (e) => setFile(e.target.files[0]);
const handleFaFileChange = (e) => setFaFile(e.target.files[0]);
// New states
const [loadingStudents, setLoadingStudents] = useState(false);
const [loadingFas, setLoadingFas] = useState(false);

const uploadStudents = async () => {
  if (!file) {
    document.getElementById("student-file").click();
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  setLoadingStudents(true);

  try {
    const response = await axiosInstance.post(
      "/api/admin/manage-users/upload-students",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

    // ✅ SHOW BACKEND MESSAGE
    toast.success(response.data);
    setFile(null);
    fetchStudents();

  } catch (err) {
    let errorMessage = "Something went wrong ❌";

    if (err.response) {
      errorMessage =
        typeof err.response.data === "string"
          ? err.response.data
          : err.response.data?.message || "Upload failed";
    } else if (err.request) {
      errorMessage = "Server not responding. Please try again later.";
    } else {
      errorMessage = err.message;
    }

    toast.error(errorMessage);

  } finally {
    setLoadingStudents(false);
  }
};

// Upload FAs
const uploadFas = async () => {
  if (!faFile) {
    document.getElementById("fa-file").click();
    return;
  }

  const formData = new FormData();
  formData.append("file", faFile);

  setLoadingFas(true);

  try {
    const response = await axiosInstance.post(
      "/api/admin/manage-users/upload-fas",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );


    // ✅ SHOW BACKEND MESSAGE
    toast.success(response.data);
    setFaFile(null);
    fetchFas();

  } catch (err) {

    let errorMessage = "Something went wrong ❌";

    if (err.response) {
      errorMessage =
        typeof err.response.data === "string"
          ? err.response.data
          : err.response.data?.message || "FA upload failed";
    } else if (err.request) {
      errorMessage = "Server not responding. Please try again later.";
    } else {
      errorMessage = err.message;
    }

    toast.error(errorMessage);

  } finally {
    setLoadingFas(false);
  }
};

  // Fetch data
  const fetchStudents = async () => {
    try {
      const res = await axiosInstance.get("/api/admin/manage-users/student");

      setStudents(res.data);
      setView("students");
      setPage(1);
    } catch (err) {
      toast.error("Error fetching students ❌");
    }
  };
  const fetchFas = async () => {
    try {
      const res = await axiosInstance.get("/api/admin/manage-users/fa");

      setFas(res.data);
      setView("fas");
      setFaPage(1);
    } catch (err) {
      toast.error("Error fetching FAs ❌");
    }
  };

  // Delete handlers
  const deleteStudent = async (sid) => {
    if (!window.confirm("Are you sure you want to delete this student?")) return;
    try {
      await axiosInstance.delete(
      `/api/admin/manage-users/student/${sid}`
    );

      setStudents(students.filter((s) => s.sid !== sid));
    } catch (err) {
      toast.error("Error deleting student ❌");
    }
  };

  // Bulk delete students
  const bulkDeleteStudents = async () => {
    if (loadingDelete) return;
  
    const fileInput = document.createElement("input");
    fileInput.type = "file";
    fileInput.accept = ".xlsx,.xls";
  
    fileInput.onchange = async (e) => {
      const deleteFile = e.target.files[0];
      if (!deleteFile) return;
  
      const formData = new FormData();
      formData.append("file", deleteFile);
  
      setLoadingDelete(true);
  
      try {
        const response = await axiosInstance.post(
        "/api/admin/manage-users/students/bulk-delete",
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
      );

  
        // ✅ SHOW BACKEND MESSAGE
        toast.success(response.data);
        fetchStudents();
  
      } catch (err) {
  
        let errorMessage = "Bulk delete failed";
  
        if (err.response) {
          errorMessage =
            typeof err.response.data === "string"
              ? err.response.data
              : err.response.data?.message || "Bulk delete failed";
        } else if (err.request) {
          errorMessage = "Server not responding. Please try again later.";
        } else {
          errorMessage = err.message;
        }
  
        toast.error(errorMessage);
  
      } finally {
        setLoadingDelete(false);
      }
    };
  
    fileInput.click();
  };
  


  // Update handlers
  const updateStudent = async () => {
    try {
        const res = await axiosInstance.put(
        `/api/admin/manage-users/student/${editStudent.sid}`,
        editStudent
      );

      setStudents(
        students.map((s) => (s.sid === editStudent.sid ? res.data : s))
      );
      setEditStudent(null);
    } catch (err) {
      toast.error("Error updating student ❌");
    }
  };

  // Filters
  const filteredStudents = students.filter((s) => {
  const yearMatch = filterYear
    ? s.sid.substring(1, 3) === filterYear
    : true;

  const deptMatch = filterDept
    ? s.sid.slice(-2).toUpperCase() === filterDept.toUpperCase()
    : true;

  const faMatch = filterFaName
    ? s.faName?.toLowerCase().includes(filterFaName.toLowerCase())
    : true;

  return yearMatch && deptMatch && faMatch;
  });


  const filteredFas = fas.filter((fa) => {
  const deptMatch = filterDeptFa
    ? fa.department?.name?.toLowerCase().includes(filterDeptFa.toLowerCase())
    : true;

  const nameMatch = filterFaName
    ? fa.name?.toLowerCase().includes(filterFaName.toLowerCase())
    : true;

  return deptMatch && nameMatch;
  });


  // Paginated arrays
  const paginatedStudents = filteredStudents.slice(
    (page - 1) * itemsPerPage,
    page * itemsPerPage
  );

  const paginatedFas = filteredFas.slice(
    (faPage - 1) * itemsPerPage,
    faPage * itemsPerPage
  );

  return (
    <div className="management-container"><Toaster />
      <h2 style={{ textTransform: "uppercase", textAlign: "center" }}>Admin - Manage Users</h2>

      {/* Upload Section */}
      <div className="upload-section-container">
        <div className="see-all-column">
          <button onClick={fetchFas} className="see-all-btn">See All FAs</button>
          <button onClick={fetchStudents} className="see-all-btn">See All Students</button>
        </div>
        <div className="tot-upload">
          <div className="upload-section">
              <label><PiStudent size={50} /></label>
              <input type="file" id="student-file" accept=".xlsx,.xls" hidden onChange={handleStudentFileChange} />
              {file && <p>{file.name}</p>}
              <button onClick={uploadStudents} disabled={loadingStudents}>
                {loadingStudents ? "Uploading..." : file ? "Upload Students File" : "Add Students in Bulk"}
              </button>
            </div>
          
            <div className="upload-section">
              <label><GiTeacher size={50} /></label>
              <input type="file" id="fa-file" accept=".xlsx,.xls" hidden onChange={handleFaFileChange} />
              {faFile && <p>{faFile.name}</p>}
              <button onClick={uploadFas} disabled={loadingFas}>
                {loadingFas ? "Uploading..." : faFile ? "Upload FA File" : "Add FA's in Bulk"}
              </button>
            </div>
        </div>
      </div>
      {/* Students Table */}
      {view === "students" && (
        <div>
          <br />
          <div className="filter-section">
            <input type="text" placeholder="Enter Year" value={filterYear} onChange={(e) => setFilterYear(e.target.value)} />
            <input type="text" placeholder="Enter Dept" value={filterDept} onChange={(e) => setFilterDept(e.target.value)} />
            <input type="text" placeholder="Enter FA Name" value={filterFaName} onChange={(e) => setFilterFaName(e.target.value)}/>
            <button onClick={bulkDeleteStudents} disabled={loadingDelete} className="bulk-delete-btn"> {loadingDelete ? "Deleting..." : "Bulk Delete Students"}</button>

          </div>

          <table>
            <thead>
              <tr>
                <th>SID</th><th>Name</th><th>Email</th><th>FA Name</th><th>Dept Points</th><th style={{ width: "5%" }}>Institute Points</th><th>Other Points</th><th>Total</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginatedStudents.map((s) => (
                <tr key={s.sid}>
                  <td>{s.sid}</td>
                  <td>{s.name}</td>
                  <td>{s.email}</td>
                  <td>{s.faName}</td>
                  <td>{s.deptPoints}</td>
                  <td>{s.institutePoints}</td>
                  <td>{s.otherPoints}</td>
                  <td>{s.activityPoints}</td>
                  <td className="actions-cell">
                    <button onClick={() => setEditStudent({...s})}>Edit</button>
                    <button onClick={() => deleteStudent(s.sid)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Pagination for Students */}
          <div className="pagination">
            {Array.from({ length: Math.ceil(filteredStudents.length / itemsPerPage) }, (_, i) => (
              <button key={i} className={page === i + 1 ? "active" : ""} onClick={() => setPage(i + 1)}>
                {i + 1}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* FA Table */}
      {view === "fas" && (
        <div className="filter-section">
          <br />
          <input type="text" placeholder="Search by Department Name" value={filterDeptFa} onChange={(e) => setFilterDeptFa(e.target.value)}/>
          <input type="text" placeholder="Enter FA Name" value={filterFaName} onChange={(e) => setFilterFaName(e.target.value)}/>
          <table>
            <thead>
              <tr><th>Name</th><th>Email</th><th>Department Name</th></tr>
            </thead>
            <tbody>
              {paginatedFas.map((fa) => (
                <tr key={fa.id}>
                  <td>{fa.name}</td>
                  <td>{fa.emailID}</td>
                  <td>{fa.department?.name}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Pagination for FAs */}
          <div className="pagination">
            {Array.from({ length: Math.ceil(filteredFas.length / itemsPerPage) }, (_, i) => (
              <button key={i} className={faPage === i + 1 ? "active" : ""} onClick={() => setFaPage(i + 1)}>
                {i + 1}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Edit Student Modal */}
      {editStudent && (
        <div className="modal">
          <div className="modal-content">
            <h3>Edit Student</h3>

            <div className="form-group">
              <label>Name</label>
              <input
                value={editStudent.name}
                onChange={(e) =>
                  setEditStudent({ ...editStudent, name: e.target.value })
                }
              />
            </div>

            <div className="form-group">
              <label>Email</label>
              <input
                value={editStudent.email || editStudent.emailID || ''}
                onChange={(e) =>
                  setEditStudent({ 
                    ...editStudent, 
                    email: e.target.value,
                    emailID: e.target.value
                  })
                }
              />
            </div>

            <div className="form-group">
              <label>Department Points</label>
              <input
                type="number"
                value={editStudent.deptPoints || 0}
                min="0"
                onKeyDown={(e) => {
                  if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
                }}
                onChange={(e) => {
                  const val = e.target.value === '' ? 0 : parseInt(e.target.value, 10);
                  setEditStudent({
                    ...editStudent,
                    deptPoints: isNaN(val) ? 0 : val,  // Extra safety check
                  });
                }}
              />
            </div>

            <div className="form-group">
              <label>Institute Points</label>
              <input
                type="number"
                value={editStudent.institutePoints || 0}
                min="0"
                onKeyDown={(e) => {
                  if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
                }}
                onChange={(e) => {
                  const val = e.target.value === '' ? 0 : parseInt(e.target.value, 10);
                  setEditStudent({
                    ...editStudent,
                    institutePoints: isNaN(val) ? 0 : val,  // Extra safety check
                  });
                }}
              />
            </div>

            <div className="form-group">
              <label>Other Points</label>
              <input
                type="number"
                value={editStudent.otherPoints || 0}
                min="0"
                onKeyDown={(e) => {
                  if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
                }}
                onChange={(e) => {
                    const val = e.target.value === '' ? 0 : parseInt(e.target.value, 10);
                    setEditStudent({
                      ...editStudent,
                      otherPoints: isNaN(val) ? 0 : val,  // Extra safety check
                    });
                  }}
              />
            </div>

            <div className="form-group">
              <label>FA ID</label>
              <input
                type="number"
                value={editStudent.faid}
                onChange={(e) =>
                  setEditStudent({
                    ...editStudent,
                    faid: parseInt(e.target.value),
                  })
                }
              />
            </div>

            <button onClick={updateStudent}>Save</button>
            <button onClick={() => setEditStudent(null)}>Cancel</button>
          </div>
        </div>
      )}

    </div>
  );
};

export default UserManagement;
