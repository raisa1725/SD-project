import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { PersonRole } from '../../models/person.model';

export interface PersonFormDialogData {
  title: string;
  submitLabel?: string;
  showPasswordField?: boolean;
  initialValue?: PersonFormInitialValue | null;
}

export interface PersonFormValue {
  name: string;
  age: number;
  email: string;
  role: PersonRole;
  password?: string;
}

export interface PersonFormInitialValue {
  name: string;
  age: number;
  email: string;
  role?: PersonRole;
}

export type PersonFormDialogResult = PersonFormValue | undefined;

export function nameValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '');
    if (!value) return null;
    return /^[a-zA-ZÀ-ÿ\s'-]+$/.test(value) ? null : { invalidName: true };
  };
}

export function strongPasswordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '');
    if (!value) return null;
    const errors: ValidationErrors = {};
    if (value.length < 10) errors['passwordTooShort'] = true;
    if (!/[A-Z]/.test(value)) errors['passwordNoUpper'] = true;
    if (!/[a-z]/.test(value)) errors['passwordNoLower'] = true;
    if (!/[!@#$%^&*()]/.test(value)) errors['passwordNoSpecial'] = true;
    return Object.keys(errors).length ? errors : null;
  };
}

@Component({
  selector: 'app-person-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
  ],
  templateUrl: './person-form-dialog.component.html',
  styleUrl: './person-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PersonFormDialogComponent>);
  protected readonly data = inject<PersonFormDialogData>(MAT_DIALOG_DATA);

  protected readonly isPasswordVisible = signal(false);
  protected readonly roles: PersonRole[] = ['USER', 'ORGANIZER'];

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100), nameValidator()]],
    age: [0, [Validators.required, Validators.min(18), Validators.max(200)]],
    email: ['', [Validators.required, Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)]],
    role: ['USER' as PersonRole, [Validators.required]],
    password: ['', []],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue(this.data.initialValue);
    }
    if (this.data.showPasswordField) {
      this.form.controls.password.setValidators([Validators.required, strongPasswordValidator()]);
      this.form.controls.password.updateValueAndValidity();
    }
  }

  protected togglePasswordVisibility(): void {
    this.isPasswordVisible.update((v) => !v);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { name, age, email, role, password } = this.form.getRawValue();
    const result: PersonFormValue = this.data.showPasswordField
      ? { name, age, email, role, password }
      : { name, age, email, role };
    this.dialogRef.close(result);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}
