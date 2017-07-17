package org.example.controller;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.example.token.ExtendedUsernamePasswordAuthenticationToken;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Controller
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    public static String PATH = "C:\\Users\\Niko\\Downloads";
//    public static String PATH = "/home/niko/Downloads";

    private final ServletContext servletContext;

    @Autowired
    public MainController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    @RequestMapping(value = "/secured/delete", method = RequestMethod.GET)
    @ResponseBody
    public void delete(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter printWriter = httpServletResponse.getWriter();


        
    }

    @RequestMapping(value = "/secured/files", method = RequestMethod.GET)
    @ResponseBody
    public void files(HttpServletRequest httpServletRequest,
                      HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter printWriter = httpServletResponse.getWriter();

        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate("templates/secured/files.html");

        List<String> files = fileList(PATH);

        Principal principal = httpServletRequest.getUserPrincipal();

        Map<String, Object> context = new HashMap<>();
        context.put("contextPath", servletContext.getContextPath());
        context.put("servletPath", httpServletRequest.getServletPath());
        context.put("_csrf", ((CsrfToken) httpServletRequest.getAttribute("_csrf")).getToken());
        context.put("files", files);
        context.put("username", principal != null ? principal.getName() : "unknown");

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        printWriter.println(writer.toString());
    }

    @RequestMapping(value = "/secured/upload", method = RequestMethod.POST)
    @ResponseBody
    public void upload(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setContentType("text/html;charset=UTF-8");

        JSONObject respond = new JSONObject();

        Collection<Part> parts = httpServletRequest.getParts();
        for (Part part : parts) {
            LOGGER.info("Uploaded : " + part.getName());
            Files.copy(part.getInputStream(), Paths.get(PATH + File.separator + part.getName()), REPLACE_EXISTING);
        }

        PrintWriter printWriter = httpServletResponse.getWriter();
        printWriter.println(respond.toString());
    }

    @RequestMapping(value = "/secured/download", method = RequestMethod.GET)
    @ResponseBody
    public void download(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setContentType("text/html");
        String file = httpServletRequest.getParameter("file");
        String referer = httpServletRequest.getHeader("referer");

        LOGGER.info("Request from : " + referer);

        String filePath = PATH + File.separator + file;
        LOGGER.info("file = " + filePath);
        File downloadFile = new File(filePath);
        FileInputStream inStream = new FileInputStream(downloadFile);

        // gets MIME type of the file
        String mimeType = servletContext.getMimeType(filePath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        LOGGER.info("MIME type: " + mimeType);

        // modifies response
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setContentLength((int) downloadFile.length());

        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", MimeUtility.encodeText(downloadFile.getName()));
        httpServletResponse.setHeader(headerKey, headerValue);

        // obtains response's output stream
        OutputStream outStream = httpServletResponse.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }

    @RequestMapping(value = "/secured/{name:.+}", method = RequestMethod.GET)
    @ResponseBody
    public void securedIndex(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             @PathVariable("name") String name) throws Exception {
        httpServletResponse.setContentType("text/html;charset=UTF-8");

        if ("error".equals(name)) {
            throw new RuntimeException("New Runtime Exception");
        }

        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate("templates/secured/index.html");

        Principal principal = httpServletRequest.getUserPrincipal();
        ExtendedUsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (ExtendedUsernamePasswordAuthenticationToken) principal;

        List<String> authorityList = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : usernamePasswordAuthenticationToken.getAuthorities()) {
            LOGGER.info(grantedAuthority.getAuthority());
            authorityList.add(grantedAuthority.getAuthority());
        }

        Map<String, Object> context = new HashMap<>();
        context.put("name", name);
        context.put("contextPath", servletContext.getContextPath());
        context.put("servletPath", httpServletRequest.getServletPath());
        context.put("username", usernamePasswordAuthenticationToken.getName());
        context.put("authorityList", authorityList);
        context.put("officialName", "Official Name");

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        PrintWriter printWriter = httpServletResponse.getWriter();

        printWriter.println(writer.toString());
    }


    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public void welcome(HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setContentType("text/html;charset=UTF-8");

        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate("templates/welcome.html");

        Map<String, Object> context = new HashMap<>();
        context.put("servletPath", httpServletRequest.getServletPath());

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        PrintWriter printWriter = httpServletResponse.getWriter();

        printWriter.println(writer.toString());
    }

    public static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    fileNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException ex) {
        }
        return fileNames;
    }

}
