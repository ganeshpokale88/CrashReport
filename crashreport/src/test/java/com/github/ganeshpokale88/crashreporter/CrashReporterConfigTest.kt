package com.github.ganeshpokale88.crashreporter

import org.junit.Assert.*
import org.junit.Test

class CrashReporterConfigTest {

    @Test
    fun `test builder sets base url correctly`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .build()
        
        assertEquals("https://api.example.com", config.baseUrl)
    }

    @Test
    fun `test builder sets api endpoint correctly`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/v1/crashes")
            .build()
            
        assertEquals("/v1/crashes", config.apiEndpoint)
    }
    
    @Test
    fun `test builder normalizes api endpoint`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("crashes") // Should add leading slash
            .build()
            
        assertEquals("/crashes", config.apiEndpoint)
    }

    @Test
    fun `test builder adds single header`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .addHeader("Authorization", "Bearer token")
            .build()
            
        assertEquals("Bearer token", config.headers["Authorization"])
    }

    @Test
    fun `test builder adds multiple headers`() {
        val headers = mapOf(
            "Header1" to "Value1",
            "Header2" to "Value2"
        )
        
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .headers(headers)
            .build()
            
        assertEquals("Value1", config.headers["Header1"])
        assertEquals("Value2", config.headers["Header2"])
    }

    @Test(expected = IllegalStateException::class)
    fun `test build throws exception when base url missing`() {
        CrashReporterConfig.Builder()
            .apiEndpoint("/crash")
            .build()
    }

    @Test(expected = IllegalStateException::class)
    fun `test build throws exception when api endpoint missing`() {
        CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .build()
    }

    @Test
    fun `test default values`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .build()
            
        assertEquals(90L, config.dataRetentionDays)
        assertTrue(config.headers.isEmpty())
        assertNull(config.sanitizationConfig)
        assertNull(config.certificatePins)
    }

    @Test
    fun `test enable sanitization with default config`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .enableSanitization()
            .build()
            
        assertNotNull(config.sanitizationConfig)
        // Default HIPAA config validation
        assertTrue(config.sanitizationConfig!!.redactSSNs)
        assertTrue(config.sanitizationConfig!!.redactEmails)
    }

    @Test
    fun `test custom data retention`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .dataRetentionDays(30)
            .build()
            
        assertEquals(30L, config.dataRetentionDays)
    }

    @Test
    fun `test certificate pinning single host`() {
        val pin = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .addCertificatePin("api.example.com", pin)
            .build()
            
        assertNotNull(config.certificatePins)
        assertEquals(1, config.certificatePins!!.size)
        assertEquals(pin, config.certificatePins!!["api.example.com"]!![0])
    }

    @Test
    fun `test certificate pinning automatic hostname extraction`() {
        val pin = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .addCertificatePin(pin)
            .build()
            
        assertNotNull(config.certificatePins)
        assertEquals(pin, config.certificatePins!!["api.example.com"]!![0])
    }
    
    @Test
    fun `test custom sanitization config builder`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crash")
            .sanitizationConfig(
                redactEmails = false,
                redactSSNs = true
            )
            .build()
            
        assertNotNull(config.sanitizationConfig)
        assertFalse(config.sanitizationConfig!!.redactEmails)
        assertTrue(config.sanitizationConfig!!.redactSSNs)
    }
}
