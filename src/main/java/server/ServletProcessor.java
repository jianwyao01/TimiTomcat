package server;

import javax.servlet.Servlet;

public class ServletProcessor {

    public void process(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf('/') + 1);

        response.setCharacterEncoding("UTF-8");
        Class<?> servletClass = null;
        try {
            servletClass = HttpConnector.loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            HttpRequestFacade httpRequestFacade = new HttpRequestFacade(request);
            HttpResponseFacade httpResponseFacade = new HttpResponseFacade(response);
            System.out.println("Call Service()");
            servlet.service(httpRequestFacade, httpResponseFacade);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

}
