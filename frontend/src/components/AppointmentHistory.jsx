import React, { useState, useEffect } from 'react';
import { appointmentAPI } from '../api/client';
import './Appointments.css';

export default function AppointmentHistory() {
  const [appointments, setAppointments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancellingId, setCancellingId] = useState(null);
  const [cancelSuccess, setCancelSuccess] = useState('');

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await appointmentAPI.getUserAppointments();
      setAppointments(response.data);
    } catch (err) {
      setError('Failed to fetch appointments');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelAppointment = async (appointmentId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) {
      return;
    }

    setCancellingId(appointmentId);
    setCancelSuccess('');

    try {
      await appointmentAPI.cancel(appointmentId);
      setCancelSuccess('Appointment cancelled successfully');
      setTimeout(() => {
        setCancelSuccess('');
        setCancellingId(null);
        fetchAppointments();
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel appointment');
      setCancellingId(null);
    }
  };

  if (isLoading) {
    return <div className="status-message">Loading appointment history...</div>;
  }

  return (
    <div className="appointments-container">
      {error && <div className="error-message">{error}</div>}
      {cancelSuccess && <div className="success-message">{cancelSuccess}</div>}

      {appointments.length === 0 ? (
        <div className="no-appointments">
          You have no appointments yet. <br />
          Go to Available Slots to book one.
        </div>
      ) : (
       <div className="appointments-list">

  {appointments.map((apt) => (

    <div
      key={apt.id}
      className="appointment-card"
    >

      <div className="appointment-avatar">

        👨‍⚕️

      </div>

      <div className="apt-header">

        <div>

          <h3>{apt.slot.doctorName}</h3>

          <p className="department">
            {apt.slot.department}
          </p>

        </div>

        <span className={`apt-status ${apt.status.toLowerCase()}`}>
          {apt.status}
        </span>

      </div>

      <div className="apt-details">

        <div className="detail-row">
          <span>📅</span>
          <p>{new Date(apt.slot.slotDate).toLocaleDateString()}</p>
        </div>

        <div className="detail-row">
          <span>🕒</span>
          <p>{apt.slot.startTime} - {apt.slot.endTime}</p>
        </div>

        <div className="detail-row">
          <span>📌</span>
          <p>
            {new Date(apt.bookedAt).toLocaleString()}
          </p>
        </div>

      </div>

      {apt.status === "BOOKED" && (

        <button
          className="cancel-btn"
          onClick={() => handleCancelAppointment(apt.id)}
          disabled={cancellingId === apt.id}
        >
          {cancellingId === apt.id
            ? "Cancelling..."
            : "Cancel Appointment"}
        </button>

      )}

    </div>

  ))}

</div>
      )}
    </div>
  );
}
