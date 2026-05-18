import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, finalize, Observable, of, tap } from 'rxjs';
import { LoginRequest, LoginResponse, LoginService } from '../../services/login.service';

type UserRole = 'USER' | 'ORGANIZER' | 'ADMIN';

interface AuthSnapshot {
  role: UserRole | null;
  email: string | null;
  token: string;
}

const STORAGE_KEY = 'demo-app-auth';

@Injectable({ providedIn: 'root' })
export class LoginStore {
  private readonly loginService = inject(LoginService);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly token = signal<string | null>(null);
  readonly isAuthenticated = computed(() => this.token() !== null);
  readonly role = signal<UserRole | null>(null);
  readonly email = signal<string | null>(null);

  constructor() {
    this.restoreAuthState();
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    return this.loginService.login(request).pipe(
      tap((response) => this.applyResponse(response, request.email)),
      catchError((error: unknown) => {
        const response = this.normalizeError(error);
        this.applyResponse(response, null);
        return of(response);
      }),
      finalize(() => this.isSubmitting.set(false)),
    );
  }

  logout(): void {
    this.clearSession();
  }

  private applyResponse(response: LoginResponse, email: string | null): void {
    if (response.success && response.token) {
      this.token.set(response.token);
      this.role.set((response.role as UserRole) ?? null);
      this.email.set(email);
      this.errorMessage.set(null);
      this.persistAuthState();
      return;
    }

    this.clearSession(response.errorMessage ?? 'Login failed. Please try again.');
  }

  private normalizeError(error: unknown): LoginResponse {
    if (error instanceof HttpErrorResponse && error.error) {
      const maybeError = error.error as Partial<LoginResponse>;

      if (typeof maybeError.success === 'boolean') {
        return {
          success: maybeError.success,
          role: maybeError.role ?? null,
          token: maybeError.token ?? null,
          errorMessage:
            maybeError.errorMessage ??
            (error.status === 401
              ? 'Invalid email or password.'
              : 'Unable to complete login. Please try again.'),
        };
      }
    }

    return {
      success: false,
      role: null,
      token: null,
      errorMessage: 'Unable to complete login. Please try again.',
    };
  }

  private restoreAuthState(): void {
    const stored = sessionStorage.getItem(STORAGE_KEY);

    if (!stored) {
      return;
    }

    try {
      const snapshot = JSON.parse(stored) as AuthSnapshot;

      if (!snapshot.token) {
        this.clearSession();
        return;
      }

      this.token.set(snapshot.token);
      this.role.set(snapshot.role ?? null);
      this.email.set(snapshot.email ?? null);

      sessionStorage.setItem('token', snapshot.token);
      sessionStorage.setItem('role', snapshot.role ?? '');
      sessionStorage.setItem('email', snapshot.email ?? '');
    } catch {
      this.clearSession();
    }
  }

  private persistAuthState(): void {
    const token = this.token();

    if (!token) {
      return;
    }

    const snapshot: AuthSnapshot = {
      role: this.role(),
      email: this.email(),
      token,
    };

    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));

    sessionStorage.setItem('token', token);
    sessionStorage.setItem('role', this.role() ?? '');
    sessionStorage.setItem('email', this.email() ?? '');
  }

  private clearSession(errorMessage: string | null = null): void {
    this.token.set(null);
    this.role.set(null);
    this.email.set(null);
    this.errorMessage.set(errorMessage);

    sessionStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('role');
    sessionStorage.removeItem('email');
  }
}
