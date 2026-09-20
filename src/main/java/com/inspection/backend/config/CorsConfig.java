package com.inspection.backend.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // The production frontend is served from the same origin and proxies /api
    // requests to this backend. No server-side CORS filter is required here.
}
