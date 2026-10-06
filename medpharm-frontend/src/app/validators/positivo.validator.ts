import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const positivoValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;

  if (valor === null || valor === undefined || valor === '') {
    return null;
  }

  const numero = Number(valor);
  const esValido = Number.isInteger(numero) && numero > 0;

  return esValido ? null : { positivo: { valorRecibido: valor } };
};