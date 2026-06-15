import requests
import pandas as pd
from datetime import datetime, date

# 1. Fetch the Enriched Data from Spring Boot (All Theaters)
print("PYTHON: Downloading regional database footprint from http://localhost:8080/api/v1/metrics...")
url = "http://localhost:8080/api/v1/metrics"
response = requests.get(url)

if response.status_code == 200:
    data = response.json()
    print(f"PYTHON: Successfully received JSON payload containing {len(data)} movies!\n")
    
    # 2. Build the initial Pandas DataFrame
    df = pd.DataFrame(data)
    
    # --- FEATURE ENGINEERING ---
    
    # Feature 1: The Financial Gap (Breakeven Target - Revenue)
    # If the gap is less than 0, the movie is a certified hit.
    df['breakeven_target'] = df['budget'] * 2.5
    df['financial_gap'] = df['breakeven_target'] - df['revenue']
    
    # Clean up dates for math
    df['scrapedAt'] = pd.to_datetime(df['scrapedAt'])
    df['releaseDate'] = pd.to_datetime(df['releaseDate'], errors='coerce')
    
    # Feature 2: Days Since Release
    # We strip the timezone info to do simple day math
    scraped_dates_only = df['scrapedAt'].dt.tz_localize(None).dt.floor('d')
    df['days_since_release'] = (scraped_dates_only - df['releaseDate']).dt.days

    # Feature 3: The Countdown (Hours until showtime)
    # 1. Combine today's date with the "7:45 PM" string so Pandas can do math
    date_strings = df['scrapedAt'].dt.strftime('%Y-%m-%d') + ' ' + df['startTime']
    df['start_datetime'] = pd.to_datetime(date_strings, errors='coerce')
    
    # 2. Subtract the times and convert to hours
    df['hours_until_showtime'] = (df['start_datetime'] - df['scrapedAt']).dt.total_seconds() / 3600.0
    
    # 3. Handle edge cases (like movies that already started / negative countdowns)
    df['hours_until_showtime'] = df['hours_until_showtime'].apply(lambda x: 0 if x < 0 else x)
    
    # ---------------------------
    # 4. Data Cleaning: Neutralize the Landmines (NaNs)
    # If time is missing, default it to 0 so the model doesn't crash
    df['hours_until_showtime'] = df['hours_until_showtime'].fillna(0)

    # 5. Define the Target Label (The "Dashboard Optimized" Logic)
    def determine_flop_risk(row):
        # We tie the target directly to theater momentum so the UI sliders feel highly responsive
        
        # Rule 1: Catastrophic Flop
        # The theater is mostly empty, and the movie starts very soon.
        if row['fillRate'] < 30.0 and row['hours_until_showtime'] < 2.0:
            return 1
            
        # Rule 2: The Stagnant Bleed
        # The movie is older, but the theater is an absolute ghost town.
        if row['days_since_release'] > 14 and row['fillRate'] < 10.0:
            return 1
            
        # Otherwise, the momentum is safe!
        return 0

    df['is_flop_risk'] = df.apply(determine_flop_risk, axis=1)

    # Apply the global rules to create our target column
    df['is_flop_risk'] = df.apply(determine_flop_risk, axis=1)

    # 6. Save the snapshot for offline Machine Learning!
    df.to_csv('amc_training_data.csv', index=False)
    print("💾 DATA SAVED! You can now turn off the Java server.")
    # ---------------------------
    
    print("==================================================")
    print("      ENRICHED DATAFRAME SUCCESSFULLY BUILT       ")
    print("==================================================")
    
    # Let's look at the smartest columns
    columns_to_show = ['movieTitle', 'fillRate', 'financial_gap', 'days_since_release', 'hours_until_showtime']
    print(df[columns_to_show].tail(10))

else:
    print(f"CRASH: Spring Boot returned status code {response.status_code}")