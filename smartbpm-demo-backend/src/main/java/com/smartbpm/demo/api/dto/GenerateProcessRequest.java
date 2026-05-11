package com.smartbpm.demo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateProcessRequest(
        String exampleId,
        @NotBlank(message = "Narrative is required")
        String narrative) {
}
