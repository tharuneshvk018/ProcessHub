package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RfiRequest {

    @NotBlank(message = "Requested information details are mandatory")
    @Size(min = 10, max = 1000, message = "Requested information must be between 10 and 1000 characters")
    private String question;

    public RfiRequest() {
    }

    public RfiRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
