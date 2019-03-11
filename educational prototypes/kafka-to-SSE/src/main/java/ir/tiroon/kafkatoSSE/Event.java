package ir.tiroon.kafkatoSSE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Event {
    int x, y;
    LocalDateTime time;

    @JsonCreator
    public Event(@JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("time") LocalDateTime time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public Event(int x, int y) {
        this.x = x;
        this.y = y;
        this.time = LocalDateTime.now();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "ClickEvent{" +
                "x=" + x +
                ", y=" + y +
                ", time=" + time +
                '}';
    }
}
