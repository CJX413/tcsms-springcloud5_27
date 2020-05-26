package com.tcsms.business.Entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "online_log", schema = "tcsmsdb")
public class OnlineLog {

    @Column(name = "deviceId")
    private String deviceId;
    @Column(name = "startTime")
    private String startTime;
    @Column(name = "endTime")
    private String endTime;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version")
    private Integer version;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlineLog that = (OnlineLog) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, startTime, endTime, version);
    }
}
