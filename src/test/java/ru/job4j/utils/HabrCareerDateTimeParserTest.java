package ru.job4j.utils;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class HabrCareerDateTimeParserTest {
    @Test
    public void whenThen() {
        String date = "2024-09-30T19:43:42+03:00";
        DateTimeParser parser = new HabrCareerDateTimeParser();
        String expected = "2024-09-30T19:43:42";
        LocalDateTime result = parser.parse(date);
        assertThat(expected).isEqualTo(result.toString());
    }
}