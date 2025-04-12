package backend.academy.scrapper.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", nullable = false)
    @NotNull
    private String tag;

    @ManyToMany(mappedBy = "tags")
    @NotNull
    private Set<Link> trackingLinks = new HashSet<>();

    ;

    public Tag() {}

    public Tag(String tag) {
        this.tag = tag;
        this.trackingLinks = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Set<Link> getTrackingLinks() {
        return trackingLinks;
    }

    public void setTrackingLinks(Set<Link> trackingLinks) {
        this.trackingLinks = trackingLinks;
    }

    public Tag(String tag, Set<Link> trackingLinks) {
        this.tag = tag;
        this.trackingLinks = trackingLinks;
    }
}
