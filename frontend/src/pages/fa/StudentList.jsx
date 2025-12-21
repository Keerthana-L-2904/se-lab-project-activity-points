import { useState, useEffect } from "react";
import "./studlist.css";
import { Link } from "react-router-dom";
import { AiOutlineSortAscending } from "react-icons/ai";
import { TbSortDescendingLetters } from "react-icons/tb";

export default function StudentList() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchName, setSearchName] = useState("");
  const [mandatoryCount, setMandatoryCount] = useState("");
  const token = localStorage.getItem("token");

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

  const faid = localStorage.getItem("FAID") || localStorage.getItem("faid");

  useEffect(() => {
    fetchStudents();
  }, [faid]);

  const fetchStudents = () => {
    if (!faid) {
      setLoading(false);
      return;
    }
    setLoading(true);
    fetch(`http://localhost:8080/api/fa/student-list/${faid}/sort-by-asc`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((response) => response.json())
      .then((data) => {
        setStudents(data);
        setLoading(false);
        setCurrentPage(1); // reset pagination when data changes
      })
      .catch((error) => {
        console.error("Error fetching students:", error);
        setLoading(false);
      });
  };

  // --- SEARCH ---
  const handleSearchByName = () => {
    if (searchName.trim() === "") {
      fetchStudents();
      return;
    }
    setLoading(true);
    fetch(
      `http://localhost:8080/api/fa/student-list/${faid}/search?name=${searchName}`,
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    )
      .then((res) => res.json())
      .then((data) => {
        setStudents(data);
        setLoading(false);
        setCurrentPage(1);
      })
      .catch(console.error);
  };

  const handleSearchByMandatory = () => {
    if (mandatoryCount === "") {
      fetchStudents();
      return;
    }
    setLoading(true);
    fetch(
      `http://localhost:8080/api/fa/student-list/${faid}/search-by-mandatory?mandatoryCount=${mandatoryCount}`,
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    )
      .then((res) => res.json())
      .then((data) => {
        setStudents(data);
        setLoading(false);
        setCurrentPage(1);
      })
      .catch(console.error);
  };

  // --- SORT ---
  const handleSort = (order) => {
    setLoading(true);
    const endpoint = order === "asc" ? "sort-by-asc" : "sort-by-desc";
    fetch(`http://localhost:8080/api/fa/student-list/${faid}/${endpoint}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => {
        setStudents(data);
        setLoading(false);
        setCurrentPage(1);
      })
      .catch(console.error);
  };

  // --- FILTER HELPERS ---
  const applyFilter = (type, condition, value) => {
    if (!value) {
      fetchStudents();
      return;
    }
    setLoading(true);
    fetch(
      `http://localhost:8080/api/fa/student-list/${faid}/filter-${type}-points-${condition}?points=${value}`,
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    )
      .then((res) => res.json())
      .then((data) => {
        setStudents(data);
        setLoading(false);
        setCurrentPage(1);
      })
      .catch(console.error);
  };

  if (loading) return <p>Loading...</p>;

  // --- PAGINATION LOGIC ---
  const totalPages = Math.ceil(students.length / studentsPerPage);
  const startIndex = (currentPage - 1) * studentsPerPage;
  const endIndex = startIndex + studentsPerPage;
  const currentStudents = students.slice(startIndex, endIndex);

  const handlePageChange = (pageNum) => {
    if (pageNum >= 1 && pageNum <= totalPages) {
      setCurrentPage(pageNum);
    }
  };

  return (
    <div className="student-list-container">
      <h2 className="list-title">List of Students</h2>
      <div className="list-header">
        <table className="list-table">
          <thead>
            <tr>
              <th>
                Student Name <br />
                <div className="search-group">
                  <input
                    type="text"
                    placeholder="Search by Name"
                    value={searchName}
                    onChange={(e) => setSearchName(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleSearchByName();
                    }}
                  />
                  <div className="sort-group">
                    <button
                      className="vieww-btn"
                      onClick={() => handleSort("asc")}
                    >
                      <AiOutlineSortAscending size={24} />
                    </button>
                    <button
                      className="vieww-btn"
                      onClick={() => handleSort("desc")}
                    >
                      <TbSortDescendingLetters size={24} />
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
                Mandatory Courses <br />
                <div className="mandatory-search">
                  <input
                    type="number"
                    placeholder="Mandatory Count"
                    value={mandatoryCount}
                    onChange={(e) => setMandatoryCount(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleSearchByMandatory();
                    }}
                  />
                </div>
              </th>
              <th>Detail View</th>
            </tr>
          </thead>
          <tbody>
            {currentStudents.length > 0 ? (
              currentStudents.map((studentObj) => {
                const student = studentObj.student;
                return (
                  <tr key={student.sid}>
                    <td style={{ textTransform: "uppercase" }}>
                      {student.name}
                    </td>
                    <td>{student.sid}</td>
                    <td>{student.deptPoints}</td>
                    <td>{student.institutePoints}</td>
                    <td>{student.activityPoints}</td>
                    <td>{studentObj.mandatoryCount}</td>
                    <td>
                      <Link to={`/fa/student-details/${student.sid}`}>
                        <button className="vieww-btn">View</button>
                      </Link>
                    </td>
                  </tr>
                );
              })
            ) : (
              <tr>
                <td colSpan="7">No students found</td>
              </tr>
            )}
          </tbody>
        </table>

        {/* PAGINATION CONTROLS */}
        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="vieww-btn"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              ◀ Prev
            </button>

            {[...Array(totalPages)].map((_, index) => (
              <button
                key={index}
                className={`page-btn ${
                  currentPage === index + 1 ? "active" : ""
                }`}
                onClick={() => handlePageChange(index + 1)}
              >
                {index + 1}
              </button>
            ))}

            <button
              className="vieww-btn"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next ▶
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
