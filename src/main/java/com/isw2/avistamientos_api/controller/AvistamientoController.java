package com.isw2.avistamientos_api.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.isw2.avistamientos_api.model.AvistamientoModelo;
import com.isw2.avistamientos_api.repository.AvistamientoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/avistamientos")
public class AvistamientoController {

    private final AvistamientoRepository aRepo;

    public AvistamientoController(AvistamientoRepository aRepo) {
        this.aRepo = aRepo;
    }

    @GetMapping
    public List<AvistamientoModelo> listar() {
        return aRepo.findAll();
    }

    @GetMapping("/resumen")
    public Map<String, Long> resumen() {
        return aRepo.findAll().stream()
                .collect(Collectors.groupingBy(AvistamientoModelo::getEspecie, Collectors.counting()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvistamientoModelo> ver(@PathVariable Long id) {
        return aRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/crear")
    public ResponseEntity<AvistamientoModelo> crear(@Valid @RequestBody AvistamientoModelo nuevo) {
        nuevo.setId(null);
        AvistamientoModelo guardado = aRepo.save(nuevo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(guardado.getId()).toUri();
        return ResponseEntity.created(location).body(guardado);
    }

    @PutMapping("/actualizar{id}")
    public ResponseEntity<AvistamientoModelo> actualizar(@PathVariable Long id,
            @Valid @RequestBody AvistamientoModelo datos) {
        return aRepo.findById(id).map(existente -> {
            existente.setEspecie(datos.getEspecie());
            existente.setUbicacion(datos.getUbicacion());
            existente.setFecha(datos.getFecha());
            existente.setObservador(datos.getObservador());
            return ResponseEntity.ok(aRepo.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/eliminar{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!aRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        aRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}