package com.isw2.avistamientos_api.Validacion;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.isw2.avistamientos_api.Model.AvistamientoModelo;

@Component
public class ValidadorDeAvistamiento {

    private final Reloj reloj;

    public ValidadorDeAvistamiento(Reloj reloj) {
        this.reloj = reloj;
    }

    public void validar(AvistamientoModelo a) {
        validarEspecie(a.getEspecie());
        validarFecha(a.getFecha());
        validarNoVacio("lugar", a.getUbicacion());
        validarNoVacio("observador", a.getObservador());
    }

    private void validarEspecie(String especie) {
        if (especie == null || especie.isBlank()
                || especie.length() < 3 || especie.length() > 50) {
            throw new AvistamientoInvalidoException("especie",
                    "La especie debe tener entre 3 y 50 caracteres");
        }
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new AvistamientoInvalidoException("fecha",
                    "La fecha es obligatoria y debe tener formato AAAA-MM-DD");
        }
        if (fecha.isAfter(reloj.hoy())) {
            throw new AvistamientoInvalidoException("fecha",
                    "La fecha no puede ser posterior a hoy");
        }
    }

    private void validarNoVacio(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new AvistamientoInvalidoException(campo,
                    "El campo " + campo + " no puede estar vacío");
        }
    }
}