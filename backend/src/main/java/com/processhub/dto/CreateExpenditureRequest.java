package com.processhub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreateExpenditureRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Justification is required")
    @Size(max = 1000, message = "Justification cannot exceed 1000 characters")
    private String justification;

    public CreateExpenditureRequest() {
    }

    public CreateExpenditureRequest(BigDecimal amount, String justification) {
        this.amount = amount;
        this.justification = justification;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
