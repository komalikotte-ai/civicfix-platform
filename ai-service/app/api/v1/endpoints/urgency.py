from fastapi import APIRouter
from app.schemas.complaint_ai import UrgencyScoreRequest, UrgencyScoreResponse
from app.services.classifier import calculate_urgency

router = APIRouter()


@router.post("/analyze-urgency", response_model=UrgencyScoreResponse, summary="Analyze urgency and SLA risk")
async def analyze_urgency(request: UrgencyScoreRequest):
    urgency, sla_hours, sentiment, escalation_risk = calculate_urgency(
        request.title, request.description, request.category
    )
    return UrgencyScoreResponse(
        urgency_level=urgency,
        recommended_sla_hours=sla_hours,
        sentiment_score=sentiment,
        escalation_risk=escalation_risk,
    )
