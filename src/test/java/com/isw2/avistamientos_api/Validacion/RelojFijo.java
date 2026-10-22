package com.isw2.avistamientos_api.Validacion;

import java.time.LocalDate;

/**
 * Stub: un doble de prueba que devuelve siempre la misma fecha.
 * Hace que las pruebas de "hoy" y "mañana" no dependan del día real.
 */
public class RelojFijo implements Reloj {

    private final LocalDate fecha;

    public RelojFijo(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public LocalDate hoy() {
        return fecha;
    }
}