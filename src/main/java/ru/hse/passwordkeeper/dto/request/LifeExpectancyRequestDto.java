package ru.hse.passwordkeeper.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LifeExpectancyRequestDto {
    @Positive(message = "duration should be positive")
    @JsonProperty("life_expectancy")
    private Long lifeExpectancy;
}
