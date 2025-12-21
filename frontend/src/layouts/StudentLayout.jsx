import React from 'react'
import { Outlet } from 'react-router-dom'
import NavBar from '../components/NavBar/NavBar'
import SideBar from '../components/SideBar/SideBar'
import './layouts.css' 

const StudentLayout = () => {
  return (
    <div>
      <NavBar />
      <Outlet />
      <div className="flex">
      <SideBar role="student" />
      
    </div>
    </div>
  )
}

export default StudentLayout
