package jobs;

public class ArenaResult {
    public String message;
    public boolean error;

    public ArenaResult(String message, boolean error) {
        this.message = message;
        this.error = error;
    }

    public String toJson() {
        if (error) {
            return "{\"error\":true, \"message\":\""+ message + "\"}";
        }
        return "{\"error\":false, \"message\":\""+ message + "\"}";
    }
}