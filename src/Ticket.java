// Abstract base class: common data + behaviour of every ticket (Abstraction)
public abstract class Ticket {
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    protected int id;
    protected String title, description, requester, createdAt;
    protected String assignee = "Unassigned";
    protected Priority priority;
    protected Status status = Status.OPEN;

    public Ticket(int id, String title, String description, String requester, Priority priority, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requester = requester;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    // Each child class decides its own type and SLA (Polymorphism)
    public abstract String getType();
    public abstract int getSlaHours();

    public int getId() { return id; }
    public Status getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public void setStatus(Status status) { this.status = status; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    // Converts ticket to a CSV line for saving to file
    public String toCsv() {
        String[] f = { getType(), String.valueOf(id), title, description, requester,
                       priority.name(), status.name(), assignee, createdAt };
        StringBuilder sb = new StringBuilder();
        for (String s : f) {
            if (sb.length() > 0) sb.append(",");
            sb.append(java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    // Converts ticket to JSON for the website
    public String toJson() {
        return "{\"id\":" + id + ",\"type\":\"" + getType() + "\",\"title\":\"" + esc(title)
             + "\",\"description\":\"" + esc(description) + "\",\"requester\":\"" + esc(requester)
             + "\",\"priority\":\"" + priority + "\",\"status\":\"" + status
             + "\",\"assignee\":\"" + esc(assignee) + "\",\"createdAt\":\"" + createdAt
             + "\",\"slaHours\":" + getSlaHours() + "}";
    }

    public static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
