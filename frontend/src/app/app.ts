import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AnalyzerService, AnalysisResult, HistoryItem } from './analyzer.service';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  selectedFile = signal<File | null>(null);
  jobDescription = '';
  loading = signal(false);
  error = signal('');
  result = signal<AnalysisResult | null>(null);
  history = signal<HistoryItem[]>([]);

  constructor(private analyzer: AnalyzerService) {}

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.analyzer.getHistory().subscribe({
      next: (items) => this.history.set(items),
      error: () => this.history.set([]),
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files && input.files.length ? input.files[0] : null);
  }

  analyze() {
    const file = this.selectedFile();
    if (!file) {
      this.error.set('Please choose a PDF or DOCX resume first.');
      return;
    }
    this.error.set('');
    this.result.set(null);
    this.loading.set(true);

    this.analyzer.analyze(file, this.jobDescription).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
        this.loadHistory();
      },
      error: (err) => {
        this.error.set(err?.error?.error ?? 'Something went wrong. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  openHistory(id: number) {
    this.error.set('');
    this.analyzer.getHistoryItem(id).subscribe({
      next: (res) => {
        this.result.set(res);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: () => this.error.set('Could not load that saved analysis.'),
    });
  }
}