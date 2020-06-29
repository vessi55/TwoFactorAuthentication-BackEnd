package twofactorauth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import twofactorauth.entity.User;
import twofactorauth.exception.ElementNotFoundException;
import twofactorauth.exception.ErrorMessage;
import twofactorauth.service.JwtUserDetailsService;
import twofactorauth.service.UserService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final int BEARER_PREFIX_LENGHT = 7;
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String USER_WAS_DELETED = "User was deleted !";
    private static final String APPLICATION_JSON = "application/json";
    private static final String TOKEN_MISSING = "JWT Token is Missing !";
    private static final String TOKEN_EXPIRED = "JWT Token has Expired !";
    private static final String UNABLE_TO_GET_TOKEN = "Unable to get token";

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (isPathPermitted(request)) {
            chain.doFilter(request, response);
        } else {
            final String requestTokenHeader = request.getHeader(AUTHORIZATION);

            if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER)) {
                tryAcquiringAccessToken(request, response, chain, requestTokenHeader);
            } else {
                errorResponse(response, TOKEN_MISSING, HttpStatus.FORBIDDEN);
            }
        }
    }

    private void tryAcquiringAccessToken(HttpServletRequest request, HttpServletResponse response,
                                         FilterChain chain, String requestTokenHeader) throws IOException, ServletException {
        try {
            String jwtToken = requestTokenHeader.substring(BEARER_PREFIX_LENGHT);

            Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(request, email);
            }
            chain.doFilter(request, response);

        } catch (ElementNotFoundException e) {
            errorResponse(response, USER_WAS_DELETED, HttpStatus.GONE);
        } catch (IllegalArgumentException e) {
            errorResponse(response, UNABLE_TO_GET_TOKEN, HttpStatus.FORBIDDEN);
        } catch (ExpiredJwtException e) {
            errorResponse(response, TOKEN_EXPIRED, HttpStatus.FORBIDDEN);
        }
    }

    private void authenticateUser(HttpServletRequest request, String email) {
        User user = userService.findUserByEmail(email);
        request.setAttribute("userId", user.getUid());

        UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    private void errorResponse(HttpServletResponse response, String message, HttpStatus httpStatus) throws IOException {
        log.error(message);
        response.setStatus(httpStatus.value());
        response.setContentType(APPLICATION_JSON);
        response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorMessage(httpStatus, message)));
    }

    private boolean isPathPermitted(HttpServletRequest request) {

        String servletPath = request.getServletPath();

        return isUserPathPermitted(request) ||
                isActuatorPathPermitted(servletPath) ||
                isSwaggerPathPermitted(servletPath);
    }

    private boolean isUserPathPermitted(HttpServletRequest request) {

        return request.getServletPath().startsWith("/users") ;
    }

    private boolean isActuatorPathPermitted(String servletPath) {

        return servletPath.startsWith("/actuator");
    }

    private boolean isSwaggerPathPermitted(String servletPath) {

        return servletPath.startsWith("/swagger") ||
                servletPath.startsWith("/v2/api-docs") ||
                servletPath.startsWith("/webjars/springfox-swagger-ui/");
    }
}