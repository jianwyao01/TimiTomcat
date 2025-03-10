package test;

import server.Request;
import server.Response;
import server.Servlet;

public class HelloServlet implements Servlet {
    public void service(Request request, Response response) throws Exception {
        String content = "<!DOCTYPE html>\n<html>\n"
                + "<head><meta charset=\"utf-8\"><title>Test</title></head>\n"
                + "<body bgcolor=\"#f0f0f0\">\n"
                + "<h1 align=\"center\">Hello World 你好</h1>\n"
                + "</body></html>";
        response.getOutput().write(content.getBytes("utf-8"));
    }
}