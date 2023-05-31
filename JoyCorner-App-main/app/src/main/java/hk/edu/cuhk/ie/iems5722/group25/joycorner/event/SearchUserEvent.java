package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

public class SearchUserEvent {
    private String condition;

    public SearchUserEvent(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}
