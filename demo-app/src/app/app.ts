import { DOCUMENT } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LoginStore } from './features/login/login.store';

type AppTheme = 'light' | 'dark';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  private readonly loginStore = inject(LoginStore);
  private readonly document = inject(DOCUMENT);

  protected readonly role = this.loginStore.role;

  protected readonly theme = signal<AppTheme>(
    (sessionStorage.getItem('app-theme') as AppTheme | null) ?? 'light'
  );

  protected readonly themeIcon = computed(() =>
    this.theme() === 'light' ? '☀️' : '🌙'
  );

  protected readonly themeLabel = computed(() =>
    this.theme() === 'light' ? 'Light theme' : 'Dark theme'
  );

  private readonly themeEffect = effect(() => {
    const currentTheme = this.theme();

    this.document.body.classList.toggle('light-theme', currentTheme === 'light');
    this.document.body.classList.toggle('dark-theme', currentTheme === 'dark');

    sessionStorage.setItem('app-theme', currentTheme);
  });

  protected isLoginPage(): boolean {
    return this.router.url === '/login' || this.router.url === '/forgot-password';
  }

  protected toggleTheme(): void {
    this.theme.update((currentTheme) =>
      currentTheme === 'light' ? 'dark' : 'light'
    );
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }

}
