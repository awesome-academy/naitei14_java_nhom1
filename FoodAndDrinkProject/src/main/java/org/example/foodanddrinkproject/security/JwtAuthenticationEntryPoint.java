package org.example.foodanddrinkproject.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.foodanddrinkproject.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


import java.io.IOException;


@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {


    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {


        logger.error("Responding with unauthorized error. Message - {}", authException.getMessage());


        ApiResponse apiResponse = new ApiResponse(false, "You are not authorized to access this resource.");


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);


        new ObjectMapper().writeValue(response.getOutputStream(), apiResponse);
    }
}
