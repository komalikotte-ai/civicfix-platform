from pydantic import BaseModel, Field
from typing import List, Optional


class CategorizationRequest(BaseModel):
    title: str = Field(..., min_length=3, description="Complaint summary title")
    description: str = Field(..., min_length=5, description="Full complaint narrative")


class CategorizationResponse(BaseModel):
    predicted_category: str
    suggested_department_code: str
    suggested_priority: str
    confidence: float
    keywords: List[str]


class DuplicateCheckRequest(BaseModel):
    title: str
    description: str
    ward_id: int
    category: Optional[str] = None


class DuplicateCandidate(BaseModel):
    complaint_id: int
    tracking_number: str
    similarity_score: float
    status: str


class DuplicateCheckResponse(BaseModel):
    is_potential_duplicate: bool
    top_match_score: float
    candidates: List[DuplicateCandidate]


class UrgencyScoreRequest(BaseModel):
    title: str
    description: str
    category: str


class UrgencyScoreResponse(BaseModel):
    urgency_level: str
    recommended_sla_hours: int
    sentiment_score: float
    escalation_risk: bool
