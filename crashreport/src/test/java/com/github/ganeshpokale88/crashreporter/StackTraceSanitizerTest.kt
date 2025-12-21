package com.github.ganeshpokale88.crashreporter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for StackTraceSanitizer to verify PHI redaction functionality.
 */
class StackTraceSanitizerTest {
    
    // ... Keeping existing tests ...
    
    @Test
    fun `test SSN redaction`() {
        val stackTrace = "Error at line 42: User SSN is 123-45-6789"
        val sanitized = StackTraceSanitizer.sanitize(stackTrace, StackTraceSanitizer.SanitizationConfig(redactSSNs = true))
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("123-45-6789"))
    }
    
    // ... New Tests for New Features ...
    
    @Test
    fun `test coordinates redaction`() {
        val stackTrace = "Location: Lat 37.7749, Lng -122.4194"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactCoordinates = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("37.7749"))
    }
    
    @Test
    fun `test age redaction`() {
        val stackTrace = "Patient Age: 45 years old"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactAges = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("45"))
    }
    
    @Test
    fun `test VIN redaction`() {
        val stackTrace = "Vehicle VIN: 1HGCM82633A004352"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactVehicleVIN = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("1HGCM82633A004352"))
    }
    
    @Test
    fun `test private key redaction`() {
        val stackTrace = """
            -----BEGIN RSA PRIVATE KEY-----
            MIIEpQIBAAKCAQEA3Tz2mr7SZiAMfQyuvBjM9Oi..
            -----END RSA PRIVATE KEY-----
        """.trimIndent()
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactPrivateKeys = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("MIIEpQIBAAKCAQEA3Tz2mr7SZiAMfQyuvBjM9Oi"))
    }
    
    @Test
    fun `test AWS key redaction`() {
        val stackTrace = "AWS Access: AKIAIOSFODNN7EXAMPLE"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactAWSKeys = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("AKIAIOSFODNN7EXAMPLE"))
    }
    
    @Test
    fun `test city state contextual redaction`() {
        val stackTrace = "City: San Francisco\nState: CA"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactCityNames = true, redactStateNames = true)
        )
        // Check that "City: " label remains but value is redacted
        assertTrue(sanitized.contains("City: [REDACTED]"))
        assertFalse(sanitized.contains("San Francisco"))
        assertTrue(sanitized.contains("State: [REDACTED]"))
        assertFalse(sanitized.contains("CA"))
    }

    @Test
    fun `test oauth token redaction`() {
        val stackTrace = "Authorization: Bearer ya29.a0Af1234567890abcdefghijklmnopqrstuvwxyz"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactAuthTokens = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("ya29.a0Af1234567890abcdefghijklmnopqrstuvwxyz"))
    }
}
