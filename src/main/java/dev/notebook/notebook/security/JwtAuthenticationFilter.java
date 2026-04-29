package dev.notebook.notebook.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String jwt = extractJwtFromRequest(request);
      if (StringUtils.hasText(jwt)) {
        if (jwtUtils.validateToken(jwt)) {
          Long userId = jwtUtils.getUserIdFromToken(jwt);
          String username = jwtUtils.getUsernameFromToken(jwt);
          log.debug("JWT validated successfully for user: {} (id: {})", username, userId);

           UsernamePasswordAuthenticationToken authentication =
               new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
           authentication.setDetails(username);
           SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          log.warn("JWT validation failed for token: {}", jwt.substring(0, Math.min(20, jwt.length())));
        }
      }
    } catch (Exception ex) {
      log.error("Could not set user authentication", ex);
    }

    filterChain.doFilter(request, response);
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
