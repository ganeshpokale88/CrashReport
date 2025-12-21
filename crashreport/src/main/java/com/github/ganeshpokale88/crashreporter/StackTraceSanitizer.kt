package com.github.ganeshpokale88.crashreporter

/**
 * Utility class for sanitizing stack traces to remove or redact potential PHI (Protected Health Information)
 * and other sensitive data.
 * 
 * This class implements HIPAA-compliant data sanitization by detecting and redacting:
 * 
 * **Personal Identifiers:**
 * - Social Security Numbers (SSN)
 * - Medical Record Numbers (MRN)
 * - Driver's License Numbers
 * - Passport Numbers
 * - Patient names (configurable)
 * - Ages (contextual)
 * - Biometric terms
 * 
 * **Contact Information:**
 * - Email addresses
 * - Phone numbers
 * 
 * **Financial Information:**
 * - Credit card numbers
 * - Bank account numbers
 * - Routing numbers
 * - Account numbers
 * 
 * **Authentication & Security:**
 * - API keys
 * - JWT tokens
 * - Authorization tokens (Bearer, Basic)
 * - OAuth / Refresh tokens
 * - Session cookies
 * - Private Keys
 * - Certificates
 * - AWS / GCP / Azure secrets
 * - Password fields
 * 
 * **Network & Device Identifiers:**
 * - IP addresses (IPv4 and IPv6)
 * - MAC addresses
 * - Device identifiers (IMEI, serial numbers)
 * 
 * **Healthcare Identifiers:**
 * - Health plan beneficiary numbers
 * - Insurance policy numbers
 * 
 * **Location Data (Contextual/Best Effort):**
 * - Coordinates (Lat/Long)
 * - ZIP codes
 * - Cities, States, Countries (Contextual)
 * - Street Addresses (Contextual)
 * 
 * **Vehicle Data:**
 * - VIN
 * - License Plates
 * 
 * **Other Sensitive Data:**
 * - Database connection strings
 * - URLs with sensitive query parameters
 * - File paths containing user data
 * - UUIDs (optional)
 * - Dates (optional)
 * 
 * **Custom Patterns:**
 * - Custom regex patterns via configuration
 * 
 * All detected PHI and sensitive data is replaced with [REDACTED] to prevent exposure of sensitive information.
 */
object StackTraceSanitizer {
    
    private const val REDACTED_PLACEHOLDER = "[REDACTED]"
    
    // --- Existing Patterns ---
    private val ssnPattern = Regex("""\b\d{3}-?\d{2}-?\d{4}\b""", RegexOption.IGNORE_CASE)
    private val emailPattern = Regex("""\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b""", RegexOption.IGNORE_CASE)
    private val phonePattern = Regex("""\b(?:\+?1[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b""", RegexOption.IGNORE_CASE)
    private val medicalRecordNumberPattern = Regex("""\b(?:MRN|Medical Record|Record #)[:=\s]*[A-Z0-9-]{4,}\b""", RegexOption.IGNORE_CASE)
    // Updated path pattern
    private val userDataPathPattern = Regex("""(/[^\s/]+)*/(?:user|home|Users|patient|data|private|personal|documents|downloads|desktop)/[^\s]*""", RegexOption.IGNORE_CASE)
    private val datePattern = Regex("""\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b""", RegexOption.IGNORE_CASE)
    private val creditCardPattern = Regex("""\b(?:\d{4}[-.\s]?){3}\d{4}\b""", RegexOption.IGNORE_CASE)
    private val ipv4Pattern = Regex("""\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""")
    private val ipv6Pattern = Regex("""\b([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\b""")
    private val macAddressPattern = Regex("""\b(?:[0-9A-Fa-f]{2}[:-]){5}(?:[0-9A-Fa-f]{2})\b""")
    private val uuidPattern = Regex("""\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\b""")
    private val driversLicensePattern = Regex("""\b(?:DL|DLN|Driver['']?s?\s*License|License\s*#?)[:=\s]*[A-Z0-9]{5,15}\b""", RegexOption.IGNORE_CASE)
    private val passportPattern = Regex("""\b(?:Passport|Passport\s*#?)[:=\s]*[A-Z0-9]{6,12}\b""", RegexOption.IGNORE_CASE)
    private val insurancePolicyPattern = Regex("""\b(?:Policy|Policy\s*#?|Insurance\s*Policy|Ins\s*#?)[:=\s]*[A-Z0-9-]{6,20}\b""", RegexOption.IGNORE_CASE)
    private val accountNumberPattern = Regex("""\b(?:Account|Acct|Account\s*#?|Acct\s*#?)[:=\s]*\d{6,20}\b""", RegexOption.IGNORE_CASE)
    private val apiKeyPattern = Regex("""\b(?:api[_-]?key|apikey|api_key|access[_-]?key|secret[_-]?key)[:=\s]*['"]?[A-Za-z0-9_\-]{20,}['"]?\b""", RegexOption.IGNORE_CASE)
    private val jwtTokenPattern = Regex("""\bgl-[\w-]+\b|eyJ[A-Za-z0-9_-]*\.[A-Za-z0-9_-]*\.[A-Za-z0-9_-]*\b""")
    private val authTokenPattern = Regex("""\b(?:Bearer|Basic|Token|Authorization)[:\s]+[A-Za-z0-9_\-\.+/=]{20,}\b""", RegexOption.IGNORE_CASE)
    private val dbConnectionPattern = Regex("""\b(?:jdbc|mongodb|postgresql|mysql|sqlite|redis)://[^\s]+""", RegexOption.IGNORE_CASE)
    private val urlWithQueryPattern = Regex("""https?://[^\s]+[?&](?:token|key|password|secret|auth|session|user|email|phone|ssn|mrn|patient|id)=[^\s&]+""", RegexOption.IGNORE_CASE)
    private val zipCodePattern = Regex("""\b(?:ZIP|Zip|Postal\s*Code|ZIP\s*Code)[:=\s]*\d{5}(?:-\d{4})?\b""", RegexOption.IGNORE_CASE)
    private val healthPlanBeneficiaryPattern = Regex("""\b(?:Beneficiary|Beneficiary\s*#?|Health\s*Plan|Member\s*ID|Member\s*#?)[:=\s]*[A-Z0-9-]{6,20}\b""", RegexOption.IGNORE_CASE)
    private val deviceIdPattern = Regex("""\b(?:IMEI|Serial\s*Number|Serial\s*#?|Device\s*ID|Device\s*Identifier|UDID)[:=\s]*[A-Z0-9-]{8,20}\b""", RegexOption.IGNORE_CASE)
    private val bankAccountPattern = Regex("""\b(?:Bank\s*Account|Account\s*Number|Acct\s*Number)[:=\s]*\d{8,20}\b""", RegexOption.IGNORE_CASE)
    private val routingNumberPattern = Regex("""\b(?:Routing|Routing\s*#?|ABA|RTN)[:=\s]*\d{9}\b""", RegexOption.IGNORE_CASE)
    private val isoDatePattern = Regex("""\b\d{4}-\d{2}-\d{2}(?:T\d{2}:\d{2}:\d{2}(?:\.\d{3})?Z?)?\b""")
    private val passwordFieldPattern = Regex("""\b(?:password|passwd|pwd|pass|secret)[:=\s]*['"]?[^\s'"]{4,}['"]?\b""", RegexOption.IGNORE_CASE)

    // --- NEW PATTERNS ---
    
    // Coordinates: Lat/Long (e.g., 37.7749, -122.4194) - Separate checks for Lat and Long for better matching
    private val coordinatesPattern = Regex("""\b(?:Lat(?:itude)?|Lng|Lon(?:gitude)?|Coords?)[:=\s]*[-+]?\d{1,3}\.\d{3,}\b""", RegexOption.IGNORE_CASE)
    
    // Ages: Contextual (e.g., "Age: 45", "45 years old")
    private val agePattern = Regex("""\b(?:Age|Years\s*old)[:=\s]*\d{1,3}\b""", RegexOption.IGNORE_CASE)
    
    // Vehicle VIN (17 chars, alphanumeric, excluding I, O, Q usually, but strictly 17 alphanumeric is safeish)
    private val vinPattern = Regex("""\b[A-HJ-NPR-Z0-9]{17}\b""", RegexOption.IGNORE_CASE)
    
    // License Plate (Contextual)
    private val licensePlatePattern = Regex("""\b(?:License\s*Plate|Plate\s*Number|Vehicle\s*ID)[:=\s]*[A-Z0-9-]{4,10}\b""", RegexOption.IGNORE_CASE)
    
    // Biometric Terms (Contextual)
    private val biometricPattern = Regex("""\b(?:Fingerprint|Face\s*ID|Touch\s*ID|Retina|Biometric|Iris)[:=\s]*[A-Za-z0-9+/=]{10,}\b""", RegexOption.IGNORE_CASE)
    
    // OAuth/Refresh Tokens / Session Cookies (Common formats)
    private val oauthTokenPattern = Regex("""\b(?:access_token|refresh_token|session_id|JSESSIONID|PHPSESSID)[:=\s]*[A-Za-z0-9-._]+""", RegexOption.IGNORE_CASE)
    
    // Private Keys (PEM format)
    private val privateKeyPattern = Regex("""-----BEGIN [A-Z ]+ PRIVATE KEY-----[\s\S]*?-----END [A-Z ]+ PRIVATE KEY-----""")
    
    // Certificates (PEM format)
    private val certificatePattern = Regex("""-----BEGIN CERTIFICATE-----[\s\S]*?-----END CERTIFICATE-----""")
    
    // Cloud Keys
    // AWS: AKIA..., ASIA... (20 chars)
    private val awsKeyPattern = Regex("""\b(AKIA|ASIA)[A-Z0-9]{16}\b""")
    // Google API Key: AIza... (39 chars)
    private val gcpKeyPattern = Regex("""\bAIza[0-9A-Za-z-_]{35}\b""")
    // Azure SAS / Connection Strings (Contextual key=value)
    private val azureSecretPattern = Regex("""\b(?:SharedAccessKey|AccountKey)=([A-Za-z0-9+/=]+)""", RegexOption.IGNORE_CASE)
    
    // Location Contextual (Best Effort for City/State/Street) - Less greedy, stop at comma or newline
    private val addressContextPattern = Regex("""\b(?:Address|Street|City|State|Country|Zip)[:=\s]+[^,\n]+""", RegexOption.IGNORE_CASE)

    /**
     * Configuration for sanitization behavior
     */
    data class SanitizationConfig(
        val patientNames: List<String> = emptyList(),
        val customPatterns: List<Regex> = emptyList(),
        
        // Existing Flags
        val redactEmails: Boolean = true,
        val redactPhones: Boolean = true,
        val redactSSNs: Boolean = true,
        val redactMRNs: Boolean = true,
        val redactUserPaths: Boolean = true,
        val redactDates: Boolean = false,
        val redactCreditCards: Boolean = true,
        val redactIPAddresses: Boolean = true,
        val redactMACAddresses: Boolean = true,
        val redactUUIDs: Boolean = false,
        val redactDriversLicense: Boolean = true,
        val redactPassport: Boolean = true,
        val redactInsurancePolicy: Boolean = true,
        val redactAccountNumbers: Boolean = true,
        val redactAPIKeys: Boolean = true,
        val redactJWTTokens: Boolean = true,
        val redactAuthTokens: Boolean = true,
        val redactDBConnections: Boolean = true,
        val redactSensitiveURLs: Boolean = true,
        val redactZIPCodes: Boolean = false,
        val redactHealthPlanBeneficiary: Boolean = true,
        val redactDeviceIDs: Boolean = true,
        val redactBankAccounts: Boolean = true,
        val redactRoutingNumbers: Boolean = true,
        val redactISODates: Boolean = false,
        val redactPasswordFields: Boolean = true,
        
        // --- NEW FLAGS ---
        val redactCityNames: Boolean = true,    
        val redactStateNames: Boolean = true,   
        val redactCountryNames: Boolean = false, 
        val redactStreetAddresses: Boolean = true,
        val redactCoordinates: Boolean = true,
        val redactAges: Boolean = true,
        val redactVehicleVIN: Boolean = true,
        val redactLicensePlateNumbers: Boolean = true,
        val redactBiometricTerms: Boolean = true,
        val redactNamedFiles: Boolean = true, 
        val redactFreeTextPHI: Boolean = true, 
        val redactOAuthTokens: Boolean = true,
        val redactRefreshTokens: Boolean = true,
        val redactSessionCookies: Boolean = true,
        val redactPrivateKeys: Boolean = true,
        val redactCertificates: Boolean = true,
        val redactAWSKeys: Boolean = true,
        val redactGCPKeys: Boolean = true,
        val redactAzureSecrets: Boolean = true
    )
    
    /**
     * Sanitize a stack trace string by redacting potential PHI.
     * 
     * @param stackTrace The original stack trace string
     * @param config Configuration for sanitization behavior (default: all enabled)
     * @return Sanitized stack trace with PHI redacted
     */
    fun sanitize(
        stackTrace: String,
        config: SanitizationConfig = SanitizationConfig()
    ): String {
        var sanitized = stackTrace
        
        // 1. Long blocks / Keys / Certificates
        if (config.redactPrivateKeys) sanitized = sanitized.replace(privateKeyPattern, REDACTED_PLACEHOLDER)
        if (config.redactCertificates) sanitized = sanitized.replace(certificatePattern, REDACTED_PLACEHOLDER)
        if (config.redactAWSKeys) sanitized = sanitized.replace(awsKeyPattern, REDACTED_PLACEHOLDER)
        if (config.redactGCPKeys) sanitized = sanitized.replace(gcpKeyPattern, REDACTED_PLACEHOLDER)
        if (config.redactAzureSecrets) {
             sanitized = sanitized.replace(azureSecretPattern) { matchResult ->
                 val fullMatch = matchResult.value
                 val key = matchResult.groupValues[1]
                 fullMatch.replace(key, REDACTED_PLACEHOLDER)
             }
        }
        
        // 2. Tokens and Auth
        if (config.redactAPIKeys) sanitized = sanitized.replace(apiKeyPattern, REDACTED_PLACEHOLDER)
        if (config.redactJWTTokens) sanitized = sanitized.replace(jwtTokenPattern, REDACTED_PLACEHOLDER)
        if (config.redactAuthTokens) sanitized = sanitized.replace(authTokenPattern, REDACTED_PLACEHOLDER)
        if (config.redactOAuthTokens || config.redactRefreshTokens || config.redactSessionCookies) {
            sanitized = sanitized.replace(oauthTokenPattern, REDACTED_PLACEHOLDER)
        }
        
        // 3. Identifiers
        if (config.redactSSNs) sanitized = sanitized.replace(ssnPattern, REDACTED_PLACEHOLDER)
        if (config.redactEmails) sanitized = sanitized.replace(emailPattern, REDACTED_PLACEHOLDER)
        if (config.redactPhones) sanitized = sanitized.replace(phonePattern, REDACTED_PLACEHOLDER)
        if (config.redactCreditCards) sanitized = sanitized.replace(creditCardPattern, REDACTED_PLACEHOLDER)
        if (config.redactVehicleVIN) sanitized = sanitized.replace(vinPattern, REDACTED_PLACEHOLDER)
        if (config.redactDeviceIDs) sanitized = sanitized.replace(deviceIdPattern, REDACTED_PLACEHOLDER)
        
        // 4. Contextual PHI
        if (config.redactMRNs) sanitized = sanitized.replace(medicalRecordNumberPattern, REDACTED_PLACEHOLDER)
        if (config.redactAges) sanitized = sanitized.replace(agePattern, REDACTED_PLACEHOLDER)
        if (config.redactDriversLicense) sanitized = sanitized.replace(driversLicensePattern, REDACTED_PLACEHOLDER)
        if (config.redactPassport) sanitized = sanitized.replace(passportPattern, REDACTED_PLACEHOLDER)
        if (config.redactInsurancePolicy) sanitized = sanitized.replace(insurancePolicyPattern, REDACTED_PLACEHOLDER)
        if (config.redactLicensePlateNumbers) sanitized = sanitized.replace(licensePlatePattern, REDACTED_PLACEHOLDER)
        if (config.redactBiometricTerms) sanitized = sanitized.replace(biometricPattern, REDACTED_PLACEHOLDER)
        
        // 5. Financial
        if (config.redactAccountNumbers) sanitized = sanitized.replace(accountNumberPattern, REDACTED_PLACEHOLDER)
        if (config.redactBankAccounts) sanitized = sanitized.replace(bankAccountPattern, REDACTED_PLACEHOLDER)
        if (config.redactRoutingNumbers) sanitized = sanitized.replace(routingNumberPattern, REDACTED_PLACEHOLDER)

        // 6. Network
        if (config.redactIPAddresses) {
            sanitized = sanitized.replace(ipv4Pattern, REDACTED_PLACEHOLDER)
            sanitized = sanitized.replace(ipv6Pattern, REDACTED_PLACEHOLDER)
        }
        if (config.redactMACAddresses) sanitized = sanitized.replace(macAddressPattern, REDACTED_PLACEHOLDER)
        if (config.redactDBConnections) sanitized = sanitized.replace(dbConnectionPattern, REDACTED_PLACEHOLDER)
        if (config.redactSensitiveURLs) sanitized = sanitized.replace(urlWithQueryPattern, REDACTED_PLACEHOLDER)
        
        // 7. Files
        if (config.redactUserPaths || config.redactNamedFiles) {
            sanitized = sanitized.replace(userDataPathPattern, REDACTED_PLACEHOLDER)
        }

        // 8. Locations
        if (config.redactCoordinates) sanitized = sanitized.replace(coordinatesPattern, REDACTED_PLACEHOLDER)
        if (config.redactZIPCodes) sanitized = sanitized.replace(zipCodePattern, REDACTED_PLACEHOLDER)
        
        if (config.redactCityNames || config.redactStateNames || config.redactStreetAddresses || config.redactCountryNames) {
            sanitized = sanitized.replace(addressContextPattern) { matchResult ->
                val fullMatch = matchResult.value
                val label = fullMatch.substringBefore(":").trim()
                
                var shouldRedact = false
                if (config.redactCityNames && label.contains("City", ignoreCase = true)) shouldRedact = true
                if (config.redactStateNames && label.contains("State", ignoreCase = true)) shouldRedact = true
                if (config.redactCountryNames && label.contains("Country", ignoreCase = true)) shouldRedact = true
                if (config.redactStreetAddresses && (label.contains("Address", ignoreCase = true) || label.contains("Street", ignoreCase = true))) shouldRedact = true
                
                if (shouldRedact) "$label: $REDACTED_PLACEHOLDER" else fullMatch
            }
        }
        
        // 9. Generic
        if (config.redactHealthPlanBeneficiary) sanitized = sanitized.replace(healthPlanBeneficiaryPattern, REDACTED_PLACEHOLDER)
        if (config.redactPasswordFields) sanitized = sanitized.replace(passwordFieldPattern, REDACTED_PLACEHOLDER)
        if (config.redactUUIDs) sanitized = sanitized.replace(uuidPattern, REDACTED_PLACEHOLDER)
        if (config.redactDates || config.redactISODates) {
             sanitized = sanitized.replace(datePattern, REDACTED_PLACEHOLDER)
             sanitized = sanitized.replace(isoDatePattern, REDACTED_PLACEHOLDER)
        }

        // 10. Custom
        config.patientNames.forEach { name ->
            if (name.isNotBlank()) {
                val namePattern = Regex("""\b${Regex.escape(name)}\b""", RegexOption.IGNORE_CASE)
                sanitized = sanitized.replace(namePattern, REDACTED_PLACEHOLDER)
            }
        }
        
        config.customPatterns.forEach { pattern ->
            sanitized = sanitized.replace(pattern, REDACTED_PLACEHOLDER)
        }
        
        return sanitized
    }
    
    /**
     * Create a default HIPAA-compliant sanitization config.
     */
    fun createHipaaCompliantConfig(
        patientNames: List<String> = emptyList(),
        customPatterns: List<Regex> = emptyList()
    ): SanitizationConfig {
        return SanitizationConfig(
            patientNames = patientNames,
            customPatterns = customPatterns,
            redactDates = false,
            redactUUIDs = false,
            redactCountryNames = false,
            redactZIPCodes = false,
            redactISODates = false
        )
    }
}
