package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {
    Socket socket;
    boolean available = false;
    HttpConnector connector;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    @Override
    public void run() {
        while (true) {
            Socket socket = await();
            if (socket == null) continue;
            process(socket);
            connector.recycle(this);
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void process(Socket socket) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            HttpRequest request = new HttpRequest(input);
            request.parse(socket);

            HttpResponse response = new HttpResponse(output);
            response.setRequest(request);

            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void assign(Socket socket) {
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.socket = socket;
        // false -> true: socket is available
        available = true;
        notify();
    }

    private synchronized Socket await() {
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Socket socket = this.socket;
        // true -> false: socket is being processed
        available = false;
        notifyAll();
        return socket;
    }


}
