/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Vulnerable controller for security demonstration purposes.
 * WARNING: This code contains intentional security vulnerabilities.
 * DO NOT USE IN PRODUCTION!
 */
@Controller
public class VulnerableController {

    private static final Logger logger = Logger.getLogger(VulnerableController.class.getName());
    
    // Hardcoded credentials - Security vulnerability
    private static final String DB_URL = "jdbc:mysql://localhost:3306/petclinic";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "password123";

    /**
     * SQL Injection vulnerability - user input directly concatenated into SQL query
     */
    @GetMapping("/vulnerable/search")
    @ResponseBody
    public String searchOwners(@RequestParam String name) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            
            // VULNERABLE: SQL Injection - user input directly concatenated
            String query = "SELECT * FROM owners WHERE first_name = '" + name + "'";
            logger.info("Executing query: " + query); // Information disclosure
            
            ResultSet rs = stmt.executeQuery(query);
            StringBuilder result = new StringBuilder();
            
            while (rs.next()) {
                result.append("Owner: ").append(rs.getString("first_name"))
                      .append(" ").append(rs.getString("last_name")).append("<br>");
            }
            
            conn.close();
            return result.toString();
            
        } catch (Exception e) {
            // Information disclosure - stack trace exposed
            return "Error: " + e.getMessage() + "\n" + e.getStackTrace()[0].toString();
        }
    }

    /**
     * Cross-Site Scripting (XSS) vulnerability - user input not sanitized
     */
    @GetMapping("/vulnerable/display")
    public String displayMessage(@RequestParam String message, Model model) {
        // VULNERABLE: XSS - user input directly added to model without sanitization
        model.addAttribute("userMessage", message);
        return "vulnerable/display";
    }

    /**
     * Path Traversal vulnerability - file path not validated
     */
    @GetMapping("/vulnerable/file")
    @ResponseBody
    public String readFile(@RequestParam String filename) {
        try {
            // VULNERABLE: Path traversal - no validation of file path
            File file = new File("/var/log/" + filename);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            
            return new String(data);
            
        } catch (IOException e) {
            return "File not found: " + filename;
        }
    }

    /**
     * Command Injection vulnerability - user input passed to system command
     */
    @PostMapping("/vulnerable/ping")
    @ResponseBody
    public String pingHost(@RequestParam String host) {
        try {
            // VULNERABLE: Command injection - user input directly passed to system command
            Process process = Runtime.getRuntime().exec("ping -c 1 " + host);
            process.waitFor();
            
            return "Ping completed for host: " + host;
            
        } catch (Exception e) {
            return "Error executing ping: " + e.getMessage();
        }
    }

    /**
     * Insecure Direct Object Reference - no authorization check
     */
    @GetMapping("/vulnerable/user")
    @ResponseBody
    public String getUserData(@RequestParam int userId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            
            // VULNERABLE: No authorization check - any user can access any user's data
            String query = "SELECT * FROM users WHERE id = " + userId;
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                return "User: " + rs.getString("username") + 
                       ", Email: " + rs.getString("email") +
                       ", SSN: " + rs.getString("ssn"); // Sensitive data exposure
            }
            
            conn.close();
            return "User not found";
            
        } catch (Exception e) {
            return "Database error: " + e.getMessage();
        }
    }

    /**
     * Weak cryptography - using deprecated MD5 hash
     */
    @PostMapping("/vulnerable/hash")
    @ResponseBody
    public String hashPassword(@RequestParam String password) {
        try {
            // VULNERABLE: Using weak MD5 hash algorithm
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "Hashed password: " + hexString.toString();
            
        } catch (Exception e) {
            return "Hashing error: " + e.getMessage();
        }
    }

    /**
     * Session fixation vulnerability - no session regeneration
     */
    @PostMapping("/vulnerable/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        
        // VULNERABLE: No session regeneration after login
        // VULNERABLE: No proper password verification
        if ("admin".equals(username) && "password".equals(password)) {
            request.getSession().setAttribute("user", username);
            request.getSession().setAttribute("role", "admin");
            return "redirect:/";
        }
        
        return "redirect:/login?error";
    }
}