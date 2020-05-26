package com.tcsms.securityserver.Dao;

import com.tcsms.securityserver.Entity.WarningRanking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;

public interface WarningRankingDao extends JpaRepository<WarningRanking, String>, JpaSpecificationExecutor<WarningRanking>, Serializable {
    @Query(value = "update warning_ranking set redWarning=redWarning+1 and  warningCount=warningCount+2 where deviceId=#{deviceId}}", nativeQuery = true)
    void addRedWarningById(@Param("deviceId") String deviceId);

    @Query(value = "update warning_ranking set yellowWarning=yellowWarning+1 and warningCount=warningCount+1 where deviceId=#{deviceId}}", nativeQuery = true)
    void addYellowWarningById(@Param("deviceId") String deviceId);
}
