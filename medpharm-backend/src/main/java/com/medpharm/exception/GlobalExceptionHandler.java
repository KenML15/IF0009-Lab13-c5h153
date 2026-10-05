package com.medpharm.exception;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail handleReglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, "Regla de negocio incumplida", ex.getMessage(), req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleCredenciales(BadCredentialsException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNAUTHORIZED, "Credenciales inválidas",
                "Usuario o contraseña incorrectos", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));

        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no cumplen las validaciones", req);
        pd.setProperty("errores", errores);
        return pd;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ProblemDetail handleSolicitudInvalida(Exception ex, HttpServletRequest req) {
        String detalle = ex instanceof HttpMessageNotReadableException
                ? "El cuerpo de la petición no es un JSON válido" : ex.getMessage();
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", detalle, req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex, HttpServletRequest req) {
        if (ex instanceof ErrorResponse er) {
            return er.getBody();
        }
        log.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado en el servidor", req);
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalle, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setTitle(titulo);
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }
}