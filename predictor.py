import pandas as pd
import numpy as np
import joblib
import lightgbm as lgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

print("==================================================")
print("🧠 GOING BACK TO THE DRAWING BOARD...")
print("==================================================")

# 1. Load the Real Scraped Data
df = pd.read_csv('amc_training_data.csv')

# ==========================================
# 2. THE SPECTRUM INJECTION (Fixing the Void)
# ==========================================
synthetic_rows = []

# A. Teach it the "Safe Zone" (30% to 100% full)
# We step by 5% so the model sees a perfect, smooth gradient.
for fill in range(30, 101, 5): 
    for days in [1, 2, 3, 5, 10, 20]:
        for hours in [0.5, 2.0, 12.0]:
            synthetic_rows.append({
                'fillRate': float(fill),
                'hours_until_showtime': float(hours),
                'days_since_release': float(days),
                'is_flop_risk': 0  # Anything over 30% capacity is fundamentally safe
            })

# B. Teach it "Absolute Rock Bottom" (0% to 15% full on opening weekend)
for fill in range(0, 16, 2):
    synthetic_rows.append({
        'fillRate': float(fill),
        'hours_until_showtime': 0.5,
        'days_since_release': 2.0,
        'is_flop_risk': 1  # Completely empty on premiere weekend = FLOP
    })

# Merge the spectrum with your real data
synthetic_df = pd.DataFrame(synthetic_rows)
df = pd.concat([df, synthetic_df], ignore_index=True)
print(f"SYSTEM: Injected {len(synthetic_df)} spectrum data points to smooth the AI's logic.")
# ==========================================

# 3. Define Features and Target
features = ['fillRate', 'hours_until_showtime', 'days_since_release']
X = df[features]
y = df['is_flop_risk']

# 4. Train/Test Split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 5. Build the Stabilized Brain
print("MODEL: Studying the stabilized data...")
model = lgb.LGBMClassifier(
    n_estimators=100,
    learning_rate=0.05,  # Slowed down the learning rate so it draws a smoother curve
    random_state=42,
    verbose=-1
    # NOTE: We deleted 'is_unbalance=True' to stop the AI from acting paranoid
)

model.fit(X_train, y_train)
print("MODEL: Training complete!\n")

# 6. Save the Brain
print("==================================================")
joblib.dump(model, 'box_office_oracle.pkl')
print("💾 EXPORTING: Model successfully saved as 'box_office_oracle.pkl'")
print("==================================================")