import { Injectable,inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG, AppConfig } from '../app-config';

@Injectable({ providedIn: 'root' })
export class VirementService {
  private config = inject(APP_CONFIG);
  private apiUrl = `${this.config.apiUrl}}/employee/employee/transfer`;
  constructor(private http: HttpClient) {}

  effectuerVirement(data: { fromAccountId: number; toRib: string; amount: number }): Observable<string> {
    const token = localStorage.getItem('token') || '';
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    });
    console.log('VirementService chargé avec URL :', this.apiUrl);
    return this.http.post(this.apiUrl, data, { headers, responseType: 'text' });
  }
}
