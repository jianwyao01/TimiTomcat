package server;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StaticResourceProcessor {
    private static final int BUFFER_SIZE = 1024;
    private static String fileNotFoundMessage = "HTTP/1.1 404 FIle Not Found\r\n"
            + "Content-Type: text/html\r\n"
            + "Content-Length: 23\r\n"
            + "\r\n"
            + "<h1>File Not Found</h1> ";
    private static String OKMessage = "HTTP/1.1 ${StatusCode} ${StatusName}\r\n"
            + "Content-Type: ${ContentType}\r\n"
            + "Content-Length: ${ContentLength}\r\n"
            + "Server: TimiTomcat\r\n"
            + "Date: ${ZonedDateTIme}\r\n"
            + "\r\n";

    public void process(Request request, Response response) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        OutputStream output = null;
        try {
            output = response.getOutput();
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if (file.exists()) {
                String head = composeResponseHead(file);
                output.write(head.getBytes("utf-8"));
                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
                output.flush();
            } else {
                output.write(fileNotFoundMessage.getBytes());
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    private String composeResponseHead(File file) {
        long fileLength = file.length();
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put("StatusCode", "200");
        valuesMap.put("StatusName", "OK");
        valuesMap.put("Content-Type", "text/html;charset=UTF-8");
        valuesMap.put("Content-Length", fileLength);
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(OKMessage);
    }
}
