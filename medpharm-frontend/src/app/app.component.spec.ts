import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';
import { positivoValidator } from './validators/positivo.validator';
import { FormControl } from '@angular/forms';

describe('AppComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should hide the header when there is no session', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('header')).toBeNull();
  });
});

describe('positivoValidator', () => {
  const validar = (valor: unknown) => positivoValidator(new FormControl(valor));

  it('accepts positive integers and empty values', () => {
    expect(validar(1)).toBeNull();
    expect(validar('5')).toBeNull();
    expect(validar(null)).toBeNull();
  });

  it('rejects zero, negatives and decimals', () => {
    expect(validar(0)).toEqual({ positivo: { valorRecibido: 0 } });
    expect(validar(-3)).not.toBeNull();
    expect(validar(1.5)).not.toBeNull();
  });
});
