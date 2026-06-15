# 🔮 Box Office Oracle
A full-stack machine learning pipeline and dashboard that predicts the financial flop risk of theatrical releases using early regional theater telemetry.

![Box Office Oracle Dashboard](dashboard.png)

## 🎯 The Business Problem
Predicting Hollywood box office success is notoriously difficult. This system acts as a "Regional Bellwether," using real-time seat fill rates from high-traffic theaters to forecast national financial momentum within the first two weeks of a movie's release.

## 🏗️ System Architecture
This project is built using a decoupled microservice architecture:

* **Frontend Client:** React.js dashboard providing real-time telemetry inputs and confidence visualizations.
* **Backend REST API:** Python and FastAPI handling asynchronous client requests and CORS routing.
* **Machine Learning Engine:** A LightGBM classification model trained on scraped theater capacity data, utilizing synthetic spectrum injection to stabilize decision-tree boundaries and prevent data-void hallucinations.

## ⚙️ Core Technical Achievements
* **Data Augmentation:** Engineered a synthetic data injection pipeline to smooth out algorithmic confidence curves and handle edge-case inputs.
* **API Integration:** Built a robust FastAPI bridge to serve the `joblib` machine learning model to the React client with sub-second latency.
* **Feature Engineering:** Transformed raw theater scrape data into predictive features (`fillRate`, `days_since_release`, `hours_until_showtime`) using Pandas.

## 🚀 How to Run Locally
1. **Start the API:** `cd backend` -> `uvicorn api:app --reload`
2. **Start the Client:** `cd frontend` -> `npm run dev`