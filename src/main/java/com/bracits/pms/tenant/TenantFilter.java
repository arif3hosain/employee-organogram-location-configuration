package com.bracits.pms.tenant;

import com.bracits.pms.dto.record.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Resolves the tenant of every /api request and puts it in {@link TenantContext} for the length of
 * the request. 400 if the tenant id is missing or not a number, 403 if it is unknown or inactive.
 */
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final TenancyProperties props;
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String raw = request.getHeader(props.header());
        Long tenantId;
        if (raw == null || raw.isBlank()) {
            tenantId = props.defaultTenantId();
            if (tenantId == null) {
                reject(response, HttpStatus.BAD_REQUEST, "Tenant is required (header " + props.header() + ")");
                return;
            }
        } else {
            try {
                tenantId = Long.valueOf(raw.trim());
            } catch (NumberFormatException e) {
                reject(response, HttpStatus.BAD_REQUEST, "Tenant id must be a number");
                return;
            }
        }
        if (!isActive(tenantId)) {
            reject(response, HttpStatus.FORBIDDEN, "Unknown or inactive tenant");
            return;
        }
        TenantContext.set(tenantId);
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isActive(Long tenantId) {
        List<String> status = jdbc.queryForList("select status from tenant where id = ?", String.class, tenantId);
        return status.size() == 1 && "ACTIVE".equals(status.getFirst());
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(response.getOutputStream(), ApiResponse.fail(message));
    }
}
