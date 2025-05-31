import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  form: any = {
    username: '',
    password: ''
  };
  isLoading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.authService.login(this.form).subscribe({
      next: data => {
        this.isLoading = false;
        console.log('Login successful', data);
        this.router.navigate(['/products']);
      },
      error: err => {
        this.isLoading = false;
        this.errorMessage = err.error.message || err.message || 'Login failed. Please check your credentials.';
        console.error('Login error', err);
      }
    });
  }
}
