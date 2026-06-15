import { useState } from 'react'
import './App.css'

function App() {
  const [formData, setFormData] = useState({
    budget: 150000000,
    fillRate: 40,
    days_since_release: 2,
    hours_until_showtime: 0.5
  });
  
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  // Handle Input Changes
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: parseFloat(e.target.value) });
  };

  // Ping the FastAPI Server
  const askOracle = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8000/api/predict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      
      const data = await response.json();
      setResult(data);
    } catch (error) {
      console.error("API Connection Error:", error);
      alert("Failed to connect to the Brain. Is FastAPI running?");
    }
    setLoading(false);
  };

  return (
    <div className="dashboard">
      <h1>🔮 Box Office Oracle</h1>
      <p className="subtitle">Real-Time ML Financial Risk Predictor</p>

      <div className="main-container">
        {/* The Input Form */}
        <div className="control-panel">
          <h3>Movie Telemetry</h3>
          <form onSubmit={askOracle}>
            <label>Budget ($):
              <input type="number" name="budget" value={formData.budget} onChange={handleChange} />
            </label>
            <label>AMC Fill Rate (%):
              <input type="number" name="fillRate" value={formData.fillRate} onChange={handleChange} />
            </label>
            <label>Days Since Release:
              <input type="number" name="days_since_release" value={formData.days_since_release} onChange={handleChange} />
            </label>
            <label>Hours Until Showtime:
              <input type="number" name="hours_until_showtime" value={formData.hours_until_showtime} step="0.1" onChange={handleChange} />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? "Scanning..." : "Run Prediction"}
            </button>
          </form>
        </div>

        {/* The Result Card */}
        <div className="result-panel">
          <h3>Prediction Engine</h3>
          {result ? (
            <div className={`status-card ${result.prediction_code === 1 ? 'flop' : 'safe'}`}>
              <h2>{result.risk_level}</h2>
              <p>Confidence: <strong>{result.confidence_percentage}%</strong></p>
            </div>
          ) : (
            <div className="status-card waiting">
              <p>Awaiting telemetry...</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default App