import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  form: any = {
    username: '',
    email: '',
    password: ''
  };
  isLoading = false;
  isSuccessful = false;
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.isSuccessful = false;

    this.authService.register(this.form).subscribe({
      next: data => {
        this.isLoading = false;
        this.isSuccessful = true;
        console.log('Regisration successful', data);
      },
      error: err => {
        this.isLoading = false;
        this.isSuccessful = false;
        this.errorMessage = err.error.message || err.message || 'Registration failed. Please try again';
        console.error('Registratrion error', err);
      }
    });
  }
}
