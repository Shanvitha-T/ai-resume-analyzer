from dotenv import load_dotenv

load_dotenv()  # must run before analyzer is imported

from fastapi import FastAPI, File, Form, HTTPException, UploadFile

from analyzer import analyze_resume
from extractor import extract_text
from models import AnalysisResult

app = FastAPI(title="Resume AI Service")

MAX_SIZE = 5 * 1024 * 1024  # 5 MB


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze", response_model=AnalysisResult)
async def analyze(
    file: UploadFile = File(...),
    job_description: str | None = Form(None),
):
    content = await file.read()
    if len(content) > MAX_SIZE:
        raise HTTPException(413, "File too large (max 5 MB)")

    try:
        text = extract_text(file.filename or "", content)
    except ValueError as e:
        raise HTTPException(400, str(e))
    except Exception:
        raise HTTPException(400, "Could not read the file. Is it corrupted?")

    if len(text) < 50:
        raise HTTPException(
            422, "No readable text found. Scanned/image-only resumes aren't supported."
        )

    try:
        return analyze_resume(text, job_description)
    except Exception as e:
        print("AI error:", repr(e))  # shows the real cause in your terminal
        raise HTTPException(502, "AI analysis failed. Please try again.")