from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.api.v1.router import api_router
from datetime import datetime, timezone

app = FastAPI(
    title=settings.app_name,
    version="1.0.0",
    description="CivicFix AI Microservice for municipal operations, grievance classification, and duplicate detection.",
    docs_url="/docs",
    openapi_url="/openapi.json",
)

# CORS Middleware configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include v1 router under /api/v1/ai
app.include_router(api_router, prefix="/api/v1/ai")


@app.get("/health", tags=["Health"])
async def health_check():
    return {
        "status": "UP",
        "service": settings.app_name,
        "environment": settings.environment,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }
