// components/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClientService} from '../../../services/banque.service'
import {DebugAuthComponent} from '../DebugAuthComponent/debugAuthComponent';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls:['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  clientCount: number = 0;
  accountCount: number = 0;

  constructor(
    private clientService: ClientService
  ) {}

  ngOnInit(): void {
    this.clientService.getClientCount().subscribe(count => this.clientCount = count);
    this.clientService.getAccountCount().subscribe(count => this.accountCount = count);
  }
}
