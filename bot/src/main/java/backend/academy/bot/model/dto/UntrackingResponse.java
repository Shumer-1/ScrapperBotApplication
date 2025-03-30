package backend.academy.bot.model.dto;

public class UntrackingResponse {
    private boolean removed;
    private String message;

    public UntrackingResponse() {}

    public UntrackingResponse(boolean removed, String message) {
        this.removed = removed;
        this.message = message;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
