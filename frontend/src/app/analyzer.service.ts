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

export interface HistoryItem {
  id: number;
  fileName: string;
  overallScore: number;
  matchScore: number; // -1 means no job description was used
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AnalyzerService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  analyze(file: File, jobDescription: string): Observable<AnalysisResult> {
    const formData = new FormData();
    formData.append('file', file);
    if (jobDescription.trim()) {
      formData.append('jobDescription', jobDescription);
    }
    return this.http.post<AnalysisResult>(`${this.baseUrl}/analyze`, formData);
  }

  getHistory(): Observable<HistoryItem[]> {
    return this.http.get<HistoryItem[]>(`${this.baseUrl}/history`);
  }

  getHistoryItem(id: number): Observable<AnalysisResult> {
    return this.http.get<AnalysisResult>(`${this.baseUrl}/history/${id}`);
  }
}