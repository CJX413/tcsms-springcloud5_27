package com.tcsms.securityserver.Service.ServiceImp;

import com.tcsms.securityserver.Dao.OnlineLogDao;
import com.tcsms.securityserver.Entity.OnlineLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class OnlineLogServiceImp {
    @Autowired
    private OnlineLogDao runningLogDao;
    @Autowired
    RedisServiceImp redisServiceImp;

    public OnlineLogDao getDao() {
        return runningLogDao;
    }

    public void startRunning(String deviceId) throws Exception {
        OnlineLog latestVersion = runningLogDao.findByLatestVersionAndDeviceId(deviceId);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        if (latestVersion == null) {
            OnlineLog onlineLog = new OnlineLog();
            onlineLog.setDeviceId(deviceId);
            onlineLog.setStartTime(date);
            runningLogDao.save(onlineLog);
            return;
        }
        if (latestVersion.getEndTime() == null) {
            latestVersion.setStartTime(date);
            runningLogDao.save(latestVersion);
        } else {
            OnlineLog onlineLog = new OnlineLog();
            onlineLog.setDeviceId(deviceId);
            onlineLog.setStartTime(date);
            runningLogDao.save(onlineLog);
        }
    }

    public void stopRunning(String deviceId) throws Exception {

        redisServiceImp.delete(deviceId);
        OnlineLog onlineLog = runningLogDao.findByLatestVersionAndDeviceId(deviceId);
        if (onlineLog == null) {
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        onlineLog.setEndTime(date);
        runningLogDao.save(onlineLog);
    }
}
