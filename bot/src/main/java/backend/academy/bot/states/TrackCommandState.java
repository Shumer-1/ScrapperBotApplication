package backend.academy.bot.states;

import java.util.ArrayList;
import java.util.List;

public class TrackCommandState {
    public enum Step {
        WAITING_FOR_TAGS,
        WAITING_FOR_FILTERS,
        COMPLETED
    }

    private final String link;
    private List<String> tags = new ArrayList<>();
    private List<String> filters = new ArrayList<>();
    private Step step;

    public TrackCommandState(String link) {
        this.link = link;
        this.step = Step.WAITING_FOR_TAGS;
    }

    public String getLink() {
        return link;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public Step getStep() {
        return step;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public void nextStep() {
        if (step == Step.WAITING_FOR_TAGS) {
            step = Step.WAITING_FOR_FILTERS;
        } else if (step == Step.WAITING_FOR_FILTERS) {
            step = Step.COMPLETED;
        }
    }
}
