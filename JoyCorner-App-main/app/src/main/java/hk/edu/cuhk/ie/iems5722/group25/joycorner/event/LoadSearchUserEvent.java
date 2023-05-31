package hk.edu.cuhk.ie.iems5722.group25.joycorner.event;

import java.util.List;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.User;

public class LoadSearchUserEvent {
    private List<User> people;

    public LoadSearchUserEvent(List<User> people) {
        this.people = people;
    }

    public List<User> getPeople() {
        return people;
    }
}
