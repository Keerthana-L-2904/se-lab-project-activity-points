import React, { useState } from "react";
import axios from "axios";
import "./manage.css";
import { PiStudent } from "react-icons/pi";
import { GiTeacher } from "react-icons/gi";

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
  const [view, setView] = useState(""); // "students" | "fas" | ""

  const itemsPerPage = 10;

  // file upload
  const handleStudentFileChange = (e) => setFile(e.target.files[0]);
  const handleFaFileChange = (e) => setFaFile(e.target.files[0]);

  const uploadStudents = async () => {
    if (!file) return alert("Please select a student Excel file.");
    const formData = new FormData();
    formData.append("file", file);
    setLoading(true);
    try {
      await axios.post("/api/admin/manage-users/upload-students", formData);
      alert("Students uploaded successfully ✅");
      setFile(null);
    } catch (err) {
      console.error(err);
      alert("Error uploading student file ❌");
    } finally {
      setLoading(false);
    }
  };

  const uploadFas = async () => {
    if (!faFile) return alert("Please select a FA Excel file.");
    const formData = new FormData();
    formData.append("file", faFile);
    setLoading(true);
    try {
      await axios.post("/api/admin/manage-users/upload-fas", formData);
      alert("FAs uploaded successfully ✅");
      setFaFile(null);
    } catch (err) {
      console.error(err);
      alert("Error uploading FA file ❌");
    } finally {
      setLoading(false);
    }
  };

  // fetch lists
  const fetchStudents = async () => {
    try {
      const response = await axios.get("/api/admin/manage-users/student");
      setStudents(response.data);
      setView("students");
    } catch (err) {
      console.error(err);
      alert("Error fetching students ❌");
    }
  };

  const fetchFas = async () => {
    try {
      const response = await axios.get("/api/admin/manage-users/fa");
      setFas(response.data);
      setView("fas");
    } catch (err) {
      console.error(err);
      alert("Error fetching FAs ❌");
    }
  };

  // delete actions
  const deleteStudent = async (sid) => {
    if (!window.confirm("Are you sure you want to delete this student?")) return;
    try {
      await axios.delete(`/api/admin/manage-users/student/${sid}`);
      setStudents(students.filter((s) => s.sid !== sid));
    } catch (err) {
      console.error(err);
      alert("Error deleting student ❌");
    }
  };

  const deleteFa = async (id) => {
    if (!window.confirm("Are you sure you want to delete this FA?")) return;
    try {
      await axios.delete(`/api/admin/manage-users/fa/${id}`);
      setFas(fas.filter((f) => f.id !== id));
    } catch (err) {
      console.error(err);
      alert("Error deleting FA ❌");
    }
  };

  // bulk delete students
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
      alert("Bulk delete successful ✅");
      fetchStudents(); // refresh list
    } catch (err) {
      console.error(err);
      alert("Error during bulk delete ❌");
    }
  };

  // directly trigger file picker
  fileInput.click();
};

  // filters
  const filteredStudents = students.filter((s) => {
    const yearMatch = filterYear ? s.sid.substring(1, 3) === filterYear : true;
    const deptMatch = filterDept
      ? s.sid.substring(s.sid.length - 2).toUpperCase() === filterDept.toUpperCase()
      : true;
    return yearMatch && deptMatch;
  });

  const filteredFas = fas.filter((fa) => {
    return filterDeptFa ? fa.did.toString() === filterDeptFa : true;
  });

  const paginatedStudents = filteredStudents.slice(
    (page - 1) * itemsPerPage,
    page * itemsPerPage
  );

  return (
    <div className="management-container">
      <h2 style={{ textTransform: "uppercase", textAlign: "center" }}>Admin - Manage Users</h2>
      <br></br>

    <div className="upload-section-container">
      {/* See All Buttons Column */}
      <div className="see-all-column">
        <button onClick={fetchFas} className="see-all-btn" style={{color:"white"}}>See All FAs</button>
        <button onClick={fetchStudents} className="see-all-btn"style={{color:"white"}}>See All Students</button>
      </div>

      {/* Upload Wrapper */}
      <div className="upload-wrapper">
        {/* Upload Students */}
        <div className="upload-section">
          <label htmlFor="student-file">
            <PiStudent className="upload-icon" size={50} />
          </label>
          <input
            type="file"
            id="student-file"
            accept=".xlsx, .xls"
            onChange={handleStudentFileChange}
            hidden
          />
          {file && <p className="file-name">{file.name}</p>}
          <button onClick={uploadStudents} disabled={!file || loading}>
            {loading ? "Uploading..." : "Upload Students File"}
          </button>
        </div>

        {/* Upload FAs */}
        <div className="upload-section">
          <label htmlFor="fa-file">
            <GiTeacher className="upload-icon" size={50} />
          </label>
          <input
            type="file"
            id="fa-file"
            accept=".xlsx, .xls"
            onChange={handleFaFileChange}
            hidden
          />
          {faFile && <p className="file-name">{faFile.name}</p>}
          <button onClick={uploadFas} disabled={!faFile || loading}>
            {loading ? "Uploading..." : "Upload FA file"}
          </button>
        </div>
      </div>
    </div>

      <hr />

      {/* Student Section */}
      {view === "students" && (
        <div>
          <div className="filter-section">
            <br></br>
            <input
              type="text"
              placeholder="Enter Year (e.g., 22)"
              value={filterYear}
              onChange={(e) => setFilterYear(e.target.value)}
            />
            <input
              type="text"
              placeholder="Enter Dept (e.g., CS)"
              value={filterDept}
              onChange={(e) => setFilterDept(e.target.value)}
            />
            <button onClick={bulkDeleteStudents} className="bulk-delete-btn">
              Bulk Delete Students
            </button>
          </div>

          <div className="table-container">
            <h3>Student List</h3>
            <table>
              <thead>
                <tr>
                  <th>SID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Dept Points</th>
                  <th>Institute Points</th>
                  <th>Other Points</th>
                  <th>Total Points</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {paginatedStudents.map((student) => (
                  <tr key={student.sid}>
                    <td>{student.sid}</td>
                    <td>{student.name}</td>
                    <td>{student.emailID}</td>
                    <td>{student.deptPoints}</td>
                    <td>{student.institutePoints}</td>
                    <td>{student.otherPoints}</td>
                    <td>{student.activityPoints}</td>
                    <td>
                      <button className="edit-btn">Edit</button>
                      <br></br>
                      <button
                        className="delete-btn"
                        onClick={() => deleteStudent(student.sid)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Page-shift */}
            <div className="pagination">
              {Array.from(
                { length: Math.ceil(filteredStudents.length / itemsPerPage) },
                (_, index) => (
                  <button
                    key={index}
                    className={page === index + 1 ? "active" : ""}
                    onClick={() => setPage(index + 1)}
                  >
                    {index + 1}
                  </button>
                )
              )}
            </div>
          </div>
        </div>
      )}

{/* FAs Section */}
{view === "fas" && (
  <div>
    <br></br>
    <div className="filter-section">
      <input
        type="text"
        placeholder="Enter Dept ID"
        value={filterDeptFa}
        onChange={(e) => setFilterDeptFa(e.target.value)}
      />
    </div>

    <div className="table-container">
      <h3>FA List</h3>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>DID</th>
            <th>DepName</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filteredFas.map((fa) => (
            <tr key={fa.id}>
              <td>{fa.name}</td>
              <td>{fa.emailID}</td>
              <td>{fa.did}</td>
              <td>{fa.department?.name}</td>
              <td>
                <button className="edit-btn">Edit</button>
                <br></br>
                <button
                  className="delete-btn"
                  onClick={() => deleteFa(fa.id)}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
  )}
    </div>
  );
};

export default UserManagement;
