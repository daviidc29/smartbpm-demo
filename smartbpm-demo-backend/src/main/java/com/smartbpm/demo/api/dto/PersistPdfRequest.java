package com.smartbpm.demo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PersistPdfRequest(
        @NotBlank String fileName,
        @NotBlank String base64Pdf) {
}
