import React, { useState, useEffect } from 'react';
import { slotAPI, appointmentAPI } from '../api/client';
import './Slots.css';

export default function AvailableSlots() {
  const [slots, setSlots] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingSlotId, setBookingSlotId] = useState(null);
  const [bookingError, setBookingError] = useState('');
  const [bookingSuccess, setBookingSuccess] = useState('');

  useEffect(() => {
    fetchSlots();
  }, []);

  const fetchSlots = async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await slotAPI.getAvailable();
      setSlots(response.data);
    } catch (err) {
      setError('Failed to fetch available slots');
    } finally {
      setIsLoading(false);
    }
  };

  const handleBookAppointment = async (slotId) => {
    setBookingSlotId(slotId);
    setBookingError('');
    setBookingSuccess('');

    try {
      setBookingSuccess('Booking appointment...');
      await appointmentAPI.create(slotId);
      setBookingSuccess('Appointment booked successfully');
      setTimeout(() => {
        setBookingSuccess('');
        setBookingSlotId(null);
        fetchSlots();
      }, 2000);
    } catch (err) {
      const msg = err.response?.data?.message;
      if (msg?.includes('already booked')) {
        setBookingError('Slot already booked');
      } else {
        setBookingError(msg || 'Failed to book appointment');
      }
      setBookingSlotId(null);
    }
  };

  if (isLoading) {
    return <div className="status-message">Fetching available slots...</div>;
  }

  return (
   <div className="slots-grid">

  {slots.map((slot) => (

    <div key={slot.id} className="slot-card">

      <div className="doctor-avatar">

        👨‍⚕️

      </div>

      <div className="slot-header">

        <h3>{slot.doctorName}</h3>

        <span className="department">

          {slot.department}

        </span>

      </div>

      <div className="slot-details">

        <div className="detail-row">

          <span>📅</span>

          <p>{new Date(slot.slotDate).toLocaleDateString()}</p>

        </div>

        <div className="detail-row">

          <span>🕒</span>

          <p>{slot.startTime} - {slot.endTime}</p>

        </div>

        <div className="detail-row">

          <span>✔</span>

          <span className={`status ${slot.status.toLowerCase()}`}>
            {slot.status}
          </span>

        </div>

      </div>

      <button
        className="book-btn"
        onClick={() => handleBookAppointment(slot.id)}
        disabled={bookingSlotId === slot.id}
      >
        {bookingSlotId === slot.id
          ? "Booking..."
          : "Book Appointment"}
      </button>

    </div>

  ))}

</div>
  );
}
