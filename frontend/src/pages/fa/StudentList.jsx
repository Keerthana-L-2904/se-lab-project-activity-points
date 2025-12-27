import { useState, useEffect } from "react";
import "./studlist.css";
import { Link } from "react-router-dom";
import { AiOutlineSortAscending } from "react-icons/ai";
import { TbSortDescendingLetters } from "react-icons/tb";
import axiosInstance from "../../utils/axiosConfig";
import { toast, Toaster } from "react-hot-toast";

export default function StudentList() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchName, setSearchName] = useState("");
  const [mandatoryCount, setMandatoryCount] = useState("");

  // Filters
  const [deptBelow, setDeptBelow] = useState("");
  const [deptAbove, setDeptAbove] = useState("");
  const [instBelow, setInstBelow] = useState("");
  const [instAbove, setInstAbove] = useState("");
  const [actBelow, setActBelow] = useState("");
  const [actAbove, setActAbove] = useState("");

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const studentsPerPage = 10;

  // ✅ IMPROVED: Get faid from localStorage
  const faid = localStorage.getItem("FAID") || localStorage.getItem("faid");

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    if (!faid) {
      console.warn("No FA ID found in localStorage");
      toast.error("Faculty Advisor ID not found");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      const response = await axiosInstance.get(
        "/api/fa/student-list/sort-by-asc"
      );
      setStudents(response.data || []);
      setCurrentPage(1);
      
    } catch (err) {
      toast.error("Failed to load students");
    } finally {
      setLoading(false);
    }
  };

  // --- SEARCH BY NAME ---
  const handleSearchByName = async () => {
    if (!searchName.trim()) {
      fetchStudents();
      return;
    }

    try {
      setLoading(true);
      const response = await axiosInstance.get(
        "/api/fa/student-list/search",
        { params: { name: searchName } }
      );
      
      setStudents(response.data || []);
      setCurrentPage(1);
      toast.success(`Found ${response.data.length} student(s)`);
      
    } catch (err) {
      toast.error("Search failed");
    } finally {
      setLoading(false);
    }
  };

  // --- SEARCH BY MANDATORY ---
  const handleSearchByMandatory = async () => {
    if (!mandatoryCount) {
      fetchStudents();
      return;
    }

    try {
      setLoading(true);
      const response = await axiosInstance.get(
        "/api/fa/student-list/search-by-mandatory",
        { params: { mandatoryCount } }
      );
      
      setStudents(response.data || []);
      setCurrentPage(1);
      toast.success(`Found ${response.data.length} student(s)`);
      
    } catch (err) {
      toast.error("Search failed");
    } finally {
      setLoading(false);
    }
  };

  // --- SORT ---
  const handleSort = async (order) => {
    try {
      setLoading(true);
      const endpoint = order === "asc" ? "sort-by-asc" : "sort-by-desc";
      const response = await axiosInstance.get(
        `/api/fa/student-list/${endpoint}`
      );
      
      setStudents(response.data || []);
      setCurrentPage(1);
      
    } catch (err) {
      toast.error("Sort failed");
    } finally {
      setLoading(false);
    }
  };

  // --- FILTER ---
  const applyFilter = async (type, condition, value) => {
    if (!value) {
      fetchStudents();
      return;
    }

    try {
      setLoading(true);
      const response = await axiosInstance.get(
        `/api/fa/student-list/filter-${type}-points-${condition}`,
        { params: { points: value } }
      );
      
      setStudents(response.data || []);
      setCurrentPage(1);
      toast.success(`Filtered to ${response.data.length} student(s)`);
      
    } catch (err) {
      toast.error("Filter failed");
    } finally {
      setLoading(false);
    }
  };

  // --- PAGINATION ---
  const totalPages = Math.ceil(students.length / studentsPerPage);
  const startIndex = (currentPage - 1) * studentsPerPage;
  const currentStudents = students.slice(
    startIndex,
    startIndex + studentsPerPage
  );

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  if (loading) return <p>Loading students...</p>;

  return (
    <div className="student-list-container">
      <Toaster />
      <h2 className="list-title">List of Students</h2>

      <table className="list-table">
        <thead>
          <tr>
            <th>
              Student Name
              <div className="search-group">
                <input
                  type="text"
                  placeholder="Search by Name"
                  value={searchName}
                  onChange={(e) => setSearchName(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" && handleSearchByName()
                  }
                />
                <div className="sort-group">
                  <button onClick={() => handleSort("asc")} title="Sort A-Z">
                    <AiOutlineSortAscending size={22} />
                  </button>
                  <button onClick={() => handleSort("desc")} title="Sort Z-A">
                    <TbSortDescendingLetters size={22} />
                  </button>
                </div>
              </div>
            </th>

            <th>Roll No.</th>

            <th>
              Department Points
              <div className="points-filter">
                <input
                  type="number"
                  placeholder="Below"
                  value={deptBelow}
                  onChange={(e) => setDeptBelow(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" &&
                    applyFilter("dept", "below", deptBelow)
                  }
                />
                <input
                  type="number"
                  placeholder="Above"
                  value={deptAbove}
                  onChange={(e) => setDeptAbove(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" &&
                    applyFilter("dept", "above", deptAbove)
                  }
                />
              </div>
            </th>

            <th>
              Institutional Points
              <div className="points-filter">
                <input
                  type="number"
                  placeholder="Below"
                  value={instBelow}
                  onChange={(e) => setInstBelow(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" &&
                    applyFilter("inst", "below", instBelow)
                  }
                />
                <input
                  type="number"
                  placeholder="Above"
                  value={instAbove}
                  onChange={(e) => setInstAbove(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" &&
                    applyFilter("inst", "above", instAbove)
                  }
                />
              </div>
            </th>

            <th>
              Total Points
              <div className="points-filter">
                <input
                  type="number"
                  placeholder="Below"
                  value={actBelow}
                  onChange={(e) => setActBelow(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" &&
                    applyFilter("activity", "below", actBelow)
                  }
                />
                <input
                  type="number"
                  placeholder="Above"
                  value={actAbove}
                  onChange={(e) => setActAbove(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" &&
                    applyFilter("activity", "above", actAbove)
                  }
                />
              </div>
            </th>

            <th>
              Mandatory
              <br></br>
              <input
                type="number"
                placeholder="Count"
                value={mandatoryCount}
                onChange={(e) => setMandatoryCount(e.target.value)}
                onKeyDown={(e) =>
                  e.key === "Enter" && handleSearchByMandatory()
                }
              />
            </th>

            <th>Details</th>
          </tr>
        </thead>

        <tbody>
          {currentStudents.length > 0 ? (
            currentStudents.map(({ student, mandatoryCount }) => (
              <tr key={student.sid}>
                <td>{student.name?.toUpperCase() || "N/A"}</td>
                <td>{student.sid || "N/A"}</td>
                <td>{student.deptPoints || 0}</td>
                <td>{student.institutePoints || 0}</td>
                <td>{student.activityPoints || 0}</td>
                <td>{mandatoryCount || 0}</td>
                <td>
                  <Link to={`/fa/student-details/${student.sid}`}>
                    <button className="vieww-btn">View</button>
                  </Link>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7">No students found</td>
            </tr>
          )}
        </tbody>
      </table>

      {totalPages > 1 && (
        <div className="pagination">
          <button
            disabled={currentPage === 1}
            onClick={() => handlePageChange(currentPage - 1)}
          >
            ◀ Prev
          </button>

          {[...Array(totalPages)].map((_, i) => (
            <button
              key={i}
              className={currentPage === i + 1 ? "active" : ""}
              onClick={() => handlePageChange(i + 1)}
            >
              {i + 1}
            </button>
          ))}

          <button
            disabled={currentPage === totalPages}
            onClick={() => handlePageChange(currentPage + 1)}
          >
            Next ▶
          </button>
        </div>
      )}
    </div>
  );
}