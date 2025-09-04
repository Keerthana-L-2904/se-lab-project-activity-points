import React, { useState, useEffect } from "react";
import axios from "axios";
import "./manage.css";
import { PiStudent } from "react-icons/pi";
import { GiTeacher } from "react-icons/gi";
import { toast, Toaster } from "react-hot-toast"; 

const UserManagement = () => {
  const [students, setStudents] = useState([]);
  const [fas, setFas] = useState([]);
  const [file, setFile] = useState(null);
  const [faFile, setFaFile] = useState(null);
  const [filterYear, setFilterYear] = useState("");
  const [filterDept, setFilterDept] = useState("");
  const [filterDeptFa, setFilterDeptFa] = useState("");
  const [loading, setLoading] = useState(false);
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

// Upload students
const uploadStudents = async () => {
  if (!file) {
    document.getElementById("student-file").click(); // open file picker
    return;
  }
  const formData = new FormData();
  formData.append("file", file);
  setLoadingStudents(true);
  try {
    await axios.post("/api/admin/manage-users/upload-students", formData);
    toast.success("Students uploaded successfully ✅");
    setFile(null);
    fetchStudents();
  } catch (err) {
    console.error(err);
    toast.error("Error uploading student file ❌");
  } finally {
    setLoadingStudents(false);
  }
};

// Upload FAs
const uploadFas = async () => {
  if (!faFile) {
    document.getElementById("fa-file").click(); // open file picker
    return;
  }
  const formData = new FormData();
  formData.append("file", faFile);
  setLoadingFas(true);
  try {
    await axios.post("/api/admin/manage-users/upload-fas", formData);
    toast.success("FAs uploaded successfully ✅");
    setFaFile(null);
    fetchFas();
  } catch (err) {
    console.error(err);
    toast.error("Error uploading FA file ❌");
  } finally {
    setLoadingFas(false);
  }
};

  // Fetch data
  const fetchStudents = async () => {
    try {
      const res = await axios.get("/api/admin/manage-users/student");
      setStudents(res.data);
      setView("students");
      setPage(1);
    } catch (err) {
      console.error(err);
      toast.error("Error fetching students ❌");
    }
  };

  const fetchFas = async () => {
    try {
      const res = await axios.get("/api/admin/manage-users/fa");
      setFas(res.data);
      setView("fas");
      setFaPage(1);
    } catch (err) {
      console.error(err);
      toast.error("Error fetching FAs ❌");
    }
  };

  // Delete handlers
  const deleteStudent = async (sid) => {
    if (!window.confirm("Are you sure you want to delete this student?")) return;
    try {
      await axios.delete(`/api/admin/manage-users/student/${sid}`);
      setStudents(students.filter((s) => s.sid !== sid));
    } catch (err) {
      console.error(err);
      toast.error("Error deleting student ❌");
    }
  };

  // Bulk delete students
  const bulkDeleteStudents = async () => {
    const fileInput = document.createElement("input");
    fileInput.type = "file";
    fileInput.accept = ".xlsx,.xls";

    fileInput.onchange = async (e) => {
      const deleteFile = e.target.files[0];
      if (!deleteFile) return;

      const formData = new FormData();
      formData.append("file", deleteFile);

      try {
        await axios.post("/api/admin/manage-users/students/bulk-delete", formData);
        toast.success("Bulk delete successful ✅");
        fetchStudents(); // refresh list
      } catch (err) {
        console.error(err);
        toast.error("Error during bulk delete ❌");
      }
    };

    // directly trigger file picker
    fileInput.click();
  };

  // Update handlers
  const updateStudent = async () => {
    try {
      const res = await axios.put(`/api/admin/manage-users/student/${editStudent.sid}`, editStudent);
      setStudents(
        students.map((s) => (s.sid === editStudent.sid ? res.data : s))
      );
      setEditStudent(null);
    } catch (err) {
      console.error(err);
      toast.error("Error updating student ❌");
    }
  };

  // Filters
  const filteredStudents = students.filter((s) => {
    const yearMatch = filterYear ? s.sid.substring(1, 3) === filterYear : true;
    const deptMatch = filterDept ? s.sid.slice(-2).toUpperCase() === filterDept.toUpperCase() : true;
    return yearMatch && deptMatch;
  });

  const filteredFas = fas.filter((fa) => {
    return filterDeptFa ? fa.did.toString() === filterDeptFa : true;
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
                {loadingStudents ? "Uploading..." : file ? "Upload Students File" : "Select Students File"}
              </button>
            </div>
          
            <div className="upload-section">
              <label><GiTeacher size={50} /></label>
              <input type="file" id="fa-file" accept=".xlsx,.xls" hidden onChange={handleFaFileChange} />
              {faFile && <p>{faFile.name}</p>}
              <button onClick={uploadFas} disabled={loadingFas}>
                {loadingFas ? "Uploading..." : faFile ? "Upload FA File" : "Select FA File"}
              </button>
            </div>
        </div>
      </div>

      <hr />

      {/* Students Table */}
      {view === "students" && (
        <div>
          <br />
          <div className="filter-section">
            <input type="text" placeholder="Enter Year" value={filterYear} onChange={(e) => setFilterYear(e.target.value)} />
            <input type="text" placeholder="Enter Dept" value={filterDept} onChange={(e) => setFilterDept(e.target.value)} />
            <button onClick={bulkDeleteStudents} className="bulk-delete-btn"> Bulk Delete Students </button>
          </div>

          <table>
            <thead>
              <tr>
                <th>SID</th><th>Name</th><th>Email</th><th>Dept Points</th><th>Institute Points</th><th>Other Points</th><th>Total</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginatedStudents.map((s) => (
                <tr key={s.sid}>
                  <td>{s.sid}</td>
                  <td>{s.name}</td>
                  <td>{s.emailID}</td>
                  <td>{s.deptPoints}</td>
                  <td>{s.institutePoints}</td>
                  <td>{s.otherPoints}</td>
                  <td>{s.activityPoints}</td>
                  <td>
                    <button onClick={() => setEditStudent({...s})}>Edit</button>
                    <br />
                    <button style={{ marginTop: 10 }} onClick={() => deleteStudent(s.sid)}>Delete</button>
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
          <input type="text" placeholder="Enter Dept ID" value={filterDeptFa} onChange={(e)=>setFilterDeptFa(e.target.value)} />
          <table>
            <thead>
              <tr><th>Name</th><th>Email</th><th>DID</th><th>DepName</th></tr>
            </thead>
            <tbody>
              {paginatedFas.map((fa) => (
                <tr key={fa.id}>
                  <td>{fa.name}</td>
                  <td>{fa.emailID}</td>
                  <td>{fa.did}</td>
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
            <input value={editStudent.name} onChange={(e)=>setEditStudent({...editStudent, name:e.target.value})} placeholder="Name"/>
            <input value={editStudent.emailID} onChange={(e)=>setEditStudent({...editStudent, emailID:e.target.value})} placeholder="Email"/>
            <input type="number" value={editStudent.deptPoints} onChange={(e)=>setEditStudent({...editStudent, deptPoints: parseInt(e.target.value)})} placeholder="Dept Points"/>
            <input type="number" value={editStudent.institutePoints} onChange={(e)=>setEditStudent({...editStudent, institutePoints: parseInt(e.target.value)})} placeholder="Institute Points"/>
            <input type="number" value={editStudent.otherPoints} onChange={(e)=>setEditStudent({...editStudent, otherPoints: parseInt(e.target.value)})} placeholder="Other Points"/>
            <input type="number" value={editStudent.faid} onChange={(e)=>setEditStudent({...editStudent, faid: parseInt(e.target.value)})} placeholder="FA ID"/>
            <input type="number" value={editStudent.did} onChange={(e)=>setEditStudent({...editStudent, did: parseInt(e.target.value)})} placeholder="Dept ID"/>
            <button onClick={updateStudent}>Save</button>
            <button onClick={()=>setEditStudent(null)}>Cancel</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
