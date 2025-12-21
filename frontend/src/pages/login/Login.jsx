import React, { useState,useContext,useEffect} from 'react';
import { ChevronRight, UserCircle2, Users, ShieldUser } from 'lucide-react';
import twoPersonImg from '../../assets/twopersons.png';
import { AuthContext } from '../../context/AuthContext';
import { Link } from "react-router-dom";
import { toast, Toaster } from "react-hot-toast";
import "./login.css";
const LoginPage = () => {

    const { loginstudent,loginfa,authError,setauthError } = useContext(AuthContext);
	useEffect(() => {
  		if (authError) {
    			toast.error(authError);
    			setauthError(null); // clear after showing
  				}
		}, [authError]);

  return (
    <div className="login-container">
	 <Toaster />
      <div className="left-panel">
        <div className="max-width-md">
          <h1 style={{ animationDelay: '0.1s',transform: 'translateY(-25%)',textAlign:"left" }}>Hello!</h1>
          <p className="subheading" style={{ animationDelay: '0.2s', transform: 'translateY(-15%)' }}>
            Choose your login option
          </p>
          <div className="login-options">
            <div style={{ animationDelay: '0.3s', animationFillMode: 'both' }}>
              <button
                className="login-option"
                onClick={() =>
                    loginstudent()
		   
}
              >
                <div className="option-text">
                  <h3>Student</h3>
                  <p>Access the students&apos; portal here.</p>

                </div>
                <div className="option-icon">
                  <UserCircle2 size={24} />
                  <ChevronRight size={20} style={{ transform: 'translateX(0)', transition: 'transform 0.2s' }}/>
                </div>
              </button>
            </div>
            <div style={{ animationDelay: '0.4s', animationFillMode: 'both' }}>
              <button className="login-option"  onClick={() =>loginfa()}>
                <div className="option-text">
                  <h3>Faculty Advisor</h3>
                  <p>Exclusive to faculty members only.</p>
                </div>
                <div className="option-icon">
                  <Users size={24} />
                  <ChevronRight
                    size={20}
                    style={{ transform: 'translateX(0)', transition: 'transform 0.2s' }}
                  />
                </div>
              </button>
            </div>
            <div style={{ animationDelay: '0.4s', animationFillMode: 'both' }}>
            <Link to="/admin/login" className="login-optionn">  
              <div className="option-text">
                <h3>Admin</h3>
                <p>Exclusive to Admins only.</p>
              </div>
              <div className="option-icon">
                <ShieldUser size={24} />
                <ChevronRight
                  size={20}
                  style={{ transform: 'translateX(0)', transition: 'transform 0.2s' }}
                />
              </div>
            </Link>

            </div>
          </div>
        </div>
      </div>
      <div className="right-panel">
        <div className="bg-circle1"></div>
        <div className="bg-circle2"></div>
        <div className="content">
          <h1>Activity Points Management Portal</h1>
          <p>Login to access your account</p>
          <img src={twoPersonImg} alt="TwoPersons" />
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
