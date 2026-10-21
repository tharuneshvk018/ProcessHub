package com.processhub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProcessHubApplicationTests {

    @Test
    @DisplayName("Should successfully bootstrap Spring Boot context with Flyway migrations")
    void contextLoads() {
        // Verifies Spring context, Flyway migration execution, and JPA wiring initialize properly
    }
}
