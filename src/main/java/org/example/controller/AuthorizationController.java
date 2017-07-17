package org.example.controller;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class AuthorizationController {
    private static final Logger LOGGER = Logger.getLogger(AuthorizationController.class.getName());

    private final ServletContext servletContext;

    @Autowired
    public AuthorizationController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logoutPage(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(httpServletRequest, httpServletResponse, auth);
        }

        String referer = httpServletRequest.getHeader("Referer");

        httpServletResponse.sendRedirect(referer);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ResponseBody
    public void login(HttpServletRequest httpServletRequest,
                      HttpServletResponse httpServletResponse,
                      @RequestParam(value = "error", required = false) String error) throws Exception {
        httpServletResponse.setContentType("text/html;charset=UTF-8");

        PebbleEngine engine = new PebbleEngine.Builder().build();
//        PebbleTemplate compiledTemplate = engine.getTemplate("/templates/login/login.html");
        PebbleTemplate compiledTemplate = engine.getTemplate("templates/login/login.html");

        Map<String, Object> context = new HashMap<>();
        context.put("contextPath", servletContext.getContextPath());
        context.put("_csrf", ((CsrfToken) httpServletRequest.getAttribute("_csrf")).getToken());

        if (error != null) {
            context.put("error", "Invalid Credentials provided.");
        }

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        PrintWriter printWriter = httpServletResponse.getWriter();
        printWriter.println(writer.toString());
    }
}
