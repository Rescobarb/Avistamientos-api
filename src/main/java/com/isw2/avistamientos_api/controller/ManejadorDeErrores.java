package com.isw2.avistamientos_api.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.isw2.avistamientos_api.Validacion.AvistamientoInvalidoException;

@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(AvistamientoInvalidoException.class)
    public ResponseEntity<Map<String, String>> invalido(AvistamientoInvalidoException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("campo", e.getCampo(), "mensaje", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> cuerpoIlegible(HttpMessageNotReadableException e) {
        if (esErrorDeFecha(e)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "campo", "fecha",
                    "mensaje", "La fecha tiene un formato inválido (use AAAA-MM-DD)"));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "campo", "cuerpo",
                "mensaje", "El cuerpo de la petición no es un JSON válido"));
    }

    private boolean esErrorDeFecha(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String m = String.valueOf(t.getMessage());
            if (m.contains("fecha") || m.contains("LocalDate") || m.contains("DateTimeParseException")) {
                return true;
            }
        }
        return false;
    }
}