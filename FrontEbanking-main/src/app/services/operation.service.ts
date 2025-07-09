import { Injectable,inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG, AppConfig } from '../app-config';


@Injectable({ providedIn: 'root' })
export class OperationService {
  private config = inject(APP_CONFIG);
  private baseUrl = `${this.config.apiUrl}/employee`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || '';
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    });
  }

  deposit(accountId: string, amount: number): Observable<string> {
    const url = `${this.baseUrl}/operations/deposit`;
    const body = { accountId, amount };
    return this.http.post(url, body, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  withdraw(accountId: string, amount: number): Observable<string> {
    const url = `${this.baseUrl}/operations/withdraw`;
    const body = { accountId, amount };
    return this.http.post(url, body, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }
}
