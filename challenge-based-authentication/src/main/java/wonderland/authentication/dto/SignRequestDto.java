package wonderland.authentication.dto;


import javax.validation.constraints.NotBlank;

public record SignRequestDto(@NotBlank String jwt) { }
