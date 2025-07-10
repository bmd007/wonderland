package wonderland.authentication.dto;


import jakarta.validation.constraints.NotBlank;

public record SignRequestDto(@NotBlank String jwt) {
}
