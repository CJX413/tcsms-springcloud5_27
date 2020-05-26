package com.tcsms.securityserver.Service.ServiceImp;

import com.tcsms.securityserver.Dao.WarningRankingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarningRankingServiceImp {
    @Autowired
    private WarningRankingDao warningRankingDao;
    private static final String warningRankingLock = " warningRankingLock";

    public void addRedWarningById(String deviceId) {
        synchronized (warningRankingLock) {
            warningRankingDao.addRedWarningById(deviceId);
        }
    }

    public void addYellowWarningById(String deviceId) {
        synchronized (warningRankingLock) {
            warningRankingDao.addYellowWarningById(deviceId);
        }
    }
}
