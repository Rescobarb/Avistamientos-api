package com.isw2.avistamientos_api.Controller;

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

import com.isw2.avistamientos_api.Model.AvistamientoModelo;
import com.isw2.avistamientos_api.Repository.AvistamientoRepository;

import com.isw2.avistamientos_api.Validacion.ValidadorDeAvistamiento;

@RestController
@RequestMapping("/avistamientos")
public class AvistamientoController {

    private final AvistamientoRepository aRepo;
    private final ValidadorDeAvistamiento validador;

    public AvistamientoController(AvistamientoRepository aRepo, ValidadorDeAvistamiento validador) {
        this.aRepo = aRepo;
        this.validador = validador;
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
    public ResponseEntity<AvistamientoModelo> crear(@RequestBody AvistamientoModelo nuevo) {
        validador.validar(nuevo);
        nuevo.setId(null);
        AvistamientoModelo guardado = aRepo.save(nuevo);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/avistamientos/{id}").buildAndExpand(guardado.getId()).toUri();
        return ResponseEntity.created(location).body(guardado);
    }

    @PutMapping("/actualizar{id}")
    public ResponseEntity<AvistamientoModelo> actualizar(@PathVariable Long id, @RequestBody AvistamientoModelo datos) {
        validador.validar(datos);
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