package com.isw2.avistamientos_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.isw2.avistamientos_api.model.AvistamientoModelo;

public interface AvistamientoRepository extends JpaRepository<AvistamientoModelo, Long>{

}
