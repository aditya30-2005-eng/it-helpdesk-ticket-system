// Service Request = user asks for something new (access, software), so the SLA is relaxed
public class ServiceRequest extends Ticket {
    public ServiceRequest(int id, String title, String description, String requester, Priority priority, String createdAt) {
        super(id, title, description, requester, priority, createdAt);
    }

    public String getType() { return "Request"; }

    public int getSlaHours() {
        switch (priority) {
            case CRITICAL: return 8;
            case HIGH: return 24;
            case MEDIUM: return 48;
            default: return 72;
        }
    }
}
