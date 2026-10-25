package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProvideInfoRequest {

    @NotBlank(message = "Response content is mandatory")
    @Size(min = 5, max = 2000, message = "Response content must be between 5 and 2000 characters")
    private String response;

    public ProvideInfoRequest() {
    }

    public ProvideInfoRequest(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
