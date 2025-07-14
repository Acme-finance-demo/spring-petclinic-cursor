/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Simple JavaBean domain object representing an appointment.
 *
 * @author Spring Petclinic Team
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "pet_id")
	@NotNull
	private Pet pet;

	@ManyToOne
	@JoinColumn(name = "vet_id")
	@NotNull
	private Vet vet;

	@Column(name = "appointment_date")
	@NotNull
	private LocalDate appointmentDate;

	@Column(name = "appointment_time")
	@NotNull
	private LocalTime appointmentTime;

	@Column(name = "duration_minutes")
	private Integer durationMinutes = 30;

	@Column(name = "reason")
	private String reason;

	@Column(name = "symptoms", columnDefinition = "CLOB")
	private String symptoms;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private AppointmentStatus status = AppointmentStatus.PENDING;

	@Column(name = "notes", columnDefinition = "CLOB")
	private String notes;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public Vet getVet() {
		return vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDate appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public LocalTime getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(LocalTime appointmentTime) {
		this.appointmentTime = appointmentTime;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getSymptoms() {
		return symptoms;
	}

	public void setSymptoms(String symptoms) {
		this.symptoms = symptoms;
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Convenience method to get the appointment date and time as a single LocalDateTime
	 */
	public LocalDateTime getAppointmentDateTime() {
		if (appointmentDate != null && appointmentTime != null) {
			return LocalDateTime.of(appointmentDate, appointmentTime);
		}
		return null;
	}

	/**
	 * Convenience method to get the end time of the appointment
	 */
	public LocalDateTime getAppointmentEndDateTime() {
		LocalDateTime startDateTime = getAppointmentDateTime();
		if (startDateTime != null && durationMinutes != null) {
			return startDateTime.plusMinutes(durationMinutes);
		}
		return null;
	}

	/**
	 * Check if this appointment conflicts with another appointment
	 */
	public boolean conflictsWith(Appointment other) {
		if (other == null || !this.vet.equals(other.vet) || !this.appointmentDate.equals(other.appointmentDate)) {
			return false;
		}
		
		LocalDateTime thisStart = this.getAppointmentDateTime();
		LocalDateTime thisEnd = this.getAppointmentEndDateTime();
		LocalDateTime otherStart = other.getAppointmentDateTime();
		LocalDateTime otherEnd = other.getAppointmentEndDateTime();
		
		if (thisStart == null || thisEnd == null || otherStart == null || otherEnd == null) {
			return false;
		}
		
		return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
	}

}