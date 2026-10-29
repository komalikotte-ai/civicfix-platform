from typing import List, Tuple
import re

# Rule-based intent and domain dictionary for municipal grievances
CATEGORY_MAPPINGS = {
    "Pothole & Road Damage": {
        "dept": "ROADS",
        "priority": "HIGH",
        "keywords": ["pothole", "crater", "asphalt", "road", "tar", "accident", "cracked", "pavement"],
    },
    "Garbage & Solid Waste": {
        "dept": "SANITATION",
        "priority": "MEDIUM",
        "keywords": ["garbage", "trash", "waste", "dump", "bin", "smell", "rotting", "litter", "debris"],
    },
    "Streetlight Failure": {
        "dept": "ELECTRICAL",
        "priority": "MEDIUM",
        "keywords": ["streetlight", "dark", "light", "bulb", "pole", "wire", "blackout", "lamp"],
    },
    "Water Leakage / Contamination": {
        "dept": "WATER_SUPPLY",
        "priority": "CRITICAL",
        "keywords": ["leak", "pipe", "burst", "water", "drain", "contamination", "drinking", "flooding"],
    },
    "Sewage Overflow": {
        "dept": "SANITATION",
        "priority": "CRITICAL",
        "keywords": ["sewage", "gutter", "overflow", "manhole", "drainage", "stench"],
    },
}


def classify_complaint(title: str, description: str) -> Tuple[str, str, str, float, List[str]]:
    text = f"{title} {description}".lower()
    matched_category = "Other Municipal Issue"
    suggested_dept = "GENERAL"
    suggested_priority = "MEDIUM"
    best_score = 0
    matched_keywords = []

    for cat_name, data in CATEGORY_MAPPINGS.items():
        score = 0
        current_matched = []
        for kw in data["keywords"]:
            if re.search(r"\b" + re.escape(kw) + r"\b", text):
                score += 1
                current_matched.append(kw)

        if score > best_score:
            best_score = score
            matched_category = cat_name
            suggested_dept = data["dept"]
            suggested_priority = data["priority"]
            matched_keywords = current_matched

    confidence = min(0.95, 0.40 + (best_score * 0.15)) if best_score > 0 else 0.30
    return matched_category, suggested_dept, suggested_priority, confidence, matched_keywords


def calculate_urgency(title: str, description: str, category: str) -> Tuple[str, int, float, bool]:
    text = f"{title} {description}".lower()
    hazard_words = ["danger", "hazard", "spark", "fire", "emergency", "fatal", "injury", "hospital", "flood", "electric shock"]
    hazard_hits = [w for w in hazard_words if w in text]

    if hazard_hits or category in ["Sewage Overflow", "Water Leakage / Contamination"]:
        return "CRITICAL", 6, -0.85, True
    elif any(w in text for w in ["blocked", "broken", "severe", "traffic", "accident"]):
        return "HIGH", 24, -0.50, False
    else:
        return "MEDIUM", 48, -0.15, False
