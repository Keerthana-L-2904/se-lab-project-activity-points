import React, { useState, useRef, useEffect } from "react";
import axiosInstance from "../../utils/axiosConfig";
import "./uploadstatusmodal.css";
import { toast, Toaster } from "react-hot-toast"; 

const UploadStatusModal = ({ actid, isOpen, onClose }) => {
  const [file, setFile] = useState(null);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef(null);
  
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
      const res = await axiosInstance.post(
        `/admin/check-attendance/${actid}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      const data = res.data || {};
      const totalRows = data.totalRows ?? data.total_rows ?? data.total ?? 0;

      const validRowsCandidate = data.validRows ?? data.valid_rows ?? data.valid ?? null;
      const skippedRowsCandidate = data.skippedRows ?? data.skipped_rows ?? data.skipped ?? null;

      const validRowsArray = Array.isArray(validRowsCandidate)
        ? validRowsCandidate
        : Array.isArray(data.validSids)
        ? data.validSids
        : [];

      const skippedRowsArray = Array.isArray(skippedRowsCandidate)
        ? skippedRowsCandidate
        : Array.isArray(data.skippedRows)
        ? data.skippedRows
        : [];

      const successCount = Array.isArray(validRowsCandidate)
        ? validRowsCandidate.length
        : data.successCount ?? data.success_count ?? validRowsArray.length ?? 0;

      const skippedCount = Array.isArray(skippedRowsCandidate)
        ? skippedRowsCandidate.length
        : data.skippedCount ?? data.skipped_count ?? skippedRowsArray.length ?? 0;

      const skippedDetails = skippedRowsArray.length > 0
        ? skippedRowsArray
        : data.skippedDetails ?? data.skipped_details ?? [];

      // ✅ Count already enrolled students from skipped details
      const alreadyEnrolledCount = skippedDetails.filter(msg => 
        msg.includes("Already enrolled") || msg.includes("already enrolled")
      ).length;

      setSummary({
        totalRows,
        successCount,
        skippedCount,
        alreadyEnrolledCount,
        validRows: validRowsArray,
        skippedDetails,
      });
      
      // ✅ Show already enrolled count in success toast
      const successMessage = `✅ File checked! ${successCount} valid,${skippedCount} skipped, ${alreadyEnrolledCount} already enrolled`
        
      
      toast.success(successMessage);
      
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data || err.message;
      toast.error(`❌ Error checking file: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e) => {
    const chosen = e.target.files && e.target.files[0];
    if (!chosen) return;
    
    const validTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-excel'
    ];
    
    if (!validTypes.includes(chosen.type)) {
      toast.error("❌ Please upload an Excel file (.xlsx or .xls)");
      return;
    }
    
    if (chosen.size > 5 * 1024 * 1024) {
      toast.error("❌ File size must be less than 5MB");
      return;
    }
    
    setFile(chosen);
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
      toast.error("No checked data to populate. Please upload & check a file first.");
      return;
    }

    const rowsToSend = summary.validRows ?? [];
    if (!Array.isArray(rowsToSend) || rowsToSend.length === 0) {
      const proceed = window.confirm(
        "No detailed valid rows are available to send. The backend expects the list of valid students to finalize. Do you want to continue anyway?"
      );
      if (!proceed) return;
    }

    try {
      setLoading(true);
      const response = await axiosInstance.post(
        `/admin/finalize-attendance/${actid}`,
        rowsToSend,
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
      toast.success("✅ Points populated successfully!");
      onClose();
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data || err.message;
      toast.error(`❌ Error populating points: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="usm-overlay" role="dialog" aria-modal="true">
      <Toaster />
      <div className="usm-modal">
        <h2 className="usm-title">📂 Upload Attendance</h2>

        <input
          ref={inputRef}
          type="file"
          accept=".xlsx,.xls"
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
                  <div className="usm-no-file">No file chosen — choose an Excel file to check</div>
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
                <li>File size must be at most 5MB</li>
                <li>It must have only one column heading <b>Student ID</b></li>
                <li>File name must have the name <b>enrollment_list.xlsx</b></li>
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
              {summary.alreadyEnrolledCount > 0 && (
                <div className="usm-stat">
                  <div className="usm-stat-label">Already Enrolled</div>
                  <div className="usm-stat-value">{summary.alreadyEnrolledCount}</div>
                </div>
              )}
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