// Incident = something is broken, so the SLA is tighter (Inheritance)
public class Incident extends Ticket {
    public Incident(int id, String title, String description, String requester, Priority priority, String createdAt) {
        super(id, title, description, requester, priority, createdAt);
    }

    public String getType() { return "Incident"; }

    public int getSlaHours() {
        switch (priority) {
            case CRITICAL: return 4;
            case HIGH: return 8;
            case MEDIUM: return 24;
            default: return 48;
        }
    }
}
