import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api/client';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await authAPI.login(email, password);
      const { userId, fullName, email: userEmail, token, tokenType } = response.data;
      login({ userId, fullName, email: userEmail }, token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };
return (
  <div className="auth-container">

    <div className="auth-left">

      <h1>🏥 HealthCare+</h1>

      <h2>Welcome Back</h2>

      <p>
        Securely access your healthcare dashboard, manage appointments,
        and connect with doctors anytime, anywhere.
      </p>

      <ul>
        <li>✔ Secure JWT Authentication</li>
        <li>✔ Book Appointments Instantly</li>
        <li>✔ View Appointment History</li>
        <li>✔ Modern Healthcare Experience</li>
      </ul>

    </div>

    <div className="auth-card">

      <h2>Sign In</h2>

      <p className="subtitle">
        Login to continue
      </p>

      <form onSubmit={handleSubmit}>

        <input
          type="email"
          placeholder="Email Address"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          disabled={isLoading}
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          disabled={isLoading}
        />

        {error && (
          <div className="error-box">
            {error}
          </div>
        )}

        <button
          className="auth-btn"
          type="submit"
          disabled={isLoading}
        >
          {isLoading ? "Signing In..." : "Sign In"}
        </button>

      </form>

      <p className="bottom-text">
        Don't have an account?

        <Link to="/register">
          Register
        </Link>

      </p>

    </div>

  </div>
);
  
}
