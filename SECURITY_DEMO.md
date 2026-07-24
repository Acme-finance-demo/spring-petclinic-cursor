# Security Vulnerabilities Demo

⚠️ **WARNING: This code contains intentional security vulnerabilities for demonstration purposes only. DO NOT USE IN PRODUCTION!**

## Overview

This branch contains intentionally vulnerable code to demonstrate various security issues that can be detected by GitLab's security scanning tools.

## Vulnerabilities Included

### 1. SQL Injection
- **File**: `VulnerableController.java`
- **Method**: `searchOwners()`
- **Description**: User input is directly concatenated into SQL queries without parameterization
- **Example**: `/vulnerable/search?name='; DROP TABLE owners; --`

### 2. Cross-Site Scripting (XSS)
- **File**: `VulnerableController.java`, `display.html`
- **Method**: `displayMessage()`
- **Description**: User input is displayed without proper escaping
- **Example**: `/vulnerable/display?message=<script>alert('XSS')</script>`

### 3. Path Traversal
- **File**: `VulnerableController.java`
- **Method**: `readFile()`
- **Description**: File paths are not validated, allowing access to arbitrary files
- **Example**: `/vulnerable/file?filename=../../../etc/passwd`

### 4. Command Injection
- **File**: `VulnerableController.java`
- **Method**: `pingHost()`
- **Description**: User input is passed directly to system commands
- **Example**: `/vulnerable/ping?host=localhost; cat /etc/passwd`

### 5. Hardcoded Credentials
- **Files**: `VulnerableController.java`, `vulnerable-config.properties`
- **Description**: Database credentials and API keys are hardcoded in source code

### 6. Weak Cryptography
- **File**: `VulnerableController.java`
- **Method**: `hashPassword()`
- **Description**: Using deprecated MD5 hash algorithm for password hashing

### 7. Insecure Direct Object Reference
- **File**: `VulnerableController.java`
- **Method**: `getUserData()`
- **Description**: No authorization checks for accessing user data

### 8. Information Disclosure
- **Files**: Multiple files
- **Description**: Stack traces, database queries, and sensitive data exposed in responses

### 9. Session Management Issues
- **File**: `VulnerableController.java`
- **Method**: `login()`
- **Description**: No session regeneration after login, weak session configuration

### 10. Client-Side Vulnerabilities
- **File**: `vulnerable.js`
- **Description**: DOM-based XSS, client-side authentication, insecure data handling

### 11. Configuration Issues
- **File**: `vulnerable-config.properties`
- **Description**: Insecure SSL/TLS settings, permissive CORS, debug mode enabled

## Security Scanning Tools

This code should trigger alerts in the following GitLab security scanners:

- **SAST (Static Application Security Testing)**: Will detect most code-level vulnerabilities
- **Secret Detection**: Will find hardcoded credentials and API keys
- **Dependency Scanning**: May find vulnerable dependencies
- **Container Scanning**: Will scan for vulnerabilities in container images

## Testing the Vulnerabilities

### Prerequisites
- Ensure the application is running in a safe, isolated environment
- Never test these vulnerabilities against production systems
- Use only for educational and demonstration purposes

### Example Requests

```bash
# SQL Injection
curl "http://localhost:8080/vulnerable/search?name=admin' OR '1'='1"

# XSS
curl "http://localhost:8080/vulnerable/display?message=<script>alert('XSS')</script>"

# Path Traversal
curl "http://localhost:8080/vulnerable/file?filename=../../../etc/passwd"

# Command Injection
curl -X POST "http://localhost:8080/vulnerable/ping" -d "host=localhost; whoami"
```

## Remediation

Each vulnerability type has well-established remediation techniques:

1. **SQL Injection**: Use parameterized queries/prepared statements
2. **XSS**: Implement proper input validation and output encoding
3. **Path Traversal**: Validate and sanitize file paths
4. **Command Injection**: Avoid system commands, use safe APIs
5. **Hardcoded Secrets**: Use environment variables or secret management
6. **Weak Crypto**: Use strong, modern cryptographic algorithms
7. **Access Control**: Implement proper authorization checks
8. **Information Disclosure**: Implement proper error handling
9. **Session Management**: Use secure session configuration
10. **Client-Side Issues**: Implement server-side validation and security

## Disclaimer

This code is created solely for educational and demonstration purposes. The vulnerabilities are intentional and should never be deployed to production environments. Always follow secure coding practices and conduct regular security assessments of your applications.