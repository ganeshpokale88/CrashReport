package com.github.ganeshpokale88.crashreporter

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import com.github.ganeshpokale88.crashreporter.api.CrashReportApi
import com.github.ganeshpokale88.crashreporter.database.CrashLogDao

/**
 * Unit tests for DependencyRegistry to verify singleton dependency management.
 * Tests cover initialization, update, and state management flows.
 * 
 * Note: We use initializeForTesting() with a mocked CrashLogDao instead of
 * mocking CrashLogDatabase, since Room database classes require Android runtime.
 */
class DependencyRegistryTest {
    
    private lateinit var mockDao: CrashLogDao
    private lateinit var mockApi: CrashReportApi
    
    @Before
    fun setup() {
        mockDao = mock(CrashLogDao::class.java)
        mockApi = mock(CrashReportApi::class.java)
        
        // Ensure clean state before each test
        DependencyRegistry.reset()
    }
    
    @After
    fun tearDown() {
        DependencyRegistry.reset()
    }
    
    // ================= Initialization Tests =================
    
    @Test
    fun `test initialize with dao only`() {
        DependencyRegistry.initializeForTesting(mockDao)
        
        val dao = DependencyRegistry.getCrashLogDao()
        assertNotNull(dao)
    }
    
    @Test
    fun `test initialize with dao and api`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        DependencyRegistry.initializeForTesting(mockDao, mockApi, config)
        
        val dao = DependencyRegistry.getCrashLogDao()
        assertNotNull(dao)
        
        val api = DependencyRegistry.getCrashReportApi()
        assertNotNull(api)
    }
    
    @Test
    fun `test getCrashLogDao throws when not initialized`() {
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.getCrashLogDao()
        }
    }
    
    @Test
    fun `test getCrashReportApi throws when not initialized`() {
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.getCrashReportApi()
        }
    }
    
    @Test
    fun `test getCrashReportApi throws when initialized without api`() {
        DependencyRegistry.initializeForTesting(mockDao)
        
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.getCrashReportApi()
        }
    }
    
    // ================= Update Tests =================
    
    @Test
    fun `test updateApi updates api and config`() {
        DependencyRegistry.initializeForTesting(mockDao)
        
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        DependencyRegistry.updateApi(mockApi, config)
        
        val api = DependencyRegistry.getCrashReportApi()
        assertNotNull(api)
        
        val savedConfig = DependencyRegistry.getConfig()
        assertNotNull(savedConfig)
    }
    
    @Test
    fun `test updateApi throws when not initialized`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.updateApi(mockApi, config)
        }
    }
    
    @Test
    fun `test updateConfig only updates config`() {
        DependencyRegistry.initializeForTesting(mockDao)
        
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        DependencyRegistry.updateConfig(config)
        
        val savedConfig = DependencyRegistry.getConfig()
        assertNotNull(savedConfig)
        
        // API should still throw since we only updated config
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.getCrashReportApi()
        }
    }
    
    @Test
    fun `test updateConfig throws when not initialized`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.updateConfig(config)
        }
    }
    
    // ================= isApiConfigured Tests =================
    
    @Test
    fun `test isApiConfigured returns false when not initialized`() {
        assertFalse(DependencyRegistry.isApiConfigured())
    }
    
    @Test
    fun `test isApiConfigured returns false when initialized without api`() {
        DependencyRegistry.initializeForTesting(mockDao)
        
        assertFalse(DependencyRegistry.isApiConfigured())
    }
    
    @Test
    fun `test isApiConfigured returns true when fully configured`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        DependencyRegistry.initializeForTesting(mockDao, mockApi, config)
        
        assertTrue(DependencyRegistry.isApiConfigured())
    }
    
    @Test
    fun `test isApiConfigured returns false with empty baseUrl`() {
        val config = CrashReporterConfig(
            baseUrl = "",
            apiEndpoint = "/crashes"
        )
        
        DependencyRegistry.initializeForTesting(mockDao, mockApi, config)
        
        assertFalse(DependencyRegistry.isApiConfigured())
    }
    
    @Test
    fun `test isApiConfigured returns false with empty apiEndpoint`() {
        val config = CrashReporterConfig(
            baseUrl = "https://api.example.com",
            apiEndpoint = ""
        )
        
        DependencyRegistry.initializeForTesting(mockDao, mockApi, config)
        
        assertFalse(DependencyRegistry.isApiConfigured())
    }
    
    // ================= getConfig Tests =================
    
    @Test
    fun `test getConfig returns null when not set`() {
        DependencyRegistry.initializeForTesting(mockDao)
        
        val config = DependencyRegistry.getConfig()
        assertNull(config)
    }
    
    @Test
    fun `test getConfig returns config when set during initialization`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        DependencyRegistry.initializeForTesting(mockDao, mockApi, config)
        
        val retrievedConfig = DependencyRegistry.getConfig()
        assertNotNull(retrievedConfig)
    }
    
    // ================= Reset Tests =================
    
    @Test
    fun `test reset clears all dependencies`() {
        val config = CrashReporterConfig.Builder()
            .baseUrl("https://api.example.com")
            .apiEndpoint("/crashes")
            .build()
        
        DependencyRegistry.initializeForTesting(mockDao, mockApi, config)
        
        // Verify initialized
        assertNotNull(DependencyRegistry.getCrashLogDao())
        
        // Reset
        DependencyRegistry.reset()
        
        // Verify cleared
        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.getCrashLogDao()
        }
    }
}
