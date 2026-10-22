package com.isw2.avistamientos_api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.isw2.avistamientos_api.Model.AvistamientoModelo;

public interface AvistamientoRepository extends JpaRepository<AvistamientoModelo, Long>{

}
