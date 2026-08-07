import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import AvailableSlots from '../components/AvailableSlots';
import AppointmentHistory from '../components/AppointmentHistory';
import './Dashboard.css';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('slots');

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
  <div className="dashboard">

    <header className="dashboard-header">

      <div>

        <h1>🏥 HealthCare Appointment Platform</h1>

        <p>
          Welcome back,
          <strong> {user?.fullName}</strong>
        </p>

      </div>

      <button
        className="logout-btn"
        onClick={handleLogout}
      >
        Logout
      </button>

    </header>

    <div className="dashboard-cards">

      <div className="stat-card">

        <h3>👨‍⚕️ Doctors</h3>

        <span>25+</span>

      </div>

      <div className="stat-card">

        <h3>📅 Appointments</h3>

        <span>Manage Easily</span>

      </div>

      <div className="stat-card">

        <h3>🔒 Security</h3>

        <span>JWT Protected</span>

      </div>

      <div className="stat-card">

        <h3>⚡ Status</h3>

        <span>Online</span>

      </div>

    </div>

    <nav className="tabs">

      <button
        className={`tab ${activeTab === "slots" ? "active" : ""}`}
        onClick={() => setActiveTab("slots")}
      >
        📅 Available Slots
      </button>

      <button
        className={`tab ${activeTab === "history" ? "active" : ""}`}
        onClick={() => setActiveTab("history")}
      >
        📖 Appointment History
      </button>

    </nav>

    <div className="dashboard-content">

      {activeTab === "slots" && <AvailableSlots />}

      {activeTab === "history" && <AppointmentHistory />}

    </div>

  </div>
);
    
}
