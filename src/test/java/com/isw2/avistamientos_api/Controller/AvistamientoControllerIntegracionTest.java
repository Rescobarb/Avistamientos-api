package com.isw2.avistamientos_api.Controller;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.isw2.avistamientos_api.Model.AvistamientoModelo;
import com.isw2.avistamientos_api.Repository.AvistamientoRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AvistamientoControllerIntegracionTest {

    private static final long ID_INEXISTENTE = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvistamientoRepository repo;

    @BeforeEach
    void limpiarBaseDeDatos() {
        // Independencia: cada prueba arranca con la tabla vacía
        repo.deleteAll();
    }

    // ---------- Utilidades ----------

    private AvistamientoModelo guardar(String especie) {
        return repo.save(new AvistamientoModelo(especie, "Bogota", LocalDate.of(2020, 1, 15), "Ana"));
    }

    private String json(String especie, String lugar, String fecha, String observador) {
        return String.format(
                "{\"especie\":\"%s\",\"lugar\":\"%s\",\"fecha\":\"%s\",\"observador\":\"%s\"}",
                especie, lugar, fecha, observador);
    }

    private String jsonValido() {
        return json("Colibri", "Bogota", "2020-01-15", "Ana");
    }

    // =====================================================
    // GET /avistamientos
    // =====================================================

    @Test
    @DisplayName("GET /avistamientos responde 200 y una lista vacía si no hay datos")
    void listarSinDatosDevuelveListaVacia() throws Exception {
        // Arrange: tabla vacía (la deja el @BeforeEach)

        // Act + Assert
        mockMvc.perform(get("/avistamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /avistamientos responde 200 con todos los avistamientos guardados")
    void listarConDatosDevuelveTodos() throws Exception {
        // Arrange
        guardar("Colibri");
        guardar("Aguila");

        // Act + Assert
        mockMvc.perform(get("/avistamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].especie").value("Colibri"))
                .andExpect(jsonPath("$[1].especie").value("Aguila"));
    }

    // =====================================================
    // GET /avistamientos/{id}
    // =====================================================

    @Test
    @DisplayName("GET /avistamientos/{id} responde 200 con todos los campos del avistamiento")
    void verExistenteDevuelveElCuerpoCompleto() throws Exception {
        // Arrange
        AvistamientoModelo guardado = guardar("Colibri");

        // Act + Assert
        mockMvc.perform(get("/avistamientos/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.especie").value("Colibri"))
                .andExpect(jsonPath("$.lugar").value("Bogota"))
                .andExpect(jsonPath("$.fecha").value("2020-01-15"))
                .andExpect(jsonPath("$.observador").value("Ana"));
    }

    @Test
    @DisplayName("GET /avistamientos/{id} responde 404 si el id no existe")
    void verInexistenteResponde404() throws Exception {
        // Arrange: tabla vacía

        // Act + Assert
        mockMvc.perform(get("/avistamientos/" + ID_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // POST /avistamientos/crear
    // =====================================================

    @Test
    @DisplayName("POST crear con datos válidos responde 201 y el GET posterior lo devuelve")
    void crearValidoResponde201YQuedaConsultable() throws Exception {
        // Arrange
        String cuerpo = jsonValido();

        // Act
        MvcResult resultado = mockMvc.perform(post("/avistamientos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.especie").value("Colibri"))
                .andExpect(jsonPath("$.lugar").value("Bogota"))
                .andExpect(jsonPath("$.fecha").value("2020-01-15"))
                .andExpect(jsonPath("$.observador").value("Ana"))
                .andReturn();
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");

        // Assert: el efecto, el GET al nuevo id lo devuelve
        mockMvc.perform(get("/avistamientos/" + id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especie").value("Colibri"));
    }

    @Test
    @DisplayName("POST crear responde con un header Location que apunta a un recurso consultable")
    void crearDevuelveLocationConsultable() throws Exception {
        // Arrange
        String cuerpo = jsonValido();

        // Act
        MvcResult resultado = mockMvc.perform(post("/avistamientos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn();
        String location = resultado.getResponse().getHeader("Location");

        // Assert: seguir el Location debe devolver el avistamiento creado
        mockMvc.perform(get(URI.create(location)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especie").value("Colibri"));
    }

    @Test
    @DisplayName("POST crear con especie de 2 caracteres responde 400 indicando el campo especie")
    void crearConEspecieCortaResponde400() throws Exception {
        // Arrange
        String cuerpo = json("ab", "Bogota", "2020-01-15", "Ana");

        // Act + Assert
        mockMvc.perform(post("/avistamientos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("especie"))
                .andExpect(jsonPath("$.mensaje", notNullValue()));
    }

    @Test
    @DisplayName("POST crear con fecha futura responde 400 indicando el campo fecha")
    void crearConFechaFuturaResponde400() throws Exception {
        // Arrange
        String cuerpo = json("Colibri", "Bogota", "2999-12-31", "Ana");

        // Act + Assert
        mockMvc.perform(post("/avistamientos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("fecha"));
    }

    @Test
    @DisplayName("POST crear con fecha en formato DD/MM/AAAA responde 400 indicando el campo fecha")
    void crearConFormatoDeFechaInvalidoResponde400() throws Exception {
        // Arrange: esto lo rechaza la deserialización, no el validador
        String cuerpo = json("Colibri", "Bogota", "21/10/2026", "Ana");

        // Act + Assert
        mockMvc.perform(post("/avistamientos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("fecha"));
    }

    @Test
    @DisplayName("POST crear con datos inválidos no guarda nada en la base")
    void crearInvalidoNoGuardaNada() throws Exception {
        // Arrange
        String cuerpo = json("ab", "Bogota", "2020-01-15", "Ana");

        // Act
        mockMvc.perform(post("/avistamientos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());

        // Assert: el efecto, no se persistió nada
        assertEquals(0, repo.count());
    }

    // =====================================================
    // PUT /avistamientos/actualizar{id}
    // =====================================================

    @Test
    @DisplayName("PUT actualizar con datos válidos responde 200 y el cambio queda guardado")
    void actualizarValidoResponde200YPersiste() throws Exception {
        // Arrange
        AvistamientoModelo guardado = guardar("Colibri");
        String cuerpo = json("Aguila", "Leticia", "2021-05-10", "Luis");

        // Act
        mockMvc.perform(put("/avistamientos/actualizar" + guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.especie").value("Aguila"))
                .andExpect(jsonPath("$.lugar").value("Leticia"))
                .andExpect(jsonPath("$.fecha").value("2021-05-10"))
                .andExpect(jsonPath("$.observador").value("Luis"));

        // Assert: el efecto, un GET posterior ve los datos nuevos
        mockMvc.perform(get("/avistamientos/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especie").value("Aguila"))
                .andExpect(jsonPath("$.observador").value("Luis"));
    }

    @Test
    @DisplayName("PUT actualizar con un id inexistente responde 404")
    void actualizarInexistenteResponde404() throws Exception {
        // Arrange
        String cuerpo = jsonValido();

        // Act + Assert
        mockMvc.perform(put("/avistamientos/actualizar" + ID_INEXISTENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT actualizar con datos inválidos responde 400 y no modifica el registro")
    void actualizarInvalidoResponde400YNoModifica() throws Exception {
        // Arrange
        AvistamientoModelo guardado = guardar("Colibri");
        String cuerpo = json("ab", "Leticia", "2021-05-10", "Luis");

        // Act
        mockMvc.perform(put("/avistamientos/actualizar" + guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("especie"));

        // Assert: el registro sigue igual
        mockMvc.perform(get("/avistamientos/" + guardado.getId()))
                .andExpect(jsonPath("$.especie").value("Colibri"))
                .andExpect(jsonPath("$.observador").value("Ana"));
    }

    @Test
    @DisplayName("PUT actualizar con datos inválidos y id inexistente responde 400 (se valida antes de buscar)")
    void actualizarInvalidoEInexistenteResponde400() throws Exception {
        // Arrange
        String cuerpo = json("ab", "Bogota", "2020-01-15", "Ana");

        // Act + Assert
        mockMvc.perform(put("/avistamientos/actualizar" + ID_INEXISTENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    // =====================================================
    // DELETE /avistamientos/eliminar{id}
    // =====================================================

    @Test
    @DisplayName("DELETE eliminar responde 204 y el GET posterior responde 404")
    void eliminarExistenteResponde204YDejaDeExistir() throws Exception {
        // Arrange
        AvistamientoModelo guardado = guardar("Colibri");

        // Act
        mockMvc.perform(delete("/avistamientos/eliminar" + guardado.getId()))
                .andExpect(status().isNoContent());

        // Assert: el efecto
        mockMvc.perform(get("/avistamientos/" + guardado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE eliminar con un id inexistente responde 404")
    void eliminarInexistenteResponde404() throws Exception {
        // Arrange: tabla vacía

        // Act + Assert
        mockMvc.perform(delete("/avistamientos/eliminar" + ID_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // GET /avistamientos/resumen
    // =====================================================

    @Test
    @DisplayName("GET resumen responde 200 con el conteo de avistamientos por especie")
    void resumenCuentaAvistamientosPorEspecie() throws Exception {
        // Arrange
        guardar("Colibri");
        guardar("Colibri");
        guardar("Aguila");

        // Act + Assert
        mockMvc.perform(get("/avistamientos/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Colibri").value(2))
                .andExpect(jsonPath("$.Aguila").value(1));
    }
}