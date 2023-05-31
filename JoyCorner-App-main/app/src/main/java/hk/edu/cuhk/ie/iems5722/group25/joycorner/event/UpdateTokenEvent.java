package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

public class UpdateTokenEvent {
    private String token;

    public UpdateTokenEvent(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
