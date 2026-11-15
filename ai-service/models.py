from pydantic import BaseModel, Field


class AnalysisResult(BaseModel):
    overall_score: int = Field(ge=0, le=100, description="Overall resume quality, 0-100")
    summary: str = Field(description="2-3 sentence summary of the candidate")
    skills: list[str] = Field(description="Technical and soft skills found in the resume")
    strengths: list[str] = Field(description="What the resume does well")
    weaknesses: list[str] = Field(description="Problems or gaps")
    suggestions: list[str] = Field(description="Specific, actionable improvements")
    match_score: int | None = Field(
        default=None, ge=0, le=100,
        description="Fit against the job description, 0-100. Null if no job description given",
    )
    missing_keywords: list[str] = Field(
        default_factory=list,
        description="Important job-description keywords absent from the resume",
    )