package com.processhub.controller;

import com.processhub.dto.CategoryResponse;
import com.processhub.dto.CreateCategoryRequest;
import com.processhub.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryResponse response = categoryService.createCategory(orgId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @PathVariable UUID orgId,
            @RequestParam(required = false) UUID departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CategoryResponse> responses = categoryService.getCategories(orgId, departmentId, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable UUID orgId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryResponse response = categoryService.getCategory(orgId, categoryId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
