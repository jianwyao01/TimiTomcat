package server;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class ServletProcessor {

    public void process(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf('/') + 1);
        URLClassLoader loader = null;

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

        response.setCharacterEncoding("UTF-8");
        Class<?> servletClass = null;
        try {
            servletClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            response.sendHeaders();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            HttpRequestFacade httpRequestFacade = new HttpRequestFacade(request);
            HttpResponseFacade httpResponseFacade = new HttpResponseFacade(response);
            servlet.service(httpRequestFacade, httpResponseFacade);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

}
