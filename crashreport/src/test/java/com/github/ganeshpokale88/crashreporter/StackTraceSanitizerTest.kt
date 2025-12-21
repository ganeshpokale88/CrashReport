package com.github.ganeshpokale88.crashreporter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for StackTraceSanitizer to verify PHI redaction functionality.
 */
class StackTraceSanitizerTest {
    
    @Test
    fun `test SSN redaction`() {
        val stackTrace = "Error at line 42: User SSN is 123-45-6789"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactSSNs = true)
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("123-45-6789"))
    }
    
    @Test
    fun `test email redaction`() {
        val stackTrace = "Error: Contact user@example.com for support"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactEmails = true)
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("user@example.com"))
    }
    
    @Test
    fun `test phone number redaction`() {
        val stackTrace = "Error: Call 555-123-4567 for help"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactPhones = true)
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("555-123-4567"))
    }
    
    @Test
    fun `test medical record number redaction`() {
        val stackTrace = "Error: MRN: ABC123456"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactMRNs = true)
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("ABC123456"))
    }
    
    @Test
    fun `test patient name redaction`() {
        val stackTrace = "Error processing data for John Doe"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(patientNames = listOf("John Doe"))
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("John Doe"))
    }
    
    @Test
    fun `test multiple PHI redaction`() {
        val stackTrace = """
            Error: Patient John Doe (SSN: 123-45-6789)
            Contact: user@example.com or 555-123-4567
            MRN: ABC123456
        """.trimIndent()
        
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.createHipaaCompliantConfig(patientNames = listOf("John Doe"))
        )
        
        assertFalse(sanitized.contains("123-45-6789"))
        assertFalse(sanitized.contains("user@example.com"))
        assertFalse(sanitized.contains("555-123-4567"))
        assertFalse(sanitized.contains("ABC123456"))
        assertFalse(sanitized.contains("John Doe"))
        assertTrue(sanitized.contains("[REDACTED]"))
    }
    
    @Test
    fun `test sanitization disabled`() {
        val stackTrace = "Error: SSN is 123-45-6789"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactSSNs = false)
        )
        
        assertEquals(stackTrace, sanitized)
    }
    
    @Test
    fun `test custom pattern redaction`() {
        val stackTrace = "Error: Patient ID is PAT-12345"
        val customPattern = Regex("""PAT-\d+""")
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(customPatterns = listOf(customPattern))
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("PAT-12345"))
    }
    
    @Test
    fun `test user path redaction`() {
        val stackTrace = "Error reading file: /data/user/0/com.app/files/patient_data.txt"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactUserPaths = true)
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("/data/user/0/com.app/files/patient_data.txt"))
    }
    
    @Test
    fun `test credit card redaction`() {
        val stackTrace = "Error: Card number 1234-5678-9012-3456"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.SanitizationConfig(redactCreditCards = true)
        )
        
        assertTrue(sanitized.contains("[REDACTED]"))
        assertFalse(sanitized.contains("1234-5678-9012-3456"))
    }
    
    @Test
    fun `test empty stack trace`() {
        val stackTrace = ""
        val sanitized = StackTraceSanitizer.sanitize(stackTrace)
        assertEquals("", sanitized)
    }
    
    @Test
    fun `test stack trace without PHI`() {
        val stackTrace = "java.lang.NullPointerException\n\tat com.example.MainActivity.onCreate(MainActivity.kt:42)"
        val sanitized = StackTraceSanitizer.sanitize(
            stackTrace,
            StackTraceSanitizer.createHipaaCompliantConfig()
        )
        
        // Should remain unchanged if no PHI detected
        assertEquals(stackTrace, sanitized)
    }
}

