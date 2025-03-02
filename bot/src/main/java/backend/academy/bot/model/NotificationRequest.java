package backend.academy.bot.model;

public class NotificationRequest {
    private String message;
    private long userId;

    public NotificationRequest() {
    }

    public NotificationRequest(String message) {
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
