/**
 * Vulnerable JavaScript code for security demonstration
 * WARNING: This code contains intentional security vulnerabilities
 * DO NOT USE IN PRODUCTION!
 */

// Global variables with sensitive information
var API_KEY = "sk-1234567890abcdef";
var ADMIN_TOKEN = "admin_secret_token_123";

/**
 * DOM-based XSS vulnerability
 */
function displayUserInput() {
    var userInput = document.getElementById('userInput').value;
    // VULNERABLE: Direct innerHTML assignment without sanitization
    document.getElementById('output').innerHTML = userInput;
}

/**
 * Insecure random number generation
 */
function generateSessionId() {
    // VULNERABLE: Using Math.random() for security-sensitive operations
    return Math.random().toString(36).substring(2, 15);
}

/**
 * Client-side authentication bypass
 */
function checkAccess() {
    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;
    
    // VULNERABLE: Client-side authentication logic
    if (username === "admin" && password === "password123") {
        localStorage.setItem('isAuthenticated', 'true');
        localStorage.setItem('userRole', 'admin');
        window.location.href = '/admin';
        return true;
    }
    return false;
}

/**
 * Insecure data transmission
 */
function sendSensitiveData() {
    var creditCard = document.getElementById('creditCard').value;
    var ssn = document.getElementById('ssn').value;
    
    // VULNERABLE: Sending sensitive data over HTTP without encryption
    fetch('http://api.example.com/submit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            creditCard: creditCard,
            ssn: ssn,
            apiKey: API_KEY // Exposing API key
        })
    });
}

/**
 * Prototype pollution vulnerability
 */
function mergeObjects(target, source) {
    // VULNERABLE: No protection against prototype pollution
    for (let key in source) {
        if (typeof source[key] === 'object' && source[key] !== null) {
            if (!target[key]) target[key] = {};
            mergeObjects(target[key], source[key]);
        } else {
            target[key] = source[key];
        }
    }
    return target;
}

/**
 * Insecure eval usage
 */
function executeUserCode() {
    var userCode = document.getElementById('codeInput').value;
    // VULNERABLE: Using eval with user input
    try {
        eval(userCode);
    } catch (e) {
        console.error('Error executing code:', e);
    }
}

/**
 * Information disclosure through console logging
 */
function debugUser(user) {
    // VULNERABLE: Logging sensitive information
    console.log('User details:', {
        id: user.id,
        username: user.username,
        password: user.password, // Sensitive data in logs
        ssn: user.ssn,
        creditCard: user.creditCard
    });
}

/**
 * Insecure cookie handling
 */
function setUserSession(userData) {
    // VULNERABLE: Storing sensitive data in cookies without security flags
    document.cookie = "userToken=" + userData.token + "; path=/";
    document.cookie = "userRole=" + userData.role + "; path=/";
    document.cookie = "sessionData=" + JSON.stringify(userData) + "; path=/";
}

/**
 * CSRF vulnerability - no token validation
 */
function transferMoney() {
    var amount = document.getElementById('amount').value;
    var toAccount = document.getElementById('toAccount').value;
    
    // VULNERABLE: No CSRF protection
    fetch('/api/transfer', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            amount: amount,
            toAccount: toAccount
        })
    });
}

// VULNERABLE: Exposing internal functions globally
window.vulnerableFunctions = {
    displayUserInput,
    generateSessionId,
    checkAccess,
    sendSensitiveData,
    executeUserCode,
    debugUser,
    setUserSession,
    transferMoney
};