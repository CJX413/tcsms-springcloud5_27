package com.tcsms.business.Dao;


import com.tcsms.business.Entity.WarningRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface WarningRankingDao extends JpaRepository<WarningRanking, String>, JpaSpecificationExecutor<WarningRanking>, Serializable {

    @Query(value = "select warning_ranking.deviceId,device_registry.deviceModel,device_registry.isRegistered,warning_ranking.yellowWarning,warning_ranking.redWarning" +
            " from device_registry inner join warning_ranking on " +
            "device_registry.deviceId = warning_ranking.deviceId order by rankingCount", nativeQuery = true)
    List<Map<String, Object>> orderByWarningCount();
}
