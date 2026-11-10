import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

// Starts a small web server (built into Java, no libraries) and connects the website to TicketService
public class Main {
    static final TicketService service = new TicketService("data/tickets.csv");

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/tickets", Main::handleApi);   // data
        server.createContext("/", Main::handleStatic);           // website files
        server.start();
        System.out.println("IT Helpdesk running -> http://localhost:" + port);
    }

    // GET  /api/tickets          -> all tickets + stats
    // POST /api/tickets          -> create ticket
    // POST /api/tickets/update   -> change status / assignee
    // POST /api/tickets/delete   -> delete ticket
    static void handleApi(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (ex.getRequestMethod().equals("GET")) {
                String list = service.all().stream().map(Ticket::toJson).collect(Collectors.joining(","));
                send(ex, 200, "{\"stats\":" + service.statsJson() + ",\"tickets\":[" + list + "]}", "application/json");
                return;
            }
            Map<String, String> f = readForm(ex);
            if (path.endsWith("/update")) {
                String s = f.get("status");
                service.update(Integer.parseInt(f.get("id")), s == null ? null : Ticket.Status.valueOf(s), f.get("assignee"));
            } else if (path.endsWith("/delete")) {
                service.delete(Integer.parseInt(f.get("id")));
            } else {
                String title = f.getOrDefault("title", "").trim();
                if (title.isEmpty()) throw new IllegalArgumentException("Title is required");
                service.create(f.getOrDefault("type", "Incident"), title, f.getOrDefault("description", ""),
                        f.getOrDefault("requester", "Unknown"), Ticket.Priority.valueOf(f.getOrDefault("priority", "MEDIUM")));
            }
            send(ex, 200, "{\"ok\":true}", "application/json");
        } catch (Exception e) {
            send(ex, 400, "{\"error\":\"" + Ticket.esc(String.valueOf(e.getMessage())) + "\"}", "application/json");
        }
    }

    // Serves index.html, style.css, app.js from the web/ folder
    static void handleStatic(HttpExchange ex) throws IOException {
        String p = ex.getRequestURI().getPath();
        Path file = Paths.get("web", p.equals("/") ? "index.html" : p.substring(1)).normalize();
        if (!file.startsWith("web") || !Files.isRegularFile(file)) {
            send(ex, 404, "Not found", "text/plain");
            return;
        }
        String name = file.toString();
        String type = name.endsWith(".css") ? "text/css" : name.endsWith(".js") ? "application/javascript" : "text/html";
        send(ex, 200, new String(Files.readAllBytes(file), StandardCharsets.UTF_8), type + "; charset=utf-8");
    }

    static Map<String, String> readForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return map;
    }

    static void send(HttpExchange ex, int code, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
