from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert "CivicFix AI" in data["service"]


def test_categorization_endpoint():
    payload = {
        "title": "Large pothole on highway",
        "description": "A deep crater in the road asphalt causing vehicle accidents near market gate",
    }
    response = client.post("/api/v1/ai/categorize", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["predicted_category"] == "Pothole & Road Damage"
    assert data["suggested_department_code"] == "ROADS"
    assert data["confidence"] > 0.5
