import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import "./uploadstatusmodal.css";
import { toast, Toaster } from "react-hot-toast"; 

const UploadStatusModal = ({ actid, isOpen, onClose }) => {
  const [file, setFile] = useState(null);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef(null);
  const token=localStorage.getItem("token");


  useEffect(() => {
    if (!isOpen) {
      setFile(null);
      setSummary(null);
      setLoading(false);
    }
  }, [isOpen]);

  const openFilePicker = () => {
    if (inputRef.current) inputRef.current.click();
  };

  const uploadAndCheck = async (chosenFile) => {
    if (!chosenFile) return;
    setLoading(true);
    const formData = new FormData();
    formData.append("file", chosenFile);

    try {
      const res = await axios.post(
        `http://localhost:8080/api/admin/check-attendance/${actid}`,
        formData,{
          headers:{
          Authorization: `Bearer ${token}`
        }
        }
        // do NOT set Content-Type manually here — browser will add boundary
      );

      // Debug: inspect what the server actually returned
      console.log("check-attendance response:", res.data);

      // Normalize different possible response shapes
      const data = res.data || {};
      const totalRows =
        data.totalRows ?? data.total_rows ?? data.total ?? 0;

      // backend may return an array of valid rows (validRows) OR only successCount
      const validRowsCandidate =
        data.validRows ?? data.valid_rows ?? data.valid ?? null;
      const skippedRowsCandidate =
        data.skippedRows ?? data.skipped_rows ?? data.skipped ?? null;

      const validRowsArray = Array.isArray(validRowsCandidate)
        ? validRowsCandidate
        : Array.isArray(data.validRows)
        ? data.validRows
        : [];

      const skippedRowsArray = Array.isArray(skippedRowsCandidate)
        ? skippedRowsCandidate
        : Array.isArray(data.skippedRows)
        ? data.skippedRows
        : [];

      const successCount =
        Array.isArray(validRowsCandidate)
          ? validRowsCandidate.length
          : data.successCount ?? data.success_count ?? validRowsArray.length ?? 0;

      const skippedCount =
        Array.isArray(skippedRowsCandidate)
          ? skippedRowsCandidate.length
          : data.skippedCount ?? data.skipped_count ?? skippedRowsArray.length ?? 0;

      const skippedDetails =
        skippedRowsArray.length > 0
          ? skippedRowsArray
          : data.skippedDetails ?? data.skipped_details ?? [];

      setSummary({
        totalRows,
        successCount,
        skippedCount,
        validRows: validRowsArray,
        skippedDetails,
      });
    } catch (err) {
      console.error("Error checking file:", err);
      toast.error("❌ Error checking file: " + (err.response?.data || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e) => {
    const chosen = e.target.files && e.target.files[0];
    if (!chosen) return;
    setFile(chosen);
    // Automatically check after picking
    uploadAndCheck(chosen);
  };

  const handleUploadClick = async () => {
    if (!file) {
      openFilePicker();
      return;
    }
    await uploadAndCheck(file);
  };

  const handlePopulatePoints = async () => {
    if (!summary) {
      alert("No checked data to populate. Please upload & check a file first.");
      return;
    }

    // If backend returned validRows (array), send it. If not, warn user.
    const rowsToSend = summary.validRows ?? [];
    if (!Array.isArray(rowsToSend) || rowsToSend.length === 0) {
      const proceed = window.confirm(
        "No detailed valid rows are available to send. The backend expects the list of valid students to finalize. Do you want to continue anyway?"
      );
      if (!proceed) return;
    }

    try {
      setLoading(true);
      // backend expects List<Map<String,String>> in request body
      const res = await axios.post(
        `/api/admin/finalize-attendance/${actid}`,
        rowsToSend,{headers:{
          Authorization: `Bearer ${token}`
        }}
      );
      console.log("finalize-attendance response:", res.data);
      toast.success("✅ Points populated successfully!");
      onClose();
    } catch (err) {
      console.error("Error finalizing attendance:", err);
      toast.error("❌ Error populating points: " + (err.response?.data || err.message));
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="usm-overlay" role="dialog" aria-modal="true">
    <Toaster/>
      <div className="usm-modal">
        <h2 className="usm-title">📂 Upload Attendance</h2>

        <input
          ref={inputRef}
          type="file"
          accept=".csv"
          onChange={handleFileChange}
          className="usm-hidden-input"
        />

        {!summary ? (
          <>
            <div className="usm-file-row">
              <button
                className="usm-btn usm-btn-primary"
                onClick={openFilePicker}
                disabled={loading}
              >
                Choose file
              </button>

              <div className="usm-file-info">
                {file ? (
                  <div className="usm-file-selected">
                    <span className="usm-file-name">{file.name}</span>
                    <span className="usm-file-meta">
                      ({Math.round(file.size / 1024)} KB)
                    </span>
                    <span className="usm-badge usm-badge-selected">Selected</span>
                  </div>
                ) : (
                  <div className="usm-no-file">No file chosen — choose a CSV to check</div>
                )}
              </div>
            </div>

            <div className="usm-actions">
              <button
                className="usm-btn usm-btn-secondary"
                onClick={onClose}
                disabled={loading}
              >
                Close
              </button>
              <button
                className="usm-btn usm-btn-primary"
                onClick={handleUploadClick}
                disabled={loading}
              >
                {loading ? "Checking..." : "Upload & Check"}
              </button>
            </div>

            <div className="usm-tips">
              <strong>Tips:</strong>
              <ul>
                <li>CSV should have one student SID per line (header optional).</li>
                <li>After you choose a file it will be checked automatically.</li>
                <li>If you need to change file, click <em>Choose file</em> again.</li>
              </ul>
            </div>
          </>
        ) : (
          <>
            <h3 className="usm-subtitle">📊 Upload Summary</h3>

            <div className="usm-stats">
              <div className="usm-stat">
                <div className="usm-stat-label">Total Rows</div>
                <div className="usm-stat-value">{summary.totalRows}</div>
              </div>
              <div className="usm-stat">
                <div className="usm-stat-label">Valid Entries</div>
                <div className="usm-stat-value">{summary.successCount}</div>
              </div>
              <div className="usm-stat">
                <div className="usm-stat-label">Skipped</div>
                <div className="usm-stat-value">{summary.skippedCount}</div>
              </div>
            </div>

            {summary.skippedDetails?.length > 0 && (
              <div className="usm-skipped">
                <div className="usm-skipped-title">⚠️ Skipped Rows</div>
                <ul className="usm-skipped-list">
                  {summary.skippedDetails.map((msg, idx) => (
                    <li key={idx} className="usm-skipped-item">
                      {msg}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <div className="usm-actions">
              <button
                className="usm-btn usm-btn-secondary"
                onClick={onClose}
                disabled={loading}
              >
                Close
              </button>
              <button
                className="usm-btn usm-btn-success"
                onClick={handlePopulatePoints}
                disabled={loading}
              >
                {loading ? "Processing..." : "Populate Points"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default UploadStatusModal;