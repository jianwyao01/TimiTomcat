package server;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServletProcessor {
    private static String OKMessage = "HTTP/1.1 ${StatusCode} ${StatusName} \r\n"
            + "Content-Type: ${ContentType}\r\n"
            + "Content-Length: ${ContentLength}\r\n"
            + "Server: TimiTomcat\r\n"
            + "Date: ${ZonedDateTIme}\r\n"
            + "\r\n";

    public void process(Request request, Response response) {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf('/') + 1);
        URLClassLoader loader = null;
        OutputStream output = null;

        try {
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Class<?> servletClass = null;
        try {
            servletClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        output = response.getOutput();
        String head = composeResponseHead();
        try {
            output.write(head.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            servlet.service(request, response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }

        try {
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String composeResponseHead() {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("StatusCode", "200");
        valuesMap.put("StatusName", "OK");
        valuesMap.put("Content-Type", "text/html;charset=utf-8");
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(OKMessage);
    }

}
