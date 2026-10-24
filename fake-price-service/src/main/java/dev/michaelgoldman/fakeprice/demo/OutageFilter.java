package dev.michaelgoldman.fakeprice.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// demo only: while the switch is off, the price api answers 503, like a price system that is down.
// the demo controls (/demo/...) and actuator keep working, so it can be turned on again.
@Component
public class OutageFilter extends OncePerRequestFilter {
    private final OutageSwitch outageSwitch;

    public OutageFilter(OutageSwitch outageSwitch) {
        this.outageSwitch = outageSwitch;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/prices") || path.startsWith("/skus"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!outageSwitch.isDown()) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"title":"Service Unavailable","status":503,"detail":"The price system is turned off (demo)."}""");
    }
}
