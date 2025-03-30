package backend.academy.bot.states;

import java.util.HashSet;
import java.util.Set;

public class TrackCommandState {
    public enum Step {
        WAITING_FOR_TAGS,
        WAITING_FOR_FILTERS,
        COMPLETED
    }

    private final String link;
    private Set<String> tags = new HashSet<>();
    private Set<String> filters = new HashSet<>();
    private Step step;

    public TrackCommandState(String link) {
        this.link = link;
        this.step = Step.WAITING_FOR_TAGS;
    }

    public String getLink() {
        return link;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getFilters() {
        return filters;
    }

    public void setFilters(Set<String> filters) {
        this.filters = filters;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Step getStep() {
        return step;
    }

    public void nextStep() {
        if (step == Step.WAITING_FOR_TAGS) {
            step = Step.WAITING_FOR_FILTERS;
        } else if (step == Step.WAITING_FOR_FILTERS) {
            step = Step.COMPLETED;
        }
    }
}
