package org.example.controller;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException ade) throws IOException, ServletException {
        try {
            httpServletResponse.setContentType("text/html;charset=UTF-8");

            ServletContext servletContext = httpServletRequest.getServletContext();

            PebbleEngine engine = new PebbleEngine.Builder().build();
            PebbleTemplate compiledTemplate = engine.getTemplate("templates/secured/accessDenied.html");

            Principal principal = httpServletRequest.getUserPrincipal();

            Map<String, Object> context = new HashMap<>();
            context.put("contextPath", servletContext.getContextPath());
            context.put("servletPath", httpServletRequest.getServletPath());
            context.put("username", principal != null ? principal.getName() : "unknown");

            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);

            PrintWriter printWriter = httpServletResponse.getWriter();

            printWriter.println(writer.toString());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
