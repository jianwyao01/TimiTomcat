package server;

public interface Servlet {
    public void service(Request request, Response response) throws Exception;
}
