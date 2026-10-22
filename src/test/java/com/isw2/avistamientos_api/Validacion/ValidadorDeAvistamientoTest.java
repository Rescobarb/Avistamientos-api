package com.isw2.avistamientos_api.Validacion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.isw2.avistamientos_api.Model.AvistamientoModelo;

class ValidadorDeAvistamientoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 21);

    private ValidadorDeAvistamiento validador;

    @BeforeEach
    void preparar() {
        // El stub fija "hoy" en 2026-10-21 para todas las pruebas
        validador = new ValidadorDeAvistamiento(new RelojFijo(HOY));
    }

    // ---------- Utilidades ----------

    /** Avistamiento válido (datos base de la tabla de casos). */
    private AvistamientoModelo valido() {
        return new AvistamientoModelo("Colibrí", "Bogotá", LocalDate.of(2026, 10, 20), "Ana");
    }

    /** Verifica que se rechaza el avistamiento y que el campo reportado es el esperado. */
    private void assertRechazaCampo(AvistamientoModelo a, String campoEsperado) {
        AvistamientoInvalidoException e =
                assertThrows(AvistamientoInvalidoException.class, () -> validador.validar(a));
        assertEquals(campoEsperado, e.getCampo());
    }

    // =====================================================
    // ESPECIE (tabla A1): válida entre 3 y 50 caracteres
    // =====================================================

    @Test
    @DisplayName("E1: acepta una especie representativa de 7 caracteres")
    void aceptaEspecieRepresentativa() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setEspecie("Colibrí");

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @Test
    @DisplayName("E3: rechaza una especie de 2 caracteres (límite inferior, lado inválido)")
    void rechazaEspecieDeDosCaracteres() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setEspecie("ab");

        // Act + Assert
        assertRechazaCampo(a, "especie");
    }

    @Test
    @DisplayName("E4: acepta una especie de 3 caracteres (límite inferior, lado válido)")
    void aceptaEspecieDeTresCaracteres() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setEspecie("abc");

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @Test
    @DisplayName("E5: acepta una especie de 50 caracteres (límite superior, lado válido)")
    void aceptaEspecieDeCincuentaCaracteres() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setEspecie("a".repeat(50));

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @Test
    @DisplayName("E6: rechaza una especie de 51 caracteres (límite superior, lado inválido)")
    void rechazaEspecieDeCincuentaYUnCaracteres() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setEspecie("a".repeat(51));

        // Act + Assert
        assertRechazaCampo(a, "especie");
    }

    @ParameterizedTest(name = "[{index}] especie = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = { "     " })
    @DisplayName("E2, E7, E8: rechaza una especie nula, vacía o de solo espacios")
    void rechazaEspecieNulaVaciaOEnBlanco(String especie) {
        // Arrange
        AvistamientoModelo a = valido();
        a.setEspecie(especie);

        // Act + Assert
        assertRechazaCampo(a, "especie");
    }

    // =====================================================
    // FECHA (tabla A2): no puede ser posterior a hoy (2026-10-21)
    // =====================================================

    @Test
    @DisplayName("F1: acepta la fecha de ayer (último día claramente pasado)")
    void aceptaFechaDeAyer() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setFecha(HOY.minusDays(1));

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @Test
    @DisplayName("F2: acepta la fecha de hoy (frontera exacta)")
    void aceptaFechaDeHoy() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setFecha(HOY);

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @Test
    @DisplayName("F3: rechaza la fecha de mañana (primer día inválido)")
    void rechazaFechaDeManana() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setFecha(HOY.plusDays(1));

        // Act + Assert
        assertRechazaCampo(a, "fecha");
    }

    @Test
    @DisplayName("F4: acepta una fecha del pasado lejano")
    void aceptaFechaDelPasadoLejano() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setFecha(LocalDate.of(2020, 1, 15));

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @Test
    @DisplayName("F5: rechaza una fecha del futuro lejano")
    void rechazaFechaDelFuturoLejano() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setFecha(LocalDate.of(2030, 1, 1));

        // Act + Assert
        assertRechazaCampo(a, "fecha");
    }

    @Test
    @DisplayName("F6: rechaza una fecha nula")
    void rechazaFechaNula() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setFecha(null);

        // Act + Assert
        assertRechazaCampo(a, "fecha");
    }

    // =====================================================
    // LUGAR (tabla A3): no vacío
    // =====================================================

    @Test
    @DisplayName("L1: acepta un lugar representativo")
    void aceptaLugarRepresentativo() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setUbicacion("Bogotá");

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @ParameterizedTest(name = "[{index}] lugar = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("L2, L3, L4: rechaza un lugar nulo, vacío o de solo espacios")
    void rechazaLugarNuloVacioOEnBlanco(String lugar) {
        // Arrange
        AvistamientoModelo a = valido();
        a.setUbicacion(lugar);

        // Act + Assert
        assertRechazaCampo(a, "lugar");
    }

    // =====================================================
    // OBSERVADOR (tabla A3): no vacío
    // =====================================================

    @Test
    @DisplayName("O1: acepta un observador representativo")
    void aceptaObservadorRepresentativo() {
        // Arrange
        AvistamientoModelo a = valido();
        a.setObservador("Ana");

        // Act + Assert
        assertDoesNotThrow(() -> validador.validar(a));
    }

    @ParameterizedTest(name = "[{index}] observador = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("O2, O3, O4: rechaza un observador nulo, vacío o de solo espacios")
    void rechazaObservadorNuloVacioOEnBlanco(String observador) {
        // Arrange
        AvistamientoModelo a = valido();
        a.setObservador(observador);

        // Act + Assert
        assertRechazaCampo(a, "observador");
    }

    // =====================================================
    // INTERACCIÓN (tabla A4)
    // =====================================================

    @Test
    @DisplayName("X1: con todos los campos inválidos, reporta primero la especie")
    void reportaPrimeroLaEspecieCuandoTodoEsInvalido() {
        // Arrange
        AvistamientoModelo a = new AvistamientoModelo("ab", "", HOY.plusDays(1), "");

        // Act + Assert
        assertRechazaCampo(a, "especie");
    }
}