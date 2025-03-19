package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {
    Socket socket;
    boolean available = false;
    HttpConnector connector;
    private int serverPort = 0;
    private boolean keepAlive = false;
    private boolean http11 = true;

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
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            keepAlive = true;
            while (keepAlive) {
                HttpRequest request = new HttpRequest(input);
                request.parse(socket);

                if (request.getSessionId() == null || request.getSessionId().equals("")) {
                    request.getSession(true);
                }

                HttpResponse response = new HttpResponse(output);
                response.setRequest(request);

                try {
                    response.sendHeaders();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (request.getUri().startsWith("/servlet/")) {
                    ServletProcessor processor = new ServletProcessor();
                    processor.process(request, response);
                } else {
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request, response);
                }

                finishResponse(response);
                System.out.println("Response header connection-------" + response.getHeader("Connection"));
                if ("close".equals(response.getHeader("Connection"))) {
                    keepAlive = false;
                }
            }

            socket.close();
            socket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishResponse(HttpResponse response) {
        response.finishResponse();
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
