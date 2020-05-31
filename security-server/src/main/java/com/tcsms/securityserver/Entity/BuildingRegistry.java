package com.tcsms.securityserver.Entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@Table(name = "building_registry")
public class BuildingRegistry {
    @Id
    @Column(name = "buildingId")
    private String buildingId;
    @Basic
    @Column(name = "longitude")
    private Double longitude;
    @Basic
    @Column(name = "latitude")
    private Double latitude;
    @Basic
    @Column(name = "length")
    private double length;
    @Basic
    @Column(name = "height")
    private double height;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildingRegistry that = (BuildingRegistry) o;
        return Double.compare(that.length, length) == 0 &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Double.compare(that.height, height) == 0 &&
                Objects.equals(buildingId, that.buildingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildingId, height, longitude, latitude, length);
    }
}
