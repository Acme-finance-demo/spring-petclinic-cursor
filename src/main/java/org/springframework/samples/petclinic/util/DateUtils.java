package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility class for date calculations and operations.
 * Provides common date manipulation methods for the Pet Clinic application.
 */
public final class DateUtils {

	private DateUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Calculates the age in years from a birth date to the current date.
	 * @param birthDate the birth date
	 * @return the age in years
	 */
	public static int calculateAge(LocalDate birthDate) {
		Objects.requireNonNull(birthDate, "Birth date cannot be null");
		return Period.between(birthDate, LocalDate.now()).getYears();
	}

	/**
	 * Calculates the age in years from a birth date to a specific date.
	 * @param birthDate the birth date
	 * @param currentDate the current date to calculate age against
	 * @return the age in years
	 */
	public static int calculateAge(LocalDate birthDate, LocalDate currentDate) {
		Objects.requireNonNull(birthDate, "Birth date cannot be null");
		Objects.requireNonNull(currentDate, "Current date cannot be null");
		return Period.between(birthDate, currentDate).getYears();
	}

	/**
	 * Calculates the number of days between two dates.
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return the number of days between the dates
	 */
	public static long daysBetween(LocalDate startDate, LocalDate endDate) {
		Objects.requireNonNull(startDate, "Start date cannot be null");
		Objects.requireNonNull(endDate, "End date cannot be null");
		return ChronoUnit.DAYS.between(startDate, endDate);
	}

	/**
	 * Checks if a date is in the past.
	 * @param date the date to check
	 * @return true if the date is in the past, false otherwise
	 */
	public static boolean isInPast(LocalDate date) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.isBefore(LocalDate.now());
	}

	/**
	 * Checks if a date is in the future.
	 * @param date the date to check
	 * @return true if the date is in the future, false otherwise
	 */
	public static boolean isInFuture(LocalDate date) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.isAfter(LocalDate.now());
	}

	/**
	 * Formats a date using the standard format (yyyy-MM-dd).
	 * @param date the date to format
	 * @return the formatted date string
	 */
	public static String formatDate(LocalDate date) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	/**
	 * Formats a date using a custom pattern.
	 * @param date the date to format
	 * @param pattern the pattern to use for formatting
	 * @return the formatted date string
	 */
	public static String formatDate(LocalDate date, String pattern) {
		Objects.requireNonNull(date, "Date cannot be null");
		Objects.requireNonNull(pattern, "Pattern cannot be null");
		return date.format(DateTimeFormatter.ofPattern(pattern));
	}

	/**
	 * Parses a date string using the standard format (yyyy-MM-dd).
	 * @param dateString the date string to parse
	 * @return the parsed LocalDate
	 */
	public static LocalDate parseDate(String dateString) {
		Objects.requireNonNull(dateString, "Date string cannot be null");
		return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
	}

	/**
	 * Adds the specified number of days to a date.
	 * @param date the base date
	 * @param days the number of days to add (can be negative)
	 * @return the new date
	 */
	public static LocalDate addDays(LocalDate date, long days) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.plusDays(days);
	}

	/**
	 * Adds the specified number of months to a date.
	 * @param date the base date
	 * @param months the number of months to add (can be negative)
	 * @return the new date
	 */
	public static LocalDate addMonths(LocalDate date, long months) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.plusMonths(months);
	}

	/**
	 * Adds the specified number of years to a date.
	 * @param date the base date
	 * @param years the number of years to add (can be negative)
	 * @return the new date
	 */
	public static LocalDate addYears(LocalDate date, long years) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.plusYears(years);
	}

	/**
	 * Gets the first day of the month for the given date.
	 * @param date the date
	 * @return the first day of the month
	 */
	public static LocalDate getFirstDayOfMonth(LocalDate date) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.withDayOfMonth(1);
	}

	/**
	 * Gets the last day of the month for the given date.
	 * @param date the date
	 * @return the last day of the month
	 */
	public static LocalDate getLastDayOfMonth(LocalDate date) {
		Objects.requireNonNull(date, "Date cannot be null");
		return date.withDayOfMonth(date.lengthOfMonth());
	}

	/**
	 * Checks if a date falls within a date range (inclusive).
	 * @param date the date to check
	 * @param startDate the start of the range
	 * @param endDate the end of the range
	 * @return true if the date is within the range, false otherwise
	 */
	public static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
		Objects.requireNonNull(date, "Date cannot be null");
		Objects.requireNonNull(startDate, "Start date cannot be null");
		Objects.requireNonNull(endDate, "End date cannot be null");
		return !date.isBefore(startDate) && !date.isAfter(endDate);

	// 2つの日付をLocalDate型で受け取り、間にある平日の日数を返す

	public static long calculateWeekdaysBetween(LocalDate startDate, LocalDate endDate) {
		Objects.requireNonNull(startDate, "Start date cannot be null");
		Objects.requireNonNull(endDate, "End date cannot be null");

		long weekdays = 0;
		LocalDate current = startDate;
		while (!current.isAfter(endDate)) {
			if (current.getDayOfWeek().getValue() < 6) { // Monday (1) to Friday (5)
				weekdays++;
			}
			current = current.plusDays(1);
		}
		return weekdays;
	}

}
