import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.addHandler("GET", "/index.html", new Handler() {
            


            @Override
            public void handle(Server.Request request, BufferedOutputStream responseStream) {
                //todo
                try {
                    out.println("Handler сработал");
                    final var filePath = Path.of(".", "public", request.requestMessage);
                    final var mimeType = Files.probeContentType(filePath);


                    final var length = Files.size(filePath);
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseStream);
                    responseStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        server.initialize(9998);

        server.start1();

    }
}
