package com.tcsms.securityserver.Dao;

import com.tcsms.securityserver.Entity.WarningRanking;
import org.springframework.data.annotation.Transient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

public interface WarningRankingDao extends JpaRepository<WarningRanking, String>, JpaSpecificationExecutor<WarningRanking>, Serializable {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update warning_ranking set redWarning=redWarning+1, rankingCount=rankingCount+2 where deviceId=:deviceId", nativeQuery = true)
    void addRedWarningById(@Param("deviceId") String deviceId);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update warning_ranking set yellowWarning=yellowWarning+1, rankingCount=rankingCount+1 where deviceId=:deviceId", nativeQuery = true)
    void addYellowWarningById(@Param("deviceId") String deviceId);
}
