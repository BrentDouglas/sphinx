package io.machinecode.sphinx.example.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Entity
@Table(schema = "public", name = "coordinate")
@NamedQueries(value = {
        @NamedQuery(name = "Coordinate.withinBounds", query ="select c from Coordinate c where c.latitude <= :topLeftLat and c.latitude >= :bottomRightLat and c.longitude <= :topLeftLng and c.longitude >= :bottomRightLng")
})
public class Coordinate {

    private Integer id;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Date timestamp;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @Column(name = "longitude", nullable = false, precision=1000, scale=0)
    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(final BigDecimal longitude) {
        this.longitude = longitude;
    }

    @Column(name = "latitude", nullable = false, precision=1000, scale=0)
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(final BigDecimal latitude) {
        this.latitude = latitude;
    }

    @Temporal(TIMESTAMP)
    @Column(name="timestamp", nullable=false, length=29)
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }
}
