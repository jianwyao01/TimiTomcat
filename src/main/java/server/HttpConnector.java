package server;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class HttpConnector implements Runnable {
    int minProcessors = 3;
    int maxProcessors = 10;
    int curProcessor = 0;
    Deque<HttpProcessor> processors = new ArrayDeque<>();

    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    public static URLClassLoader loader = null;

    public static Session createSession() {
        Session session = new Session();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        String sessionId = generateSessionId();
        session.setId(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    private static synchronized String generateSessionId() {
        Random random = new Random();
        long seed = System.currentTimeMillis();
        random.setSeed(seed);

        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte) (bytes[i] & 0x0f);

            if (b1 < 10) {
                sb.append((char) ('0' + b1));
            } else {
                sb.append((char) ('A' + (b1 - 10)));
            }
            if (b2 < 10) {
                sb.append((char) ('0' + b2));
            } else {
                sb.append((char) ('A' + (b2 - 10)));
            }
        }
        return sb.toString();
    }

    public void run() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < minProcessors; i++) {
            HttpProcessor initProcessor = new HttpProcessor(this);
            initProcessor.start();
            processors.push(initProcessor);
        }

        curProcessor = minProcessors;

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                HttpProcessor processor = createProcessor();
                if (processor == null) {
                    socket.close();
                    continue;
                }

                processor.assign(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private HttpProcessor createProcessor() {
        synchronized (processors) {
            if (!processors.isEmpty()) {
                return processors.pop();
            }
            if (curProcessor < maxProcessors) {
                return newProcessor();
            } else {
                return null;
            }
        }
    }

    private HttpProcessor newProcessor() {
        HttpProcessor initProcessor = new HttpProcessor(this);
        initProcessor.start();
        processors.push(initProcessor);
        curProcessor++;
        return processors.pop();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    void recycle(HttpProcessor processor) {
        processors.push(processor);
    }
}
