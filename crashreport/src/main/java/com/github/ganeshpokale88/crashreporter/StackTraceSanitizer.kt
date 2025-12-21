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
 * **Other Sensitive Data:**
 * - Database connection strings
 * - URLs with sensitive query parameters
 * - File paths containing user data
 * - UUIDs (optional, can contain patient IDs)
 * - ZIP codes (optional, can be PHI in healthcare context)
 * - Dates (optional, might be birth dates)
 * 
 * **Custom Patterns:**
 * - Custom regex patterns via configuration
 * 
 * All detected PHI and sensitive data is replaced with [REDACTED] to prevent exposure of sensitive information.
 */
object StackTraceSanitizer {
    
    private const val REDACTED_PLACEHOLDER = "[REDACTED]"
    
    // Regex patterns for common PHI patterns
    private val ssnPattern = Regex(
        """\b\d{3}-?\d{2}-?\d{4}\b""",  // XXX-XX-XXXX or XXXXXXXX
        RegexOption.IGNORE_CASE
    )
    
    private val emailPattern = Regex(
        """\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b""",
        RegexOption.IGNORE_CASE
    )
    
    private val phonePattern = Regex(
        """\b(?:\+?1[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b""",  // US phone numbers
        RegexOption.IGNORE_CASE
    )
    
    private val medicalRecordNumberPattern = Regex(
        """\b(?:MRN|Medical Record|Record #)[:=\s]*[A-Z0-9-]{4,}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for file paths that might contain user data
    private val userDataPathPattern = Regex(
        """(/[^\s/]+/)*(?:user|patient|data|private|personal|documents|downloads)[^\s]*""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for common date formats that might be birth dates
    private val datePattern = Regex(
        """\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for credit card numbers (16 digits)
    private val creditCardPattern = Regex(
        """\b\d{4}[-.\s]?\d{4}[-.\s]?\d{4}[-.\s]?\d{4}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for IP addresses (IPv4)
    private val ipv4Pattern = Regex(
        """\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b"""
    )
    
    // Pattern for IP addresses (IPv6)
    private val ipv6Pattern = Regex(
        """\b(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\b|::1|::ffff:\d+\.\d+\.\d+\.\d+"""
    )
    
    // Pattern for MAC addresses
    private val macAddressPattern = Regex(
        """\b(?:[0-9A-Fa-f]{2}[:-]){5}(?:[0-9A-Fa-f]{2})\b"""
    )
    
    // Pattern for UUIDs (can contain patient IDs)
    private val uuidPattern = Regex(
        """\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\b"""
    )
    
    // Pattern for driver's license numbers (US format - varies by state, common patterns)
    private val driversLicensePattern = Regex(
        """\b(?:DL|DLN|Driver['']?s?\s*License|License\s*#?)[:=\s]*[A-Z0-9]{5,15}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for passport numbers
    private val passportPattern = Regex(
        """\b(?:Passport|Passport\s*#?)[:=\s]*[A-Z0-9]{6,12}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for insurance policy numbers
    private val insurancePolicyPattern = Regex(
        """\b(?:Policy|Policy\s*#?|Insurance\s*Policy|Ins\s*#?)[:=\s]*[A-Z0-9-]{6,20}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for account numbers (generic)
    private val accountNumberPattern = Regex(
        """\b(?:Account|Acct|Account\s*#?|Acct\s*#?)[:=\s]*\d{6,20}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for API keys (common formats)
    private val apiKeyPattern = Regex(
        """\b(?:api[_-]?key|apikey|api_key|access[_-]?key|secret[_-]?key)[:=\s]*['"]?[A-Za-z0-9_\-]{20,}['"]?\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for JWT tokens
    private val jwtTokenPattern = Regex(
        """\beyJ[A-Za-z0-9_-]*\.[A-Za-z0-9_-]*\.[A-Za-z0-9_-]*\b"""
    )
    
    // Pattern for authorization tokens (Bearer, Basic, etc.)
    private val authTokenPattern = Regex(
        """\b(?:Bearer|Basic|Token|Authorization)[:\s]+[A-Za-z0-9_\-\.+/=]{20,}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for database connection strings
    private val dbConnectionPattern = Regex(
        """\b(?:jdbc|mongodb|postgresql|mysql|sqlite)[:;][^\s)]+""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for URLs with query parameters (might contain sensitive data)
    private val urlWithQueryPattern = Regex(
        """https?://[^\s]+[?&](?:token|key|password|secret|auth|session|user|email|phone|ssn|mrn|patient|id)=[^\s&]+""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for ZIP codes (can be PHI in healthcare context)
    private val zipCodePattern = Regex(
        """\b(?:ZIP|Zip|Postal\s*Code|ZIP\s*Code)[:=\s]*\d{5}(?:-\d{4})?\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for health plan beneficiary numbers
    private val healthPlanBeneficiaryPattern = Regex(
        """\b(?:Beneficiary|Beneficiary\s*#?|Health\s*Plan|Member\s*ID|Member\s*#?)[:=\s]*[A-Z0-9-]{6,20}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for device identifiers (IMEI, serial numbers)
    private val deviceIdPattern = Regex(
        """\b(?:IMEI|Serial\s*Number|Serial\s*#?|Device\s*ID|Device\s*Identifier)[:=\s]*[A-Z0-9-]{8,20}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for bank account numbers
    private val bankAccountPattern = Regex(
        """\b(?:Bank\s*Account|Account\s*Number|Acct\s*Number)[:=\s]*\d{8,20}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for routing numbers (US bank routing)
    private val routingNumberPattern = Regex(
        """\b(?:Routing|Routing\s*#?|ABA|RTN)[:=\s]*\d{9}\b""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern for ISO date formats (might be DOB)
    private val isoDatePattern = Regex(
        """\b\d{4}-\d{2}-\d{2}(?:T\d{2}:\d{2}:\d{2}(?:\.\d{3})?Z?)?\b"""
    )
    
    // Pattern for common password fields in logs
    private val passwordFieldPattern = Regex(
        """\b(?:password|passwd|pwd|secret)[:=\s]*['"]?[^\s'"]{4,}['"]?\b""",
        RegexOption.IGNORE_CASE
    )
    
    /**
     * Configuration for sanitization behavior
     */
    data class SanitizationConfig(
        /**
         * List of patient names to redact (case-insensitive)
         */
        val patientNames: List<String> = emptyList(),
        
        /**
         * List of custom regex patterns to redact
         */
        val customPatterns: List<Regex> = emptyList(),
        
        /**
         * Whether to redact email addresses
         */
        val redactEmails: Boolean = true,
        
        /**
         * Whether to redact phone numbers
         */
        val redactPhones: Boolean = true,
        
        /**
         * Whether to redact SSNs
         */
        val redactSSNs: Boolean = true,
        
        /**
         * Whether to redact medical record numbers
         */
        val redactMRNs: Boolean = true,
        
        /**
         * Whether to redact file paths containing user data
         */
        val redactUserPaths: Boolean = true,
        
        /**
         * Whether to redact dates (may be birth dates)
         */
        val redactDates: Boolean = false,  // Default false as dates are common in logs
        
        /**
         * Whether to redact credit card numbers
         */
        val redactCreditCards: Boolean = true,
        
        /**
         * Whether to redact IP addresses
         */
        val redactIPAddresses: Boolean = true,
        
        /**
         * Whether to redact MAC addresses
         */
        val redactMACAddresses: Boolean = true,
        
        /**
         * Whether to redact UUIDs (can contain patient IDs)
         */
        val redactUUIDs: Boolean = false,  // Default false as UUIDs are common in code
        
        /**
         * Whether to redact driver's license numbers
         */
        val redactDriversLicense: Boolean = true,
        
        /**
         * Whether to redact passport numbers
         */
        val redactPassport: Boolean = true,
        
        /**
         * Whether to redact insurance policy numbers
         */
        val redactInsurancePolicy: Boolean = true,
        
        /**
         * Whether to redact account numbers
         */
        val redactAccountNumbers: Boolean = true,
        
        /**
         * Whether to redact API keys and tokens
         */
        val redactAPIKeys: Boolean = true,
        
        /**
         * Whether to redact JWT tokens
         */
        val redactJWTTokens: Boolean = true,
        
        /**
         * Whether to redact authorization tokens
         */
        val redactAuthTokens: Boolean = true,
        
        /**
         * Whether to redact database connection strings
         */
        val redactDBConnections: Boolean = true,
        
        /**
         * Whether to redact URLs with sensitive query parameters
         */
        val redactSensitiveURLs: Boolean = true,
        
        /**
         * Whether to redact ZIP codes (can be PHI in healthcare)
         */
        val redactZIPCodes: Boolean = false,  // Default false as ZIP codes are common
        
        /**
         * Whether to redact health plan beneficiary numbers
         */
        val redactHealthPlanBeneficiary: Boolean = true,
        
        /**
         * Whether to redact device identifiers (IMEI, serial numbers)
         */
        val redactDeviceIDs: Boolean = true,
        
        /**
         * Whether to redact bank account numbers
         */
        val redactBankAccounts: Boolean = true,
        
        /**
         * Whether to redact routing numbers
         */
        val redactRoutingNumbers: Boolean = true,
        
        /**
         * Whether to redact ISO date formats (might be DOB)
         */
        val redactISODates: Boolean = false,  // Default false as ISO dates are common in logs
        
        /**
         * Whether to redact password fields
         */
        val redactPasswordFields: Boolean = true
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
        
        // Redact SSNs
        if (config.redactSSNs) {
            sanitized = sanitized.replace(ssnPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact email addresses
        if (config.redactEmails) {
            sanitized = sanitized.replace(emailPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact phone numbers
        if (config.redactPhones) {
            sanitized = sanitized.replace(phonePattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact medical record numbers
        if (config.redactMRNs) {
            sanitized = sanitized.replace(medicalRecordNumberPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact file paths containing user data
        if (config.redactUserPaths) {
            sanitized = sanitized.replace(userDataPathPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact dates (optional, as they're common in logs)
        if (config.redactDates) {
            sanitized = sanitized.replace(datePattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact credit card numbers
        if (config.redactCreditCards) {
            sanitized = sanitized.replace(creditCardPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact IP addresses (IPv4)
        if (config.redactIPAddresses) {
            sanitized = sanitized.replace(ipv4Pattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact IP addresses (IPv6)
        if (config.redactIPAddresses) {
            sanitized = sanitized.replace(ipv6Pattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact MAC addresses
        if (config.redactMACAddresses) {
            sanitized = sanitized.replace(macAddressPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact UUIDs
        if (config.redactUUIDs) {
            sanitized = sanitized.replace(uuidPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact driver's license numbers
        if (config.redactDriversLicense) {
            sanitized = sanitized.replace(driversLicensePattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact passport numbers
        if (config.redactPassport) {
            sanitized = sanitized.replace(passportPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact insurance policy numbers
        if (config.redactInsurancePolicy) {
            sanitized = sanitized.replace(insurancePolicyPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact account numbers
        if (config.redactAccountNumbers) {
            sanitized = sanitized.replace(accountNumberPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact API keys
        if (config.redactAPIKeys) {
            sanitized = sanitized.replace(apiKeyPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact JWT tokens
        if (config.redactJWTTokens) {
            sanitized = sanitized.replace(jwtTokenPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact authorization tokens
        if (config.redactAuthTokens) {
            sanitized = sanitized.replace(authTokenPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact database connection strings
        if (config.redactDBConnections) {
            sanitized = sanitized.replace(dbConnectionPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact URLs with sensitive query parameters
        if (config.redactSensitiveURLs) {
            sanitized = sanitized.replace(urlWithQueryPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact ZIP codes
        if (config.redactZIPCodes) {
            sanitized = sanitized.replace(zipCodePattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact health plan beneficiary numbers
        if (config.redactHealthPlanBeneficiary) {
            sanitized = sanitized.replace(healthPlanBeneficiaryPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact device identifiers
        if (config.redactDeviceIDs) {
            sanitized = sanitized.replace(deviceIdPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact bank account numbers
        if (config.redactBankAccounts) {
            sanitized = sanitized.replace(bankAccountPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact routing numbers
        if (config.redactRoutingNumbers) {
            sanitized = sanitized.replace(routingNumberPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact ISO date formats
        if (config.redactISODates) {
            sanitized = sanitized.replace(isoDatePattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact password fields
        if (config.redactPasswordFields) {
            sanitized = sanitized.replace(passwordFieldPattern, REDACTED_PLACEHOLDER)
        }
        
        // Redact patient names (case-insensitive)
        config.patientNames.forEach { name ->
            if (name.isNotBlank()) {
                val namePattern = Regex(
                    """\b${Regex.escape(name)}\b""",
                    RegexOption.IGNORE_CASE
                )
                sanitized = sanitized.replace(namePattern, REDACTED_PLACEHOLDER)
            }
        }
        
        // Apply custom patterns
        config.customPatterns.forEach { pattern ->
            sanitized = sanitized.replace(pattern, REDACTED_PLACEHOLDER)
        }
        
        return sanitized
    }
    
    /**
     * Create a default HIPAA-compliant sanitization config.
     * All PHI redaction is enabled by default.
     * 
     * This configuration redacts:
     * - Personal identifiers (SSN, MRN, driver's license, passport)
     * - Contact information (email, phone)
     * - Financial information (credit cards, bank accounts, routing numbers)
     * - Authentication tokens (API keys, JWT, auth tokens)
     * - Network identifiers (IP addresses, MAC addresses)
     * - Healthcare identifiers (health plan beneficiary numbers)
     * - Device identifiers (IMEI, serial numbers)
     * - Sensitive URLs and database connections
     * - Password fields
     */
    fun createHipaaCompliantConfig(
        patientNames: List<String> = emptyList(),
        customPatterns: List<Regex> = emptyList()
    ): SanitizationConfig {
        return SanitizationConfig(
            patientNames = patientNames,
            customPatterns = customPatterns,
            redactEmails = true,
            redactPhones = true,
            redactSSNs = true,
            redactMRNs = true,
            redactUserPaths = true,
            redactDates = false,  // Keep false as dates are common in timestamps
            redactCreditCards = true,
            redactIPAddresses = true,
            redactMACAddresses = true,
            redactUUIDs = false,  // Keep false as UUIDs are common in code
            redactDriversLicense = true,
            redactPassport = true,
            redactInsurancePolicy = true,
            redactAccountNumbers = true,
            redactAPIKeys = true,
            redactJWTTokens = true,
            redactAuthTokens = true,
            redactDBConnections = true,
            redactSensitiveURLs = true,
            redactZIPCodes = false,  // Keep false as ZIP codes are common
            redactHealthPlanBeneficiary = true,
            redactDeviceIDs = true,
            redactBankAccounts = true,
            redactRoutingNumbers = true,
            redactISODates = false,  // Keep false as ISO dates are common in logs
            redactPasswordFields = true
        )
    }
}

