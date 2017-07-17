package org.example.controller;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ControllerAdvice
public class ExceptionController {
    private static final Logger LOGGER = Logger.getLogger(ExceptionController.class.getName());

    private final ServletContext servletContext;

    @Autowired
    public ExceptionController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public void exception(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse,
                          Exception exception) throws IOException, PebbleException {
        exception.printStackTrace(System.err);

        httpServletResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter printWriter = httpServletResponse.getWriter();

        if (exception instanceof NoHandlerFoundException) {
            printWriter.println("<h1>404</h1>");
        } else {
            Map<String, Object> context = new HashMap<>();
            context.put("exceptionClass", exception.getClass().getName());
            context.put("exceptionMessage", exception.getMessage());
            context.put("contextPath", servletContext.getContextPath());

            List<Map<String, Object>> stackTraceList = new LinkedList<>();

            for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
                Map<String, Object> stackTraceElementMap = new HashMap<>();
                stackTraceElementMap.put("fileName", stackTraceElement.getFileName());
                stackTraceElementMap.put("methodName", stackTraceElement.getMethodName());
                stackTraceElementMap.put("lineNumber", String.valueOf(stackTraceElement.getLineNumber()));
                stackTraceElementMap.put("className", stackTraceElement.getClassName());

                stackTraceList.add(stackTraceElementMap);
            }
            context.put("stackTraceList", stackTraceList);

            PebbleEngine engine = new PebbleEngine.Builder().build();
            PebbleTemplate compiledTemplate = engine.getTemplate("templates/error.html");

            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);

            printWriter.println(writer.toString());
        }
    }
}