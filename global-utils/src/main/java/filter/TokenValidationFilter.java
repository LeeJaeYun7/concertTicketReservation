package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String requestURI = httpRequest.getRequestURI();

        if (isTokenValidationRequired(requestURI)) {
            String token = httpRequest.getHeader("Authorization");
        }

        chain.doFilter(request, response);
    }

    private boolean isTokenValidationRequired(String requestURI) {
        List<String> validURIs = Arrays.asList(
                "/concertSchedule",
                "/reservation",
                "/seat"
        );

        return validURIs.stream().anyMatch(requestURI::startsWith);
    }
}
