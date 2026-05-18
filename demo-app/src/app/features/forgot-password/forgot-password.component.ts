import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { PasswordResetService } from '../../services/password-reset.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  private readonly passwordResetService = inject(PasswordResetService);
  private readonly router = inject(Router);

  protected readonly email = signal('');
  protected readonly code = signal('');
  protected readonly newPassword = signal('');
  protected readonly confirmPassword = signal('');

  protected readonly message = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly isSubmitting = signal(false);

  protected sendCode(): void {
    this.message.set(null);
    this.error.set(null);

    const email = this.email().trim();

    if (!email) {
      this.error.set('Email is required.');
      return;
    }

    this.isSubmitting.set(true);

    this.passwordResetService.forgotPassword(email).subscribe({
      next: (response) => {
        this.message.set(response.message);
      },
      error: (err) => {
        this.error.set(
          err.error?.error ||
          err.error?.message ||
          err.error?.email ||
          'Could not send reset code.'
        );
      },
      complete: () => {
        this.isSubmitting.set(false);
      },
    });
  }

  protected resetPassword(): void {
    this.message.set(null);
    this.error.set(null);

    const email = this.email().trim();
    const code = this.code().trim();
    const newPassword = this.newPassword();
    const confirmPassword = this.confirmPassword();

    if (!email) {
      this.error.set('Email is required.');
      return;
    }

    if (!code) {
      this.error.set('Reset code is required.');
      return;
    }

    if (!newPassword || !confirmPassword) {
      this.error.set('Both password fields are required.');
      return;
    }

    if (newPassword !== confirmPassword) {
      this.error.set('Passwords do not match.');
      return;
    }

    this.isSubmitting.set(true);

    this.passwordResetService
      .resetPassword({
        email,
        code,
        newPassword,
        confirmPassword,
      })
      .subscribe({
        next: (response) => {
          this.message.set(response.message);

          setTimeout(() => {
            void this.router.navigate(['/login']);
          }, 1200);
        },
        error: (err) => {
          this.error.set(
            err.error?.error ||
            err.error?.message ||
            err.error?.newPassword ||
            err.error?.confirmPassword ||
            'Could not reset password.'
          );
        },
        complete: () => {
          this.isSubmitting.set(false);
        },
      });
  }
}
