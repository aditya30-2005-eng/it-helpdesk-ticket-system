import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// All business logic lives here: add, update, delete, search stats, save/load
public class TicketService {
    private final List<Ticket> tickets = new ArrayList<>();   // Collections
    private final Path file;
    private int nextId = 1;

    public TicketService(String filePath) {
        this.file = Paths.get(filePath);
        load();
        if (tickets.isEmpty()) addSampleData();
    }

    public synchronized Ticket create(String type, String title, String desc, String requester, Ticket.Priority p) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"));
        Ticket t = type.equals("Incident")
                ? new Incident(nextId++, title, desc, requester, p, now)
                : new ServiceRequest(nextId++, title, desc, requester, p, now);
        tickets.add(t);
        save();
        return t;
    }

    public synchronized void update(int id, Ticket.Status status, String assignee) {
        Ticket t = find(id);
        if (status != null) t.setStatus(status);
        if (assignee != null) t.setAssignee(assignee);
        save();
    }

    public synchronized void delete(int id) {
        tickets.removeIf(t -> t.getId() == id);   // Streams / lambda style
        save();
    }

    // Newest ticket first
    public synchronized List<Ticket> all() {
        return tickets.stream()
                .sorted(Comparator.comparingInt(Ticket::getId).reversed())
                .collect(Collectors.toList());
    }

    // Dashboard numbers calculated with Streams
    public synchronized String statsJson() {
        Map<Ticket.Status, Long> byStatus = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
        long critical = tickets.stream()
                .filter(t -> t.getPriority() == Ticket.Priority.CRITICAL && t.getStatus() != Ticket.Status.CLOSED)
                .count();
        return "{\"total\":" + tickets.size()
                + ",\"open\":" + byStatus.getOrDefault(Ticket.Status.OPEN, 0L)
                + ",\"inProgress\":" + byStatus.getOrDefault(Ticket.Status.IN_PROGRESS, 0L)
                + ",\"resolved\":" + (byStatus.getOrDefault(Ticket.Status.RESOLVED, 0L) + byStatus.getOrDefault(Ticket.Status.CLOSED, 0L))
                + ",\"critical\":" + critical + "}";
    }

    private Ticket find(int id) {
        return tickets.stream().filter(t -> t.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    // ---------- File storage (simple CSV, no database needed) ----------
    private void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = tickets.stream().map(Ticket::toCsv).collect(Collectors.toList());
            Files.write(file, lines);
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] f = line.split(",", -1);
                for (int i = 0; i < f.length; i++) f[i] = URLDecoder.decode(f[i], StandardCharsets.UTF_8);
                Ticket t = f[0].equals("Incident")
                        ? new Incident(Integer.parseInt(f[1]), f[2], f[3], f[4], Ticket.Priority.valueOf(f[5]), f[8])
                        : new ServiceRequest(Integer.parseInt(f[1]), f[2], f[3], f[4], Ticket.Priority.valueOf(f[5]), f[8]);
                t.setStatus(Ticket.Status.valueOf(f[6]));
                t.setAssignee(f[7]);
                tickets.add(t);
                nextId = Math.max(nextId, t.getId() + 1);
            }
        } catch (Exception e) {
            System.out.println("Could not read saved tickets, starting fresh.");
        }
    }

    private void addSampleData() {
        create("Incident", "VPN not connecting from home", "Getting timeout error on GlobalProtect since morning.", "Neha Sharma", Ticket.Priority.HIGH);
        create("Request", "Install Docker Desktop", "Need Docker on my laptop for the new project.", "Rohit Verma", Ticket.Priority.LOW);
        create("Incident", "Production database is unreachable", "Payments service cannot connect to the DB.", "Ankit Rawat", Ticket.Priority.CRITICAL);
        create("Request", "New joiner laptop setup", "Laptop and email access for a new team member.", "HR Team", Ticket.Priority.MEDIUM);
        create("Incident", "Printer on 3rd floor offline", "Nobody can print since yesterday.", "Simran Kaur", Ticket.Priority.MEDIUM);
        update(3, Ticket.Status.IN_PROGRESS, "Aditya");
        update(5, Ticket.Status.RESOLVED, "Karan");
    }
}
