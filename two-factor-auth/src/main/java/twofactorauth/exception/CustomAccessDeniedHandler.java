package twofactorauth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    public static final String APPLICATION_JSON = "application/json";
    public static final String RESOURCE_FORBIDDEN = "Resource forbidden";

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       org.springframework.security.access.AccessDeniedException e) throws IOException {

        httpServletResponse.setStatus(403);
        httpServletResponse.setContentType(APPLICATION_JSON);
        httpServletResponse.getWriter().write(new ObjectMapper().writeValueAsString(
                new ErrorMessage(HttpStatus.FORBIDDEN, RESOURCE_FORBIDDEN)));
    }
}
