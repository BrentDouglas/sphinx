package io.machinecode.sphinx.example.core.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Entity
@Table(schema = "public", name = "leg")
public class Leg {

    private Integer id;
    private Coordinate start;
    private Coordinate end;
    private Trip trip;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Coordinate.class)
    @JoinColumn(name = "start_coordinate_id", nullable = false)
    public Coordinate getStart() {
        return start;
    }

    public void setStart(final Coordinate start) {
        this.start = start;
    }

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Coordinate.class)
    @JoinColumn(name = "end_coordinate_id", nullable = false)
    public Coordinate getEnd() {
        return end;
    }

    public void setEnd(final Coordinate end) {
        this.end = end;
    }

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = Trip.class)
    @JoinColumn(name = "trip_id", nullable = false)
    public Trip getTrip() {
        return trip;
    }

    public void setTrip(final Trip trip) {
        this.trip = trip;
    }
}
