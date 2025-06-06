package backend.academy.bot.model.dto;

import backend.academy.bot.model.entities.User;

public class UserRegistrationRequest {
    private long userId;
    private String username;

    public UserRegistrationRequest() {}

    public UserRegistrationRequest(long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User toEntity() {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        return user;
    }
}
