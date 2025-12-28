package com.github.ganeshpokale88.crashreporter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for StackTraceSanitizer to verify PHI redaction functionality.
 * These tests cover the entire flow of the sanitization process including:
 * - Personal identifiers (SSN, email, phone)
 * - Financial information (credit cards, bank accounts)
 * - Authentication tokens (JWT, OAuth, API keys)
 * - Network identifiers (IP addresses, MAC addresses)
 * - Location data (coordinates, addresses)
 * - Healthcare identifiers (MRN, insurance)
 * - And more...
 */
class StackTraceSanitizerTest {
    
    // ================= Personal Identifiers =================
    
    @Test
    fun `test SSN redaction with dashes`() {
        val stackTrace = "Error at line 42: User SSN is 123-45-6789"
        val sanitized = StackTraceSanitizer.sanitize(stackTrace, StackTraceSanitizer.SanitizationConfig(redactSSNs = true))
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("123-45-6789"))
    }
    
    @Test
    fun `test SSN redaction without dashes`() {
        val stackTrace = "SSN number: 123456789"
        val sanitized = StackTraceSanitizer.sanitize(stackTrace, StackTraceSanitizer.SanitizationConfig(redactSSNs = true))
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("123456789"))
    }
    
    @Test
    fun `test email redaction`() {
        val stackTrace = "User email: john.doe@example.com caused error"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactEmails = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("john.doe@example.com"))
    }
    
    @Test
    fun `test phone number redaction`() {
        val stackTrace = "Contact phone: (555) 123-4567"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactPhones = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("555"))
        assertFalse(sanitized.contains("123-4567"))
    }
    
    @Test
    fun `test phone number with country code redaction`() {
        val stackTrace = "Phone: +1-555-123-4567"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactPhones = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("+1-555-123-4567"))
    }
    
    // ================= Financial Information =================
    
    @Test
    fun `test credit card redaction`() {
        val stackTrace = "Payment failed for card: 4111-1111-1111-1111"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactCreditCards = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("4111-1111-1111-1111"))
    }
    
    @Test
    fun `test credit card without dashes redaction`() {
        val stackTrace = "Card number: 4111111111111111"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactCreditCards = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("4111111111111111"))
    }
    
    @Test
    fun `test bank account redaction`() {
        val stackTrace = "Bank Account Number: 123456789012"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactBankAccounts = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("123456789012"))
    }
    
    @Test
    fun `test routing number redaction`() {
        val stackTrace = "Routing: 021000021"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactRoutingNumbers = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("021000021"))
    }
    
    // ================= Authentication & Security =================
    
    @Test
    fun `test JWT token redaction`() {
        val stackTrace = "Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.Gfx6VO9tcxwk6xqx9yYzSfebfeakZp5JYIgP_edcw_A"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactJWTTokens = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
    }
    
    @Test
    fun `test API key redaction`() {
        // Using generic test pattern to avoid GitHub secret scanning false positives
        val stackTrace = "api_key=test_key_abcdefghijklmnopqrstuvwxyz123456"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactAPIKeys = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("test_key_abcdefghijklmnopqrstuvwxyz123456"))
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
    fun `test GCP key redaction`() {
        val stackTrace = "GCP Key: AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactGCPKeys = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI"))
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
    fun `test password field redaction`() {
        val stackTrace = "password=secretpass123"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactPasswordFields = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("secretpass123"))
    }
    
    // ================= Network Identifiers =================
    
    @Test
    fun `test IPv4 address redaction`() {
        val stackTrace = "Connection from IP: 192.168.1.100"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactIPAddresses = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("192.168.1.100"))
    }
    
    @Test
    fun `test MAC address redaction`() {
        val stackTrace = "Device MAC: 00:1A:2B:3C:4D:5E"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactMACAddresses = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("00:1A:2B:3C:4D:5E"))
    }
    
    @Test
    fun `test database connection string redaction`() {
        val stackTrace = "Connection: jdbc:mysql://localhost:3306/mydb?user=admin&pass=secret"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactDBConnections = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("jdbc:mysql://localhost:3306/mydb"))
    }
    
    // ================= Location Data =================
    
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
    fun `test city state contextual redaction`() {
        val stackTrace = "City: San Francisco\nState: CA"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactCityNames = true, redactStateNames = true)
        )
        assertTrue(sanitized.contains("City: [REDACTED]"))
        assertFalse(sanitized.contains("San Francisco"))
        assertTrue(sanitized.contains("State: [REDACTED]"))
    }
    
    @Test
    fun `test street address redaction`() {
        val stackTrace = "Address: 123 Main Street"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactStreetAddresses = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
    }
    
    // ================= Healthcare Identifiers =================
    
    @Test
    fun `test medical record number redaction`() {
        val stackTrace = "MRN: MRN12345678"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactMRNs = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("MRN12345678"))
    }
    
    @Test
    fun `test insurance policy redaction`() {
        val stackTrace = "Policy#: ABC-123456789"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactInsurancePolicy = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("ABC-123456789"))
    }
    
    @Test
    fun `test age redaction`() {
        val stackTrace = "Patient Age: 45 years old"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactAges = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
    }
    
    // ================= Vehicle Data =================
    
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
    fun `test license plate contextual redaction`() {
        val stackTrace = "License Plate: ABC1234"
        // Create a minimal config with only license plate redaction enabled
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(
                redactEmails = false,
                redactPhones = false,
                redactSSNs = false,
                redactMRNs = false,
                redactUserPaths = false,
                redactCreditCards = false,
                redactIPAddresses = false,
                redactMACAddresses = false,
                redactDriversLicense = false,
                redactPassport = false,
                redactInsurancePolicy = false,
                redactAccountNumbers = false,
                redactAPIKeys = false,
                redactJWTTokens = false,
                redactAuthTokens = false,
                redactDBConnections = false,
                redactSensitiveURLs = false,
                redactHealthPlanBeneficiary = false,
                redactDeviceIDs = false,
                redactBankAccounts = false,
                redactRoutingNumbers = false,
                redactPasswordFields = false,
                redactCityNames = false,
                redactStateNames = false,
                redactStreetAddresses = false,
                redactCoordinates = false,
                redactAges = false,
                redactVehicleVIN = false,
                redactLicensePlateNumbers = true, // Only this is enabled
                redactBiometricTerms = false,
                redactNamedFiles = false,
                redactFreeTextPHI = false,
                redactOAuthTokens = false,
                redactRefreshTokens = false,
                redactSessionCookies = false,
                redactPrivateKeys = false,
                redactCertificates = false,
                redactAWSKeys = false,
                redactGCPKeys = false,
                redactAzureSecrets = false
            )
        )
        assertTrue("Expected [REDACTED] in output: $sanitized", sanitized.contains("[REDACTED]"))
        assertFalse("Expected ABC1234 to be removed: $sanitized", sanitized.contains("ABC1234"))
    }
    
    // ================= Document Identifiers =================
    
    @Test
    fun `test drivers license redaction`() {
        val stackTrace = "Driver's License: D1234567890"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactDriversLicense = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("D1234567890"))
    }
    
    @Test
    fun `test passport redaction`() {
        val stackTrace = "Passport: AB1234567"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactPassport = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("AB1234567"))
    }
    
    // ================= Other Data =================
    
    @Test
    fun `test user path redaction`() {
        val stackTrace = "Path: /Users/patient/documents/medical_records.pdf"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactUserPaths = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("/Users/patient/documents"))
    }
    
    @Test
    fun `test UUID redaction when enabled`() {
        val stackTrace = "UUID: 550e8400-e29b-41d4-a716-446655440000"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactUUIDs = true)
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("550e8400-e29b-41d4-a716-446655440000"))
    }
    
    @Test
    fun `test UUID not redacted when disabled`() {
        val stackTrace = "UUID: 550e8400-e29b-41d4-a716-446655440000"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactUUIDs = false)
        )
        assertTrue(sanitized.contains("550e8400-e29b-41d4-a716-446655440000"))
    }
    
    // ================= Custom Patterns =================
    
    @Test
    fun `test custom pattern redaction`() {
        val customPattern = Regex("""\bCUSTOM-\d{6}\b""")
        val stackTrace = "Error with identifier CUSTOM-123456"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(customPatterns = listOf(customPattern))
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("CUSTOM-123456"))
    }
    
    @Test
    fun `test patient name redaction`() {
        val stackTrace = "Error for patient John Smith"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig()
        )
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("John Smith"))
    }
    
    // ================= HIPAA Compliant Config =================
    
    @Test
    fun `test HIPAA compliant config creation`() {
        val config = StackTraceSanitizer.createHipaaCompliantConfig()
        assertTrue(config.redactSSNs)
        assertTrue(config.redactEmails)
        assertTrue(config.redactPhones)
        assertTrue(config.redactCreditCards)
        assertFalse(config.redactDates) // Optional by default
        assertFalse(config.redactUUIDs) // Optional by default
    }

    
    // ================= Flow Tests =================
    
    @Test
    fun `test complete stack trace sanitization flow`() {
        val stackTrace = """
            java.lang.RuntimeException: Error processing user john@example.com
                at com.app.UserService.process(UserService.java:42)
                SSN: 123-45-6789
                Phone: 555-123-4567
                Credit Card: 4111-1111-1111-1111
        """.trimIndent()
        
        val config = StackTraceSanitizer.createHipaaCompliantConfig()
        val sanitized = StackTraceSanitizer.sanitize(stackTrace, config)
        
        // Verify all sensitive data is redacted
        assertFalse(sanitized.contains("john@example.com"))
        assertFalse(sanitized.contains("123-45-6789"))
        assertFalse(sanitized.contains("555-123-4567"))
        assertFalse(sanitized.contains("4111-1111-1111-1111"))
        
        // Verify non-sensitive stack trace parts are preserved
        assertTrue(sanitized.contains("RuntimeException"))
        assertTrue(sanitized.contains("UserService.java:42"))
    }
    
    @Test
    fun `test empty stack trace sanitization`() {
        val sanitized = StackTraceSanitizer.sanitize("", StackTraceSanitizer.SanitizationConfig())
        assertEquals("", sanitized)
    }
    
    @Test
    fun `test stack trace with no sensitive data`() {
        val stackTrace = "java.lang.NullPointerException at MyClass.method(MyClass.java:10)"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.createHipaaCompliantConfig()
        )
        assertEquals(stackTrace, sanitized)
    }
}
