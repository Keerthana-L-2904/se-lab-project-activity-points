import axios from "axios";
const handleAddStudent = async (fetchData,setAddModalOpen,setNewActivity,newActivity) => {
    try {
      console.log(newActivity)
      const response = await axios.post("/api/admin/manage-users/student", newActivity);
      if (response.status === 200) {
        fetchData(); // Refresh list
        setAddModalOpen(false);
        setNewActivity({
          sid: "",  // ✅ Backend expects this, but form sends rollNo
    name: "",
    emailID: "",
    faid: "",
    did: "",
    deptPoints: "",
    institutePoints: "",
        });
      } else {
        alert("Error adding student info!");
      }
    } catch (error) {
      console.error("Error fetching student info", error);
      alert("Failed to fetch student info!");
    }
  };

  const handleAddFA = async (fetchFAData, setFAModalOpen, newFaculty,faculty) => {
    try {
        const response = await axios.post("/api/admin/manage-users/fa", faculty);
        if (response.status === 200) {
            fetchFAData(); // Refresh list
            setFAModalOpen(false);
            newFaculty({
              name:'',
              did:'',
              emailID:''
            });
        } else {
            alert("Error adding faculty info!");
        }
    } catch (error) {
        console.error("Error fetching faculty info", error);
        alert("Failed to fetch faculty info!");
    }
};
const handleEditStud = async (fetchData,setIsEditModalOpen,editStudent) => {
  try {
    console.log(editStudent);
    const response = await axios.put(`/api/admin/manage-users/student/${editStudent.sid}`, editStudent);
    if (response.status === 200) {
      fetchData(); // Refresh list
      setIsEditModalOpen(false);
    } else {
      alert("Error updating student record!");
    }
  } catch (error) {
    console.error("Error updating student record", error);
    alert("Failed to update student record!");
  }
};

const handleEditFa = async (fetchData,setIsEditModalOpen,editFa) => {
  try {
    console.log(editFa);
    const response = await axios.put(`/api/admin/manage-users/fa/${editFa.faid}`, editFa);
    if (response.status === 200) {
      fetchData(); // Refresh list
      setIsEditModalOpen(false);
    } else {
      alert("Error updating fa record!");
    }
  } catch (error) {
    console.error("Error updating fa record", error);
    alert("Failed to update fa record!");
  }
};

const getDeptData=async(setDepartments)=>{
  try {
    const response = await axios.get("/api/admin/get-departments");
    if (response.status === 200) {
      setDepartments(response.data);
    } else {
      console.log('Error loading departments!');
    }
  } catch (error) {
    console.error('Error fetching departments', error);
    
  }
};

const handleDeleteStud = async (fetchData,id) => {
  try {
    console.log(id);
    const response = await axios.delete(`/api/admin/manage-users/student/${id}`);
    if (response.status === 200) {
      fetchData(); // Refresh list
    } else {
      alert("Error deleting student record!");
    }
  } catch (error) {
    console.error("Error deleting student record", error);
    alert("Failed to delete student record!");
  }
};

const handleDeleteFa = async (fetchData,id) => {
  try {
    console.log(id);
    const response = await axios.delete(`/api/admin/manage-users/fa/${id}`);
    if (response.status === 200) {
      fetchData(); // Refresh list
    } else {
      alert("Error deleting fa record!");
    }
  } catch (error) {
    console.error("Error deleting fa record", error);
    alert("Failed to delete fa record!");
  }
};

  export {handleAddStudent,handleAddFA,handleEditStud,handleEditFa,getDeptData,handleDeleteStud,handleDeleteFa};
