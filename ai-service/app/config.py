from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    app_name: str = "CivicFix AI Service"
    environment: str = "development"
    port: int = 8000
    cors_origins: str = "http://localhost:5173,http://localhost:3000"
    backend_api_url: str = "http://localhost:8080/api/v1"
    model_name: str = "municipal-grievance-classifier-v1"
    confidence_threshold: float = 0.75

    @property
    def cors_origins_list(self) -> List[str]:
        return [origin.strip() for origin in self.cors_origins.split(",") if origin.strip()]

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
