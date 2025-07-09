import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ClientService } from '../../../services/banque.service';
import { ClientSummaryDTO } from '../../models/client-summary.model';

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {
  clients: ClientSummaryDTO[] = [];
  searchQuery: string = '';
  error = '';

  sortField: string = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(
    private clientService: ClientService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log('🔍 ClientListComponent initialized');
    this.checkAuthOnInit();
    this.loadClients();
  }

  // ✅ Auth avec protection contre localStorage en SSR/prerender
  private checkAuthOnInit(): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      const token = localStorage.getItem('token') || localStorage.getItem('authToken') || localStorage.getItem('jwt');
      console.log('🔑 Token check in ClientList:', token ? 'EXISTS' : 'MISSING');

      if (!token) {
        console.error('❌ No token found in ClientList component');
        this.router.navigate(['/login']);
        return;
      }

      try {
        if (token.includes('.')) {
          const payload = JSON.parse(atob(token.split('.')[1]));
          const isExpired = payload.exp && payload.exp < Date.now() / 1000;

          if (isExpired) {
            console.error('🚨 Token is expired in ClientList');
            localStorage.removeItem('token');
            localStorage.removeItem('authToken');
            localStorage.removeItem('jwt');
            this.router.navigate(['/login']);
            return;
          }

          console.log('✅ JWT token is valid, expires at:', new Date(payload.exp * 1000));
        }
      } catch (e) {
        console.log('🔍 Token is not JWT format, proceeding...');
      }
    } else {
      console.warn('🚫 localStorage not available (probably prerendering)');
    }
  }

  loadClients(): void {
    console.log('📥 Loading clients...');

    this.clientService.getAllClients().subscribe({
      next: data => {
        console.log('✅ Clients loaded successfully:', data.length, 'clients');
        this.clients = data;
        this.sortClients();
        this.error = '';
      },
      error: (error) => {
        console.error('❌ Error loading clients:', error);

        if (error.status === 401) {
          console.error('🚨 401 Unauthorized - redirecting to login');
          localStorage.clear();
          this.router.navigate(['/login']);
        } else {
          this.error = 'Erreur lors du chargement des clients.';
        }
      }
    });
  }

  search(): void {
    console.log('🔍 Searching for:', this.searchQuery);

    if (this.searchQuery.trim()) {
      this.clientService.searchClientsByName(this.searchQuery).subscribe({
        next: data => {
          console.log('✅ Search results:', data.length, 'clients found');
          this.clients = data;
          this.sortClients();
          this.error = '';
        },
        error: (error) => {
          console.error('❌ Error searching clients:', error);

          if (error.status === 401) {
            console.error('🚨 401 Unauthorized in search - redirecting to login');
            localStorage.clear();
            this.router.navigate(['/login']);
          } else {
            this.error = 'Erreur lors de la recherche.';
          }
        }
      });
    } else {
      this.loadClients();
    }
  }

  navigateToDetails(client: ClientSummaryDTO): void {
    console.log('🔍 Navigating to client details:', client);

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    console.log('🔑 Token before navigation:', token ? 'EXISTS' : 'MISSING');

    if (!token) {
      console.error('❌ No token found during navigation!');
      this.router.navigate(['/login']);
      return;
    }

    if (!client.clientId) {
      console.error('❌ Client ID is missing:', client);
      this.error = 'ID client manquant';
      return;
    }

    console.log('✅ Navigating to /clients/' + client.clientId);
    this.router.navigate(['/clients', client.clientId]);
  }

  sortClients(): void {
    this.clients.sort((a, b) => {
      let aVal: any;
      let bVal: any;

      switch (this.sortField) {
        case 'name':
          aVal = a.fullName.toLowerCase();
          bVal = b.fullName.toLowerCase();
          break;
        case 'accounts':
          aVal = a.accounts?.length || 0;
          bVal = b.accounts?.length || 0;
          break;
        case 'balance':
          aVal = a.accounts?.reduce((sum, acc) => sum + acc.balance, 0) || 0;
          bVal = b.accounts?.reduce((sum, acc) => sum + acc.balance, 0) || 0;
          break;
        default:
          return 0;
      }

      return this.sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
    });
  }

  getTotalBalance(client: ClientSummaryDTO): number {
    return client.accounts?.reduce((total, acc) => total + acc.balance, 0) || 0;
  }

  toggleSort(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.sortClients();
  }

  refresh(): void {
    console.log('🔄 Manual refresh triggered');
    this.loadClients();
  }
}
