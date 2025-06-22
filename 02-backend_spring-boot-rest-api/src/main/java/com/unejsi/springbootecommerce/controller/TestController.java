package com.unejsi.springbootecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // GET - accessible by authenticated users (USER and ADMIN)
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(Map.of(
            "message", "GET request successful - accessible by USER and ADMIN",
            "user", auth.getName(),
            "authorities", auth.getAuthorities().toString(),
            "method", "GET",
            "status", 200
        ));
    }

    // POST - only accessible by ADMIN
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> postData(@RequestBody(required = false) Map<String, Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(Map.of(
            "message", "POST request successful - only ADMIN can access",
            "user", auth.getName(),
            "authorities", auth.getAuthorities().toString(), 
            "method", "POST",
            "status", 200
        ));
    }

    // DELETE - only accessible by ADMIN
    @DeleteMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(Map.of(
            "message", "DELETE request successful - only ADMIN can access",
            "user", auth.getName(),
            "authorities", auth.getAuthorities().toString(),
            "method", "DELETE",
            "id", id,
            "status", 200
        ));
    }
}