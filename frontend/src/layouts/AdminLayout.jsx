import React from 'react'
import { Outlet } from 'react-router-dom'
import NavBar from '../components/NavBar/NavBar.jsx'
import SideBar from '../components/SideBar/SideBar.jsx'
import './layouts.css' 

const AdminLayout = () => {
  return (
    <div>
      <NavBar />
      <Outlet />
      <div className="flex">
      <SideBar role="admin" />
     
    </div>
    </div>
  )
}

export default AdminLayout
