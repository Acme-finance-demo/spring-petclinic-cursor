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
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controller for vet schedule and holiday management.
 *
 * @author Spring Petclinic Team
 */
@Controller
public class ScheduleController {

	private static final String VIEWS_SCHEDULE_MANAGEMENT = "schedules/scheduleManagement";
	private static final String VIEWS_SCHEDULE_FORM = "schedules/scheduleForm";
	private static final String VIEWS_HOLIDAY_FORM = "schedules/holidayForm";

	private final ScheduleService scheduleService;
	private final VetRepository vetRepository;

	@Autowired
	public ScheduleController(ScheduleService scheduleService, VetRepository vetRepository) {
		this.scheduleService = scheduleService;
		this.vetRepository = vetRepository;
	}

	/**
	 * Display vet schedule management page.
	 */
	@GetMapping("/vets/{vetId}/schedule")
	public String showVetScheduleManagement(@PathVariable Integer vetId, Model model) {
		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		if (vetOpt.isEmpty()) {
			return "redirect:/vets";
		}

		Vet vet = vetOpt.get();
		Collection<VetSchedule> schedules = scheduleService.findSchedulesByVetId(vetId);
		Collection<VetHoliday> holidays = scheduleService.findFutureHolidaysByVetId(vetId);

		model.addAttribute("vet", vet);
		model.addAttribute("schedules", schedules);
		model.addAttribute("holidays", holidays);

		// Create empty objects for forms
		model.addAttribute("newSchedule", new VetSchedule());
		model.addAttribute("newHoliday", new VetHoliday());

		return VIEWS_SCHEDULE_MANAGEMENT;
	}

	/**
	 * Display new schedule form.
	 */
	@GetMapping("/vets/{vetId}/schedule/new")
	public String showNewScheduleForm(@PathVariable Integer vetId, Model model) {
		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		if (vetOpt.isEmpty()) {
			return "redirect:/vets";
		}

		VetSchedule schedule = new VetSchedule();
		schedule.setVet(vetOpt.get());

		model.addAttribute("schedule", schedule);
		model.addAttribute("vet", vetOpt.get());

		return VIEWS_SCHEDULE_FORM;
	}

	/**
	 * Process new schedule form submission.
	 */
	@PostMapping("/vets/{vetId}/schedule/new")
	public String processNewScheduleForm(
		@PathVariable Integer vetId,
		@Valid @ModelAttribute("schedule") VetSchedule schedule,
		BindingResult result,
		RedirectAttributes redirectAttributes,
		Model model) {

		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		if (vetOpt.isEmpty()) {
			return "redirect:/vets";
		}

		schedule.setVet(vetOpt.get());

		// Check for existing schedule on the same day
		Optional<VetSchedule> existingSchedule = scheduleService.findScheduleByVetIdAndDay(vetId, schedule.getDayOfWeek());
		if (existingSchedule.isPresent()) {
			result.rejectValue("dayOfWeek", "error.schedule.duplicate", "Schedule already exists for this day");
		}

		// Validate time range
		if (schedule.getStartTime() != null && schedule.getEndTime() != null &&
			!schedule.getStartTime().isBefore(schedule.getEndTime())) {
			result.rejectValue("endTime", "error.schedule.timeRange", "End time must be after start time");
		}

		if (result.hasErrors()) {
			model.addAttribute("vet", vetOpt.get());
			return VIEWS_SCHEDULE_FORM;
		}

		scheduleService.saveVetSchedule(schedule);
		redirectAttributes.addFlashAttribute("message", "Schedule created successfully!");
		return "redirect:/vets/" + vetId + "/schedule";
	}

	/**
	 * Display edit schedule form.
	 */
	@GetMapping("/vets/{vetId}/schedule/{scheduleId}/edit")
	public String showEditScheduleForm(
		@PathVariable Integer vetId,
		@PathVariable Integer scheduleId,
		Model model) {

		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		Optional<VetSchedule> scheduleOpt = scheduleService.findVetScheduleById(scheduleId);

		if (vetOpt.isEmpty() || scheduleOpt.isEmpty()) {
			return "redirect:/vets/" + vetId + "/schedule";
		}

		model.addAttribute("schedule", scheduleOpt.get());
		model.addAttribute("vet", vetOpt.get());

		return VIEWS_SCHEDULE_FORM;
	}

	/**
	 * Process edit schedule form submission.
	 */
	@PostMapping("/vets/{vetId}/schedule/{scheduleId}/edit")
	public String processEditScheduleForm(
		@PathVariable Integer vetId,
		@PathVariable Integer scheduleId,
		@Valid @ModelAttribute("schedule") VetSchedule schedule,
		BindingResult result,
		RedirectAttributes redirectAttributes,
		Model model) {

		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		if (vetOpt.isEmpty()) {
			return "redirect:/vets";
		}

		schedule.setId(scheduleId);
		schedule.setVet(vetOpt.get());

		// Check for duplicate schedule (excluding current one)
		Optional<VetSchedule> existingSchedule = scheduleService.findScheduleByVetIdAndDay(vetId, schedule.getDayOfWeek());
		if (existingSchedule.isPresent() && !existingSchedule.get().getId().equals(scheduleId)) {
			result.rejectValue("dayOfWeek", "error.schedule.duplicate", "Schedule already exists for this day");
		}

		// Validate time range
		if (schedule.getStartTime() != null && schedule.getEndTime() != null &&
			!schedule.getStartTime().isBefore(schedule.getEndTime())) {
			result.rejectValue("endTime", "error.schedule.timeRange", "End time must be after start time");
		}

		if (result.hasErrors()) {
			model.addAttribute("vet", vetOpt.get());
			return VIEWS_SCHEDULE_FORM;
		}

		scheduleService.saveVetSchedule(schedule);
		redirectAttributes.addFlashAttribute("message", "Schedule updated successfully!");
		return "redirect:/vets/" + vetId + "/schedule";
	}

	/**
	 * Delete a schedule.
	 */
	@PostMapping("/vets/{vetId}/schedule/{scheduleId}/delete")
	public String deleteSchedule(
		@PathVariable Integer vetId,
		@PathVariable Integer scheduleId,
		RedirectAttributes redirectAttributes) {

		scheduleService.deleteVetSchedule(scheduleId);
		redirectAttributes.addFlashAttribute("message", "Schedule deleted successfully!");
		return "redirect:/vets/" + vetId + "/schedule";
	}

	/**
	 * Display new holiday form.
	 */
	@GetMapping("/vets/{vetId}/holidays/new")
	public String showNewHolidayForm(@PathVariable Integer vetId, Model model) {
		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		if (vetOpt.isEmpty()) {
			return "redirect:/vets";
		}

		VetHoliday holiday = new VetHoliday();
		holiday.setVet(vetOpt.get());

		model.addAttribute("holiday", holiday);
		model.addAttribute("vet", vetOpt.get());

		return VIEWS_HOLIDAY_FORM;
	}

	/**
	 * Process new holiday form submission.
	 */
	@PostMapping("/vets/{vetId}/holidays/new")
	public String processNewHolidayForm(
		@PathVariable Integer vetId,
		@Valid @ModelAttribute("holiday") VetHoliday holiday,
		BindingResult result,
		RedirectAttributes redirectAttributes,
		Model model) {

		Optional<Vet> vetOpt = vetRepository.findById(vetId);
		if (vetOpt.isEmpty()) {
			return "redirect:/vets";
		}

		holiday.setVet(vetOpt.get());

		// Validate date is not in the past
		if (holiday.getHolidayDate() != null && holiday.getHolidayDate().isBefore(LocalDate.now())) {
			result.rejectValue("holidayDate", "error.holiday.pastDate", "Holiday date cannot be in the past");
		}

		if (result.hasErrors()) {
			model.addAttribute("vet", vetOpt.get());
			return VIEWS_HOLIDAY_FORM;
		}

		scheduleService.saveVetHoliday(holiday);
		redirectAttributes.addFlashAttribute("message", "Holiday added successfully!");
		return "redirect:/vets/" + vetId + "/schedule";
	}

	/**
	 * Delete a holiday.
	 */
	@PostMapping("/vets/{vetId}/holidays/{holidayId}/delete")
	public String deleteHoliday(
		@PathVariable Integer vetId,
		@PathVariable Integer holidayId,
		RedirectAttributes redirectAttributes) {

		scheduleService.deleteVetHoliday(holidayId);
		redirectAttributes.addFlashAttribute("message", "Holiday deleted successfully!");
		return "redirect:/vets/" + vetId + "/schedule";
	}

}