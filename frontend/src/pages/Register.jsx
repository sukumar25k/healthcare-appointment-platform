import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authAPI } from "../api/client";
import { useAuth } from "../context/AuthContext";
import "./Auth.css";

export default function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [validationErrors, setValidationErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setValidationErrors({});
    setIsLoading(true);

    try {
      const response = await authAPI.register(fullName, email, password);

      const {
        userId,
        fullName: name,
        email: userEmail,
        token,
      } = response.data;

      login(
        {
          userId,
          fullName: name,
          email: userEmail,
        },
        token
      );

      navigate("/dashboard");
    } catch (err) {
      if (err.response?.data?.validationErrors) {
        setValidationErrors(err.response.data.validationErrors);
      } else {
        setError(err.response?.data?.message || "Registration failed");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">

      <div className="auth-left">
        <h1>🏥 HealthCare+</h1>

        <h2>Create Account</h2>

        <p>
          Book appointments with doctors, manage your schedule and
          access healthcare services securely.
        </p>

        <ul>
          <li>✔ Secure Login</li>
          <li>✔ Instant Appointment Booking</li>
          <li>✔ Appointment History</li>
          <li>✔ Modern Healthcare Platform</li>
        </ul>
      </div>

      <div className="auth-card">

        <h2>Register</h2>

        <p className="subtitle">
          Create your account
        </p>

        <form onSubmit={handleSubmit}>

          <input
            type="text"
            placeholder="Full Name"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            disabled={isLoading}
            required
          />

          {validationErrors.fullName && (
            <div className="error-box">
              {validationErrors.fullName}
            </div>
          )}

          <input
            type="email"
            placeholder="Email Address"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isLoading}
            required
          />

          {validationErrors.email && (
            <div className="error-box">
              {validationErrors.email}
            </div>
          )}

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isLoading}
            required
          />

          {validationErrors.password && (
            <div className="error-box">
              {validationErrors.password}
            </div>
          )}

          {error && (
            <div className="error-box">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="auth-btn"
            disabled={isLoading}
          >
            {isLoading ? "Registering..." : "Register"}
          </button>

        </form>

        <p className="bottom-text">
          Already have an account?
          <Link to="/login"> Login</Link>
        </p>

      </div>

    </div>
  );
}