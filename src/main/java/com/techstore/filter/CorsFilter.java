package com.techstore.filter;

import java.io.IOException;

import javax.servlet.Filter; // Changed from jakarta.servlet
import javax.servlet.FilterChain; // Changed from jakarta.servlet
import javax.servlet.FilterConfig; // Changed from jakarta.servlet
import javax.servlet.ServletException; // Changed from jakarta.servlet
import javax.servlet.ServletRequest; // Changed from jakarta.servlet
import javax.servlet.ServletResponse; // Changed from jakarta.servlet
import javax.servlet.http.HttpServletRequest; // Changed from jakarta.servlet.http
import javax.servlet.http.HttpServletResponse; // Changed from jakarta.servlet.http

public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set CORS headers
        httpResponse.setHeader("Access-Control-Allow-Origin", "*"); // Allow all origins for development
        httpResponse.setHeader("Access-Control-Allow-Methods",
                              "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers",
                              "Content-Type, Authorization"); // Add other headers if used
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true"); // Allow cookies/sessions

        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}