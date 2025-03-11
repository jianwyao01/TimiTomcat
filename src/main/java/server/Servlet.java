package server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public interface Servlet {
    void init(ServletConfig servletConfig) throws ServletException;
    ServletConfig getServletConfig();
    void service(ServletRequest request, ServletResponse response) throws ServletException, IOException;
    String getServletInfo();
    void destroy();
}
