from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import joblib
import pandas as pd

# 1. Initialize the Web App
app = FastAPI(title="Box Office Oracle API")

# 2. Add the CORS Middleware (MUST BE RIGHT AFTER APP INITIALIZATION)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"], 
    allow_headers=["*"],
)

# 3. Load the Brain into Memory
print("🔮 THE BOX OFFICE ORACLE is waking up...")
try:
    model = joblib.load('box_office_oracle.pkl')
    print("SYSTEM: Brain loaded successfully. Ready for web requests.")
except FileNotFoundError:
    model = None
    print("CRASH: Could not find box_office_oracle.pkl. Did you run predictor.py?")

# 4. Define the strict Data Contract
class MovieTelemetry(BaseModel):
    fillRate: float
    hours_until_showtime: float
    days_since_release: float

# 5. Create the Prediction Endpoint
@app.post("/api/predict")
def predict_flop_risk(telemetry: MovieTelemetry):
    if model is None:
        raise HTTPException(status_code=500, detail="The AI Brain is offline.")

    input_data = pd.DataFrame([{
        'fillRate': telemetry.fillRate,
        'hours_until_showtime': telemetry.hours_until_showtime,
        'days_since_release': telemetry.days_since_release
    }])

    prediction = model.predict(input_data)[0]
    confidence = model.predict_proba(input_data)[0][1] * 100

    return {
        "status": "success",
        "prediction_code": int(prediction),
        "risk_level": "MASSIVE FLOP RISK 🚨" if prediction == 1 else "SAFE BOX OFFICE ✅",
        "confidence_percentage": round(confidence, 2)
    }