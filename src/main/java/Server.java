import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Server {
    Map<String, Handler> mapHandler = new HashMap<>();//для сбора методов-путей с Handler

    List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    ServerSocket serverSocket;
    Socket socket;
    static int countAccept = 0;

    public void initialize(int i) {

        try {
            serverSocket = new ServerSocket(i);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start1() {

        while (true) {
            countAccept++;

            System.out.println(LocalDateTime.now().toString() + "start1: запускаю newAccept (ожидание нового подключения) " + countAccept);


            newAccept1();
        }
    }

    public String waitAccept() {
        try (
                final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final var requestLine = in.readLine();
            System.out.println(requestLine);

            if (requestLine == null) {
                out.flush();
                return null;
            }
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {

                return null;
            }


            final String path;
            if (parts[1].contains("?")) {
                path = getQueryPath(parts[1]);//отделили путь от методов
            } else {
                path = parts[1];
            }



            if (!validPaths.contains(path)) {

                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());

                out.flush();

                return null;
            }
            

            Request request = new Request();
            request.setRequestMethod(parts[0]);
            request.setRequestMessage(path);
            System.out.println(path);


            String pathMessages = request.requestMessage;//и вот получили для этой переменной путь без парамтров,

            BufferedOutputStream responseStream;
//           Выводим данные параметров через этот метод
            System.out.println(getQueryParam(parts[1]));

            final String keyHahdler = request.requestMethod + pathMessages;
            System.out.println("Приступаем к выбору Handler");
            if (mapHandler.containsKey(keyHahdler)) {
                System.out.println("Handler выбран");
                responseStream = new BufferedOutputStream(socket.getOutputStream());
                mapHandler.get(keyHahdler).handle(request, responseStream);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String newAccept1() {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        Callable<String> myCallable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return waitAccept();
            }
        };
        try {
            socket = serverSocket.accept();

            Future<String> task = threadPool.submit(myCallable);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addHandler(String method, String messages, Handler handler) {
        System.out.println("Handler добавлен");
        mapHandler.put((method + messages), handler);
    }

    public List<NameValuePair> getQueryParam(String name) {
        //получаем список параметров из URL
        URI uri;

        try {
            uri = new URI(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        return URLEncodedUtils.parse(uri, Charset.defaultCharset());
    }


    public String getQueryPath(String name) {

        return name.substring(0, name.indexOf('?'));
    }


    public static class Request {
        public String requestMethod;
        public String requestMessage;
        public String requestBody;
        public String headBody;

        public void setRequestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
        }

        public void setRequestMessage(String requestMessage) {
            this.requestMessage = requestMessage;
        }

        public void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }

        public void setHeadBody(String headBody) {
            this.headBody = headBody;
        }
    }


}
