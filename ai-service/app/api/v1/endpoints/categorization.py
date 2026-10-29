from fastapi import APIRouter
from app.schemas.complaint_ai import CategorizationRequest, CategorizationResponse
from app.services.classifier import classify_complaint

router = APIRouter()


@router.post("/categorize", response_model=CategorizationResponse, summary="Predict category and department for grievance")
async def categorize_complaint(request: CategorizationRequest):
    category, dept, priority, conf, keywords = classify_complaint(request.title, request.description)
    return CategorizationResponse(
        predicted_category=category,
        suggested_department_code=dept,
        suggested_priority=priority,
        confidence=conf,
        keywords=keywords,
    )
