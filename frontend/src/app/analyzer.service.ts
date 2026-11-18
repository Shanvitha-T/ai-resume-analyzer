import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AnalysisResult {
  overallScore: number;
  summary: string;
  skills: string[];
  strengths: string[];
  weaknesses: string[];
  suggestions: string[];
  matchScore: number | null;
  missingKeywords: string[];
}

@Injectable({ providedIn: 'root' })
export class AnalyzerService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/analyze';

  analyze(file: File, jobDescription: string): Observable<AnalysisResult> {
    const formData = new FormData();
    formData.append('file', file);
    if (jobDescription.trim()) {
      formData.append('jobDescription', jobDescription);
    }
    return this.http.post<AnalysisResult>(this.apiUrl, formData);
  }
}