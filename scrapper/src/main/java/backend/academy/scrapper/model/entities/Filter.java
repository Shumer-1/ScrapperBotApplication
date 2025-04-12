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
@Table(name = "filter")
public class Filter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filter", nullable = false)
    @NotNull
    private String filter;

    @ManyToMany(mappedBy = "filters")
    @NotNull
    private Set<Link> trackingLinks = new HashSet<>();

    ;

    public Filter() {}

    public Set<Link> getTrackingLinks() {
        return trackingLinks;
    }

    public void setTrackingLinks(Set<Link> trackingLinks) {
        this.trackingLinks = trackingLinks;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Filter(String filter) {
        this.filter = filter;
    }
}
