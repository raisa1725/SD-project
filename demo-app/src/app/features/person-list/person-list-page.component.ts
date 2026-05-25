import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';

import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  PersonFormDialogComponent,
  PersonFormDialogData,
  PersonFormDialogResult
} from '../../components/person-form-dialog/person-form-dialog.component';

import { CreatePersonDto, Person, UpdatePersonDto } from '../../models/person.model';
import { PersonListStore } from './person-list.store';
import { LoginStore } from '../login/login.store';

@Component({
  selector: 'app-person-list-page',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './person-list-page.component.html',
  styleUrl: './person-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonListPageComponent {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(PersonListStore);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly persons = this.store.persons;
  protected readonly roleRequests = computed(() =>
    this.persons().filter((person) => person.requestedRole)
  );
  protected readonly selectedSort = signal<'alphabetical' | 'role'>('alphabetical');

  protected readonly sortedPersons = computed(() => {
    const persons = [...this.persons()];

    if (this.selectedSort() === 'alphabetical') {
      return persons.sort((a, b) => a.name.localeCompare(b.name));
    }

    const roleOrder: Record<Person['role'], number> = {
      ADMIN: 1,
      ORGANIZER: 2,
      USER: 3,
    };

    return persons.sort((a, b) => {
      const roleComparison = roleOrder[a.role] - roleOrder[b.role];

      if (roleComparison !== 0) {
        return roleComparison;
      }

      return a.name.localeCompare(b.name);
    });
  });

  protected changeSort(sort: 'alphabetical' | 'role'): void {
    this.selectedSort.set(sort);
  }
  protected readonly hasError = this.store.hasError;
  protected readonly isLoading = this.store.isLoading;

  protected readonly roles = ['USER', 'ORGANIZER', 'ADMIN'] as const;

  protected readonly displayedColumns = [
    'name',
    'age',
    'email',
    'role',
    'actions',
  ];

  constructor() {
    this.store.load();
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) {
      return;
    }

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Create Person',
            submitLabel: 'Create',
            showPasswordField: true,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) {
          return;
        }

        const dto: CreatePersonDto = {
          name: result.name,
          age: result.age,
          email: result.email,
          password: result.password ?? '',
          role: result.role,
        };

        this.store.create(dto);
      });
  }

  protected openEditDialog(person: Person): void {
    if (this.isLoading()) {
      return;
    }

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Edit Person',
            submitLabel: 'Save',
            showPasswordField: false,
            initialValue: {
              name: person.name,
              age: person.age,
              email: person.email,
              role: person.role,
            },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) {
          return;
        }

        const dto: UpdatePersonDto = {
          name: result.name,
          age: result.age,
          email: result.email,
          role: result.role,
        };

        this.store.update(person.id, dto);
      });
  }

  protected updateRole(person: Person, role: Person['role']): void {
    if (this.isLoading()) {
      return;
    }

    const dto: UpdatePersonDto = {
      name: person.name,
      age: person.age,
      email: person.email,
      role: role,
    };

    this.store.update(person.id, dto);
  }

  protected openDeleteDialog(person: Person): void {
    if (this.isLoading()) {
      return;
    }

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: Person }, boolean>(
        ConfirmDeleteDialogComponent,
        {
          data: { person },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.store.remove(person.id);
      });
  }

  protected acceptRoleRequest(person: Person): void {
    if (this.isLoading()) {
      return;
    }

    this.store.acceptRoleRequest(person.id);
  }

  protected declineRoleRequest(person: Person): void {
    if (this.isLoading()) {
      return;
    }

    this.store.declineRoleRequest(person.id);
  }
}
