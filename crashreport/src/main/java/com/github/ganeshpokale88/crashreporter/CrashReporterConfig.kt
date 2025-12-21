package com.github.ganeshpokale88.crashreporter

/**
 * Configuration class for CrashReporter library.
 * Allows users to configure base URL and headers for network requests.
 * 
 * Configuration can be set during initialization or updated later at runtime.
 */
data class CrashReporterConfig(
    val baseUrl: String,
    val apiEndpoint: String?,
    val headers: Map<String, String> = emptyMap(),
    val sanitizationConfig: StackTraceSanitizer.SanitizationConfig? = null,
    val dataRetentionDays: Long = 90L,
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
            redactCreditCards: Boolean = true,
            redactIPAddresses: Boolean = true,
            redactMACAddresses: Boolean = true,
            redactUUIDs: Boolean = false,
            redactDriversLicense: Boolean = true,
            redactPassport: Boolean = true,
            redactInsurancePolicy: Boolean = true,
            redactAccountNumbers: Boolean = true,
            redactAPIKeys: Boolean = true,
            redactJWTTokens: Boolean = true,
            redactAuthTokens: Boolean = true,
            redactDBConnections: Boolean = true,
            redactSensitiveURLs: Boolean = true,
            redactZIPCodes: Boolean = false,
            redactHealthPlanBeneficiary: Boolean = true,
            redactDeviceIDs: Boolean = true,
            redactBankAccounts: Boolean = true,
            redactRoutingNumbers: Boolean = true,
            redactISODates: Boolean = false,
            redactPasswordFields: Boolean = true,
            // New flags
            redactCityNames: Boolean = true,
            redactStateNames: Boolean = true,
            redactCountryNames: Boolean = false,
            redactStreetAddresses: Boolean = true,
            redactCoordinates: Boolean = true,
            redactAges: Boolean = true,
            redactVehicleVIN: Boolean = true,
            redactLicensePlateNumbers: Boolean = true,
            redactBiometricTerms: Boolean = true,
            redactNamedFiles: Boolean = true,
            redactFreeTextPHI: Boolean = true,
            redactOAuthTokens: Boolean = true,
            redactRefreshTokens: Boolean = true,
            redactSessionCookies: Boolean = true,
            redactPrivateKeys: Boolean = true,
            redactCertificates: Boolean = true,
            redactAWSKeys: Boolean = true,
            redactGCPKeys: Boolean = true,
            redactAzureSecrets: Boolean = true
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
                redactCreditCards = redactCreditCards,
                redactIPAddresses = redactIPAddresses,
                redactMACAddresses = redactMACAddresses,
                redactUUIDs = redactUUIDs,
                redactDriversLicense = redactDriversLicense,
                redactPassport = redactPassport,
                redactInsurancePolicy = redactInsurancePolicy,
                redactAccountNumbers = redactAccountNumbers,
                redactAPIKeys = redactAPIKeys,
                redactJWTTokens = redactJWTTokens,
                redactAuthTokens = redactAuthTokens,
                redactDBConnections = redactDBConnections,
                redactSensitiveURLs = redactSensitiveURLs,
                redactZIPCodes = redactZIPCodes,
                redactHealthPlanBeneficiary = redactHealthPlanBeneficiary,
                redactDeviceIDs = redactDeviceIDs,
                redactBankAccounts = redactBankAccounts,
                redactRoutingNumbers = redactRoutingNumbers,
                redactISODates = redactISODates,
                redactPasswordFields = redactPasswordFields,
                redactCityNames = redactCityNames,
                redactStateNames = redactStateNames,
                redactCountryNames = redactCountryNames,
                redactStreetAddresses = redactStreetAddresses,
                redactCoordinates = redactCoordinates,
                redactAges = redactAges,
                redactVehicleVIN = redactVehicleVIN,
                redactLicensePlateNumbers = redactLicensePlateNumbers,
                redactBiometricTerms = redactBiometricTerms,
                redactNamedFiles = redactNamedFiles,
                redactFreeTextPHI = redactFreeTextPHI,
                redactOAuthTokens = redactOAuthTokens,
                redactRefreshTokens = redactRefreshTokens,
                redactSessionCookies = redactSessionCookies,
                redactPrivateKeys = redactPrivateKeys,
                redactCertificates = redactCertificates,
                redactAWSKeys = redactAWSKeys,
                redactGCPKeys = redactGCPKeys,
                redactAzureSecrets = redactAzureSecrets
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
         * @return Builder instance for method chaining
         */
        fun apiEndpoint(endpoint: String): Builder {
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
            val url = baseUrl ?: throw IllegalStateException("Base URL must be set before adding certificate pins.")
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
            val url = baseUrl ?: throw IllegalStateException("Base URL must be set before adding certificate pins.")
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
        
        fun build(): CrashReporterConfig {
            val url = baseUrl ?: throw IllegalStateException("Base URL is required.")
            val endpoint = apiEndpoint ?: throw IllegalStateException("API endpoint is required.")
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
        
        private fun extractHostname(url: String): String {
            return try {
                java.net.URL(url).host
            } catch (e: Exception) {
                url.removePrefix("https://").removePrefix("http://").split("/").first().split(":").first()
            }
        }
    }
}
