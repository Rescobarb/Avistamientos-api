package com.isw2.avistamientos_api.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "avistamientos")
public class AvistamientoModelo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "La especie es obligatoria")
	private String especie;

	@NotBlank(message = "El lugar es obligatorio")
	@JsonProperty("lugar")
	@Column(name = "ubicacion")
	private String ubicacion;

	@NotNull(message = "La fecha es obligatoria y debe tener formato AAAA-MM-DD")
	private LocalDate fecha;

	@NotBlank(message = "El observador es obligatorio")
	private String observador;

	public AvistamientoModelo() {
	}

	public AvistamientoModelo(String especie, String ubicacion, LocalDate fecha, String observador) {
		this.especie = especie;
		this.ubicacion = ubicacion;
		this.fecha = fecha;
		this.observador = observador;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getEspecie() { return especie; }
	public void setEspecie(String especie) { this.especie = especie; }

	public String getUbicacion() { return ubicacion; }
	public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

	public LocalDate getFecha() { return fecha; }
	public void setFecha(LocalDate fecha) { this.fecha = fecha; }

	public String getObservador() { return observador; }
	public void setObservador(String observador) { this.observador = observador; }

	@Override
	public String toString() {
		return "AvistamientoModelo [id=" + id + ", especie=" + especie + ", ubicacion=" + ubicacion
				+ ", fecha=" + fecha + ", observador=" + observador + "]";
	}
}