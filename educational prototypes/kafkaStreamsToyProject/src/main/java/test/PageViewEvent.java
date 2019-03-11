package test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageViewEvent {
    @JsonCreator
    public PageViewEvent(@JsonProperty("userId") String userId, @JsonProperty("page") String page, @JsonProperty("duration") long duration) {
        this.userId = userId;
        this.page = page;
        this.duration = duration;
    }
    private String userId, page;
    private long duration;

    public String getUserId() {
        return userId;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}