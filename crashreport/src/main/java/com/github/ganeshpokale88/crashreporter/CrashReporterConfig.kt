package com.github.ganeshpokale88.crashreporter

/**
 * Configuration class for CrashReporter library.
 * Allows users to configure base URL and headers for network requests.
 * 
 * Configuration can be set during initialization or updated later at runtime
 * (e.g., after user login when JWT token becomes available).
 * 
 * Example usage - During initialization:
 * ```kotlin
 * val config = CrashReporterConfig.Builder()
 *     .baseUrl("https://api.example.com")
 *     .addHeader("X-API-Key", "key456")
 *     .build()
 * 
 * CrashReporter.initialize(context, config)
 * ```
 * 
 * Example usage - Update after login:
 * ```kotlin
 * // After successful login
 * val newConfig = CrashReporterConfig.Builder()
 *     .baseUrl("https://api.example.com")
 *     .addHeader("Authorization", "Bearer $jwtToken")
 *     .addHeader("X-API-Key", "key456")
 *     .build()
 * 
 * CrashReporter.updateConfiguration(newConfig)
 * ```
 */
data class CrashReporterConfig(
    /**
     * Base URL for the crash reporting API endpoint.
     * Production: Must use HTTPS (e.g., "https://api.example.com").
     * Development: HTTP allowed only for localhost (e.g., "http://10.0.2.2:8000" for emulator).
     * HTTP in production will throw IllegalArgumentException.
     * 
     * Should not include the endpoint path (use apiEndpoint for that).
     * Examples:
     * - "https://api.example.com" ✅
     * - "https://api.example.com/v1" ✅ (if you want version in base URL)
     * - "https://api.example.com/crashes" ❌ (use apiEndpoint instead)
     */
    val baseUrl: String,
    
    /**
     * API endpoint path for uploading crash reports.
     * Required: Must be provided for API calls to work.
     * 
     * Should start with "/" (e.g., "/crashes", "/api/crashes", "/v1/reports").
     * Examples:
     * - "/crashes"
     * - "/api/crashes"
     * - "/v1/reports"
     * - "/crash-reports/upload"
     */
    val apiEndpoint: String?,
    
    /**
     * Map of headers to be added to all network requests
     * Key: Header name, Value: Header value
     */
    val headers: Map<String, String> = emptyMap(),
    
    /**
     * Configuration for stack trace sanitization to remove PHI (Protected Health Information).
     * If null, sanitization is disabled. For HIPAA compliance, provide a sanitization config.
     * Default: null (sanitization disabled)
     */
    val sanitizationConfig: StackTraceSanitizer.SanitizationConfig? = null,
    
    /**
     * Data retention period in days.
     * Crash logs older than this period will be automatically deleted from the database.
     * Default: 90 days (HIPAA compliance recommendation).
     * Set to 0 or negative to disable automatic deletion.
     */
    val dataRetentionDays: Long = 90L,
    
    /**
     * SSL Certificate pinning configuration.
     * Map of hostname to list of SHA-256 certificate pins.
     * Example: mapOf("api.example.com" to listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))
     * 
     * For certificate rotation, provide multiple pins per hostname.
     * Pinning is disabled if null or empty.
     * 
     * To get SHA-256 pin of your certificate:
     * ```bash
     * openssl s_client -servername api.example.com -connect api.example.com:443 < /dev/null | \
     *   openssl x509 -pubkey -noout | \
     *   openssl pkey -pubin -outform der | \
     *   openssl dgst -sha256 -binary | \
     *   openssl enc -base64
     * ```
     * 
     * Or use online tools: https://www.ssllabs.com/ssltest/
     */
    val certificatePins: Map<String, List<String>>? = null
) {
    /**
     * Builder class for creating CrashReporterConfig
     */
    class Builder {
        private var baseUrl: String? = null
        private val headers: MutableMap<String, String> = mutableMapOf()
        private var sanitizationConfig: StackTraceSanitizer.SanitizationConfig? = null
        private var dataRetentionDays: Long = 90L
        private var apiEndpoint: String? = null
        private val certificatePins: MutableMap<String, MutableList<String>> = mutableMapOf()
        
        /**
         * Set the base URL for the API
         * @param baseUrl Base URL (e.g., "https://api.example.com")
         * @return Builder instance for method chaining
         */
        fun baseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }
        
        /**
         * Add a header to be included in all network requests
         * @param name Header name (e.g., "Authorization")
         * @param value Header value (e.g., "Bearer token123")
         * @return Builder instance for method chaining
         */
        fun addHeader(name: String, value: String): Builder {
            headers[name] = value
            return this
        }
        
        /**
         * Add multiple headers at once
         * @param headers Map of header name to value
         * @return Builder instance for method chaining
         */
        fun headers(headers: Map<String, String>): Builder {
            this.headers.putAll(headers)
            return this
        }
        
        /**
         * Enable stack trace sanitization for HIPAA compliance.
         * This will redact potential PHI (SSNs, emails, phone numbers, patient names, etc.) from stack traces.
         * 
         * @param config Sanitization configuration. If null, uses default HIPAA-compliant config.
         *               Use [StackTraceSanitizer.createHipaaCompliantConfig] to create a custom config.
         * @return Builder instance for method chaining
         */
        fun enableSanitization(config: StackTraceSanitizer.SanitizationConfig? = null): Builder {
            this.sanitizationConfig = config ?: StackTraceSanitizer.createHipaaCompliantConfig()
            return this
        }
        
        /**
         * Set custom sanitization configuration.
         * 
         * @param patientNames List of patient names to redact (case-insensitive)
         * @param customPatterns List of custom regex patterns to redact
         * @param redactEmails Whether to redact email addresses (default: true)
         * @param redactPhones Whether to redact phone numbers (default: true)
         * @param redactSSNs Whether to redact SSNs (default: true)
         * @param redactMRNs Whether to redact medical record numbers (default: true)
         * @param redactUserPaths Whether to redact file paths containing user data (default: true)
         * @param redactDates Whether to redact dates (default: false)
         * @param redactCreditCards Whether to redact credit card numbers (default: true)
         * @return Builder instance for method chaining
         */
        fun sanitizationConfig(
            patientNames: List<String> = emptyList(),
            customPatterns: List<Regex> = emptyList(),
            redactEmails: Boolean = true,
            redactPhones: Boolean = true,
            redactSSNs: Boolean = true,
            redactMRNs: Boolean = true,
            redactUserPaths: Boolean = true,
            redactDates: Boolean = false,
            redactCreditCards: Boolean = true
        ): Builder {
            this.sanitizationConfig = StackTraceSanitizer.SanitizationConfig(
                patientNames = patientNames,
                customPatterns = customPatterns,
                redactEmails = redactEmails,
                redactPhones = redactPhones,
                redactSSNs = redactSSNs,
                redactMRNs = redactMRNs,
                redactUserPaths = redactUserPaths,
                redactDates = redactDates,
                redactCreditCards = redactCreditCards
            )
            return this
        }
        
        /**
         * Set data retention period in days.
         * Crash logs older than this period will be automatically deleted from the database.
         * 
         * @param days Number of days to retain crash logs. Default: 90 days.
         *             Set to 0 or negative to disable automatic deletion.
         * @return Builder instance for method chaining
         */
        fun dataRetentionDays(days: Long): Builder {
            this.dataRetentionDays = days
            return this
        }
        
        /**
         * Set the API endpoint path for uploading crash reports.
         * Required for API calls to work.
         * 
         * @param endpoint Endpoint path (e.g., "/crashes", "/api/crashes", "/v1/reports").
         *                 Should start with "/". Required - no default value.
         * @return Builder instance for method chaining
         */
        fun apiEndpoint(endpoint: String): Builder {
            // Ensure endpoint starts with "/"
            this.apiEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
            return this
        }
        
        /**
         * Add SSL certificate pin for the base URL's hostname (automatic hostname extraction).
         * Certificate pinning protects against MITM attacks by verifying the server's certificate.
         * 
         * This is a convenience method that automatically extracts the hostname from the baseUrl.
         * 
         * @param sha256Pin SHA-256 pin of the certificate in format "sha256/XXXXXXXXXXXXXXXX" or just the hash
         * @return Builder instance for method chaining
         * @throws IllegalStateException if baseUrl is not set
         */
        fun addCertificatePin(sha256Pin: String): Builder {
            val url = baseUrl ?: throw IllegalStateException(
                "Base URL must be set before adding certificate pins. Call baseUrl() first."
            )
            val hostname = extractHostname(url)
            certificatePins.getOrPut(hostname) { mutableListOf() }.add(sha256Pin)
            return this
        }
        
        /**
         * Add SSL certificate pin for a specific hostname.
         * Certificate pinning protects against MITM attacks by verifying the server's certificate.
         * 
         * @param hostname Hostname to pin (e.g., "api.example.com" or "*.example.com")
         * @param sha256Pin SHA-256 pin of the certificate in format "sha256/XXXXXXXXXXXXXXXX" or just the hash
         * @return Builder instance for method chaining
         */
        fun addCertificatePin(hostname: String, sha256Pin: String): Builder {
            certificatePins.getOrPut(hostname) { mutableListOf() }.add(sha256Pin)
            return this
        }
        
        /**
         * Add multiple SSL certificate pins for the base URL's hostname (for certificate rotation).
         * Automatically extracts hostname from baseUrl.
         * 
         * @param sha256Pins List of SHA-256 pins in format "sha256/XXXXXXXXXXXXXXXX" or just the hash
         * @return Builder instance for method chaining
         * @throws IllegalStateException if baseUrl is not set
         */
        fun addCertificatePins(sha256Pins: List<String>): Builder {
            val url = baseUrl ?: throw IllegalStateException(
                "Base URL must be set before adding certificate pins. Call baseUrl() first."
            )
            val hostname = extractHostname(url)
            certificatePins.getOrPut(hostname) { mutableListOf() }.addAll(sha256Pins)
            return this
        }
        
        /**
         * Add multiple SSL certificate pins for a hostname (for certificate rotation).
         * 
         * @param hostname Hostname to pin (e.g., "api.example.com")
         * @param sha256Pins List of SHA-256 pins in format "sha256/XXXXXXXXXXXXXXXX"
         * @return Builder instance for method chaining
         */
        fun addCertificatePins(hostname: String, sha256Pins: List<String>): Builder {
            certificatePins.getOrPut(hostname) { mutableListOf() }.addAll(sha256Pins)
            return this
        }
        
        /**
         * Set certificate pins for multiple hostnames at once.
         * 
         * @param pins Map of hostname to list of SHA-256 pins
         * @return Builder instance for method chaining
         */
        fun certificatePins(pins: Map<String, List<String>>): Builder {
            pins.forEach { (hostname, pinList) ->
                certificatePins.getOrPut(hostname) { mutableListOf() }.addAll(pinList)
            }
            return this
        }
        
        /**
         * Build the configuration object
         * @throws IllegalStateException if baseUrl or apiEndpoint is not set
         */
        fun build(): CrashReporterConfig {
            val url = baseUrl ?: throw IllegalStateException(
                "Base URL is required. Call baseUrl() before build()"
            )
            val endpoint = apiEndpoint ?: throw IllegalStateException(
                "API endpoint is required. Call apiEndpoint() before build()"
            )
            val pins = if (certificatePins.isEmpty()) null else certificatePins.mapValues { it.value.toList() }
            return CrashReporterConfig(url, endpoint, headers.toMap(), sanitizationConfig, dataRetentionDays, pins)
        }
    }
    
    companion object {
        /**
         * Create a default configuration with a base URL
         * @param baseUrl Base URL for the API
         * @param sanitizationConfig Optional sanitization config for HIPAA compliance (default: null)
         * @param dataRetentionDays Data retention period in days (default: 90)
         * @return CrashReporterConfig instance
         */
        fun create(
            baseUrl: String,
            apiEndpoint: String,
            sanitizationConfig: StackTraceSanitizer.SanitizationConfig? = null,
            dataRetentionDays: Long = 90L,
            certificatePins: Map<String, List<String>>? = null
        ): CrashReporterConfig {
            val endpoint = if (apiEndpoint.startsWith("/")) apiEndpoint else "/$apiEndpoint"
            return CrashReporterConfig(baseUrl, endpoint, emptyMap(), sanitizationConfig, dataRetentionDays, certificatePins)
        }
        
        /**
         * Extract hostname from URL
         */
        private fun extractHostname(url: String): String {
            return try {
                val urlObj = java.net.URL(url)
                urlObj.host
            } catch (e: Exception) {
                // Fallback: try to extract manually
                val withoutProtocol = url.removePrefix("https://").removePrefix("http://")
                withoutProtocol.split("/").first().split(":").first()
            }
        }
    }
}

