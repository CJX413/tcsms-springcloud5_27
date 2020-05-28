package com.tcsms.business.Entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Data
@Entity
public class Environment {
    @Basic
    @Column(name = "invitationCode")
    private String invitationCode;
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "centerLat")
    private Double centerLat;
    @Basic
    @Column(name = "centerLng")
    private Double centerLng;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Environment that = (Environment) o;
        return id == that.id &&
                Objects.equals(invitationCode, that.invitationCode) &&
                Objects.equals(centerLat, that.centerLat) &&
                Objects.equals(centerLng, that.centerLng);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invitationCode, id, centerLat, centerLng);
    }

    @Override
    public String toString() {
        return "{" +
                "\"invitationCode\":" + "\"" + invitationCode + "\"" + "," +
                "\"centerLat\":" + centerLat + "," +
                "\"centerLng\":" + centerLng +
                "}";
    }
}
