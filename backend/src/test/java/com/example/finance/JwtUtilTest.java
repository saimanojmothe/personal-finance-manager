package com.example.finance;

import com.example.finance.security.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET =
            "personal-finance-manager-demo-secret-change-me-in-production-0123456789abcdef";

    @Test
    void tokenRoundTripsEmailAndValidates() {
        JwtUtil jwt = new JwtUtil(SECRET, 3_600_000);
        String token = jwt.generateToken("demo@finance.local");
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("demo@finance.local", jwt.extractEmail(token));
        assertTrue(jwt.isValid(token, "demo@finance.local"));
        assertFalse(jwt.isValid(token, "someone-else@example.com"));
        assertFalse(jwt.isValid("not-a-token", "demo@finance.local"));
    }
}
