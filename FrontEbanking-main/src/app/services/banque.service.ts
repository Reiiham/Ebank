import { Injectable,inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { ClientBasicDTO } from '../banque/models/client-basic.model';
import { ClientSummaryDTO } from '../banque/models/client-summary.model';
import { ClientUpdateRequest } from '../banque/models/client-update-request.model';
import { catchError, tap } from 'rxjs/operators';
import { APP_CONFIG, AppConfig } from '../app-config';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private config = inject(APP_CONFIG);
  private baseUrl = `${this.config.apiUrl}/employee`;

  constructor(private http: HttpClient) {}

  // ✅ Secure access to localStorage
  private getAuthHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (typeof window !== 'undefined' && window.localStorage) {
      const token = localStorage.getItem('token') || localStorage.getItem('authToken') || localStorage.getItem('jwt');
      console.log('🔑 Token found:', token ? 'YES' : 'NO');
      console.log('🔑 Token value:', token?.substring(0, 20) + '...');

      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      } else {
        console.warn('⚠️ No auth token found in localStorage!');
      }
    } else {
      console.warn('🚫 localStorage not available (probably prerendering)');
    }

    return headers;
  }

  private handleError = (operation = 'operation') => {
    return (error: HttpErrorResponse): Observable<never> => {
      console.error(`❌ ${operation} failed:`, error);
      console.error('Status:', error.status);
      console.error('Status Text:', error.statusText);
      console.error('Error Body:', error.error);
      console.error('Full URL:', error.url);

      if (error.status === 401) {
        console.error('🚨 UNAUTHORIZED - Check your token!');
      }

      return throwError(() => error);
    };
  };

  addAccount(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/clients/add-account`, data, {
      headers: this.getAuthHeaders(),
      responseType: 'json'
    });
  }

  getAccountCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/accounts/count`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Account count result:', result)),
      catchError(this.handleError('getAccountCount'))
    );
  }

  getClientCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/clients/count`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Client count result:', result)),
      catchError(this.handleError('getClientCount'))
    );
  }

  getBasicClients(): Observable<ClientBasicDTO[]> {
    return this.http.get<ClientBasicDTO[]>(`${this.baseUrl}/clients/basic`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Basic clients result:', result?.length, 'items')),
      catchError(this.handleError('getBasicClients'))
    );
  }

  getAllClients(): Observable<ClientSummaryDTO[]> {
    return this.http.get<ClientSummaryDTO[]>(`${this.baseUrl}/clients/detailed`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => {
        console.log('✅ All clients result:', result?.length, 'items');
        console.log('First client sample:', result?.[0]);
      }),
      catchError(this.handleError('getAllClients'))
    );
  }

  getClientById(id: string): Observable<ClientSummaryDTO> {
    return this.http.get<ClientSummaryDTO>(`${this.baseUrl}/clients/${id}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Client by ID result:', result)),
      catchError(this.handleError('getClientById'))
    );
  }

  searchClientsByName(name: string): Observable<ClientSummaryDTO[]> {
    return this.http.get<ClientSummaryDTO[]>(`${this.baseUrl}/clients/search?name=${encodeURIComponent(name)}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Search clients result:', result?.length, 'items')),
      catchError(this.handleError('searchClientsByName'))
    );
  }

  createClient(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/enroll`, data, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Create client result:', result)),
      catchError(this.handleError('createClient'))
    );
  }

  updateClient(data: ClientUpdateRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/update`, data, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    }).pipe(
      tap(result => console.log('✅ Update client result:', result)),
      catchError(this.handleError('updateClient'))
    );
  }

  deleteClient(payload: { clientId: string; supervisorCode: string }): Observable<any> {
    return this.http.request('delete', `${this.baseUrl}/delete`, {
      body: payload,
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Delete client result:', result)),
      catchError(this.handleError('deleteClient'))
    );
  }

  toggleClientStatus(clientId: string, compteBloque: boolean, documentsComplets: boolean): Observable<any> {
    return this.http.put(`${this.baseUrl}/clients/${clientId}/status`, null, {
      headers: this.getAuthHeaders(),
      params: {
        compteBloque: compteBloque.toString(),
        documentsComplets: documentsComplets.toString()
      },
      responseType: 'text' as 'json'
    });
  }
}
