package io.machinecode.sphinx.example.core.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Entity
@Table(schema = "public", name = "trip")
@NamedQueries(value = {
        @NamedQuery(name = "Trip.withinBounds", query =
                "select t from Trip t join t.legs l where " +
                    "((l.start.latitude <= :topLeftLat " +
                        "and l.start.latitude >= :bottomRightLat " +
                        "and l.start.longitude <= :topLeftLng " +
                        "and l.start.longitude >= :bottomRightLng " +
                        "and l.start.timestamp >= :startTime " +
                        "and l.start.timestamp <= :endTime) " +
                "or " +
                    "(l.end.latitude <= :topLeftLat " +
                    "and l.end.latitude >= :bottomRightLat " +
                    "and l.end.longitude <= :topLeftLng " +
                    "and l.end.longitude >= :bottomRightLng " +
                    "and l.end.timestamp >= :startTime " +
                    "and l.end.timestamp <= :endTime))")
})
public class Trip  {

    private Integer id;
    private String title;
    private String description;
    private BigDecimal distance;
    private List<Leg> legs = new ArrayList<Leg>();


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @Column(name = "title", nullable = false, unique = false, updatable = true)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "description", nullable = false, unique = false, updatable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("id desc")
    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(final List<Leg> legs) {
        this.legs = legs;
    }
}
