package com.tcsms.securityserver.Entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Data
@Entity
@Table(name = "warning_ranking", schema = "tcsmsdb")
public class WarningRanking {
    @Id
    @Column(name = "deviceId")
    private String deviceId;

    @Column(name = "redWarning")
    private Integer redWarning = 0;

    @Column(name = "yellowWarning")
    private Integer yellowWarning = 0;

    @Column(name = "rankingCount")
    private Integer rankingCount = 0;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarningRanking that = (WarningRanking) o;
        return redWarning == that.redWarning &&
                yellowWarning == that.yellowWarning &&
                rankingCount == that.rankingCount &&
                Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, redWarning, yellowWarning, rankingCount);
    }
}
