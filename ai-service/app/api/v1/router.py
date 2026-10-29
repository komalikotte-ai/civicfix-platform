from fastapi import APIRouter
from app.api.v1.endpoints import categorization, duplicates, urgency

api_router = APIRouter()

api_router.include_router(categorization.router, tags=["AI Categorization"])
api_router.include_router(duplicates.router, tags=["Duplicate Detection"])
api_router.include_router(urgency.router, tags=["Urgency & SLA Analysis"])
