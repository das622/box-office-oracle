import joblib
import pandas as pd

print("==================================================")
print("🔮 THE BOX OFFICE ORACLE is waking up...")
print("==================================================")

# 1. Load the Brain
try:
    model = joblib.load('box_office_oracle.pkl')
    print("SYSTEM: Brain loaded successfully.\n")
except FileNotFoundError:
    print("CRASH: Could not find box_office_oracle.pkl. Did you run predictor.py first?")
    exit()

# 2. Enter Custom Movie Data (Scenario 2: The Underperforming Premiere)
print("INPUT: Scanning new movie telemetry...")
movie_data = pd.DataFrame({
    'fillRate': [10.0],              
    'hours_until_showtime': [0.5],       
    'days_since_release': [2.0]          
})

# 3. Ask the Oracle
# predict() gives us the hard 1 or 0
prediction = model.predict(movie_data)[0]

# predict_proba() gives us the exact mathematical confidence percentage
confidence = model.predict_proba(movie_data)[0][1] * 100

print("\n==================================================")
if prediction == 1:
    print(f"🚨 PREDICTION: MASSIVE FLOP RISK 🚨")
    print(f"CONFIDENCE: The Oracle is {confidence:.2f}% sure this movie will bomb.")
else:
    print(f"✅ PREDICTION: SAFE BOX OFFICE ✅")
    print(f"CONFIDENCE: The Oracle sees only a {confidence:.2f}% chance of a flop.")
print("==================================================")