package org.springframework.samples.petclinic.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for DateUtils utility methods.
 */
class DateUtilsTest {

	@Test
	@DisplayName("Should calculate age correctly from birth date to current date")
	void shouldCalculateAgeFromBirthDate() {
		// Given
		LocalDate birthDate = LocalDate.of(1990, 5, 15);
		LocalDate currentDate = LocalDate.of(2023, 5, 15);

		// When
		int age = DateUtils.calculateAge(birthDate, currentDate);

		// Then
		assertThat(age).isEqualTo(33);
	}

	@Test
	@DisplayName("Should calculate age correctly when birthday hasn't occurred this year")
	void shouldCalculateAgeWhenBirthdayNotYetOccurred() {
		// Given
		LocalDate birthDate = LocalDate.of(1990, 12, 25);
		LocalDate currentDate = LocalDate.of(2023, 5, 15);

		// When
		int age = DateUtils.calculateAge(birthDate, currentDate);

		// Then
		assertThat(age).isEqualTo(32);
	}

	@Test
	@DisplayName("Should throw exception when birth date is null")
	void shouldThrowExceptionWhenBirthDateIsNull() {
		assertThatThrownBy(() -> DateUtils.calculateAge(null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("Birth date cannot be null");
	}

	@Test
	@DisplayName("Should calculate days between two dates correctly")
	void shouldCalculateDaysBetweenDates() {
		// Given
		LocalDate startDate = LocalDate.of(2023, 1, 1);
		LocalDate endDate = LocalDate.of(2023, 1, 10);

		// When
		long days = DateUtils.daysBetween(startDate, endDate);

		// Then
		assertThat(days).isEqualTo(9);
	}

	@Test
	@DisplayName("Should return negative days when end date is before start date")
	void shouldReturnNegativeDaysWhenEndDateIsBeforeStartDate() {
		// Given
		LocalDate startDate = LocalDate.of(2023, 1, 10);
		LocalDate endDate = LocalDate.of(2023, 1, 1);

		// When
		long days = DateUtils.daysBetween(startDate, endDate);

		// Then
		assertThat(days).isEqualTo(-9);
	}

	@Test
	@DisplayName("Should identify date in past correctly")
	void shouldIdentifyDateInPast() {
		// Given
		LocalDate pastDate = LocalDate.now().minusDays(1);

		// When & Then
		assertThat(DateUtils.isInPast(pastDate)).isTrue();
	}

	@Test
	@DisplayName("Should identify date in future correctly")
	void shouldIdentifyDateInFuture() {
		// Given
		LocalDate futureDate = LocalDate.now().plusDays(1);

		// When & Then
		assertThat(DateUtils.isInFuture(futureDate)).isTrue();
	}

	@Test
	@DisplayName("Should format date using standard format")
	void shouldFormatDateUsingStandardFormat() {
		// Given
		LocalDate date = LocalDate.of(2023, 5, 15);

		// When
		String formatted = DateUtils.formatDate(date);

		// Then
		assertThat(formatted).isEqualTo("2023-05-15");
	}

	@Test
	@DisplayName("Should format date using custom pattern")
	void shouldFormatDateUsingCustomPattern() {
		// Given
		LocalDate date = LocalDate.of(2023, 5, 15);
		String pattern = "dd/MM/yyyy";

		// When
		String formatted = DateUtils.formatDate(date, pattern);

		// Then
		assertThat(formatted).isEqualTo("15/05/2023");
	}

	@Test
	@DisplayName("Should parse date string correctly")
	void shouldParseDateString() {
		// Given
		String dateString = "2023-05-15";

		// When
		LocalDate parsed = DateUtils.parseDate(dateString);

		// Then
		assertThat(parsed).isEqualTo(LocalDate.of(2023, 5, 15));
	}

	@ParameterizedTest
	@ValueSource(longs = {1, 7, 30, -5})
	@DisplayName("Should add days correctly")
	void shouldAddDaysCorrectly(long daysToAdd) {
		// Given
		LocalDate baseDate = LocalDate.of(2023, 5, 15);

		// When
		LocalDate result = DateUtils.addDays(baseDate, daysToAdd);

		// Then
		assertThat(result).isEqualTo(baseDate.plusDays(daysToAdd));
	}

	@Test
	@DisplayName("Should add months correctly")
	void shouldAddMonthsCorrectly() {
		// Given
		LocalDate baseDate = LocalDate.of(2023, 5, 15);

		// When
		LocalDate result = DateUtils.addMonths(baseDate, 3);

		// Then
		assertThat(result).isEqualTo(LocalDate.of(2023, 8, 15));
	}

	@Test
	@DisplayName("Should add years correctly")
	void shouldAddYearsCorrectly() {
		// Given
		LocalDate baseDate = LocalDate.of(2023, 5, 15);

		// When
		LocalDate result = DateUtils.addYears(baseDate, 2);

		// Then
		assertThat(result).isEqualTo(LocalDate.of(2025, 5, 15));
	}

	@Test
	@DisplayName("Should get first day of month correctly")
	void shouldGetFirstDayOfMonth() {
		// Given
		LocalDate date = LocalDate.of(2023, 5, 15);

		// When
		LocalDate firstDay = DateUtils.getFirstDayOfMonth(date);

		// Then
		assertThat(firstDay).isEqualTo(LocalDate.of(2023, 5, 1));
	}

	@Test
	@DisplayName("Should get last day of month correctly")
	void shouldGetLastDayOfMonth() {
		// Given
		LocalDate date = LocalDate.of(2023, 2, 15); // February

		// When
		LocalDate lastDay = DateUtils.getLastDayOfMonth(date);

		// Then
		assertThat(lastDay).isEqualTo(LocalDate.of(2023, 2, 28));
	}

	@Test
	@DisplayName("Should check if date is within range correctly")
	void shouldCheckIfDateIsWithinRange() {
		// Given
		LocalDate date = LocalDate.of(2023, 5, 15);
		LocalDate startDate = LocalDate.of(2023, 5, 1);
		LocalDate endDate = LocalDate.of(2023, 5, 31);

		// When & Then
		assertThat(DateUtils.isWithinRange(date, startDate, endDate)).isTrue();
	}

	@Test
	@DisplayName("Should return false when date is outside range")
	void shouldReturnFalseWhenDateIsOutsideRange() {
		// Given
		LocalDate date = LocalDate.of(2023, 6, 15);
		LocalDate startDate = LocalDate.of(2023, 5, 1);
		LocalDate endDate = LocalDate.of(2023, 5, 31);

		// When & Then
		assertThat(DateUtils.isWithinRange(date, startDate, endDate)).isFalse();
	}

	@Test
	@DisplayName("Should throw exception when null parameters are passed")
	void shouldThrowExceptionWhenNullParametersArePassed() {
		LocalDate validDate = LocalDate.now();

		assertThatThrownBy(() -> DateUtils.daysBetween(null, validDate))
			.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> DateUtils.formatDate(null))
			.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> DateUtils.parseDate(null))
			.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> DateUtils.addDays(null, 1))
			.isInstanceOf(NullPointerException.class);
	}

}