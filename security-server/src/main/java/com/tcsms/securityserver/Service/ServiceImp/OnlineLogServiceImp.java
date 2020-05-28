package com.tcsms.securityserver.Service.ServiceImp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.securityserver.Dao.DeviceRegistryDao;
import com.tcsms.securityserver.Dao.OnlineLogDao;
import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Entity.OnlineLog;
import com.tcsms.securityserver.Filter.ConnectionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OnlineLogServiceImp {
    @Autowired
    private OnlineLogDao onlineLogDao;
    @Autowired
    private DeviceRegistryDao deviceRegistryDao;
    @Autowired
    RedisServiceImp redisServiceImp;

    public OnlineLogDao getDao() {
        return onlineLogDao;
    }

    public JsonArray onlineLog() throws Exception {
        List<OnlineLog> list = onlineLogDao.findAllLatestVersion();
        JsonArray jsonArray = new JsonArray();
        for (OnlineLog onlineLog : list) {
            DeviceRegistry device = deviceRegistryDao.findById(onlineLog.getDeviceId()).orElse(new DeviceRegistry());
            jsonArray.add(formToJson(device, onlineLog));
        }
        return jsonArray;
    }

    private JsonObject formToJson(DeviceRegistry device, OnlineLog onlineLog) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("deviceId", onlineLog.getDeviceId());
        jsonObject.addProperty("deviceModel", device.getDeviceModel());
        if (onlineLog.getEndTime() == null || "".equals(onlineLog.getEndTime())) {
            jsonObject.addProperty("startTime", onlineLog.getStartTime());
        } else {
            jsonObject.addProperty("startTime", "未上线");
        }
        JsonObject coordinate = new JsonObject();
        coordinate.addProperty("lat", device.getLatitude());
        coordinate.addProperty("lng", device.getLongitude());
        jsonObject.add("coordinate", coordinate);
        jsonObject.addProperty("address", "");
        return jsonObject;
    }

    public JsonArray findAllLatestVersionJoinDeviceRegistry() {
        List<Map<String, Object>> list = onlineLogDao.findAllLatestVersionJoinDeviceRegistry();
        JsonArray jsonArray = new JsonArray();
        for (Map<String, Object> map : list) {
            map.forEach((k, v) -> {
                JsonObject jsonObject = new JsonObject();
                if (v instanceof String) {
                    jsonObject.addProperty(k, (String) v);
                } else if (v instanceof Boolean) {
                    jsonObject.addProperty(k, (Boolean) v);
                } else if (v instanceof Double) {
                    jsonObject.addProperty(k, (Double) v);
                } else {
                    jsonObject.add(k, null);
                }
                jsonArray.add(jsonObject);
            });
        }
        return jsonArray;
    }

    public void connectOne(String deviceId, String date) throws Exception {
        ConnectionFilter.connect(deviceId);
        OnlineLog latestVersion = onlineLogDao.findByLatestVersionAndDeviceId(deviceId);
        if (latestVersion == null) {
            OnlineLog onlineLog = new OnlineLog();
            onlineLog.setDeviceId(deviceId);
            onlineLog.setStartTime(date);
            onlineLogDao.save(onlineLog);
            return;
        }
        if (latestVersion.getEndTime() == null) {
            latestVersion.setStartTime(date);
            onlineLogDao.save(latestVersion);
        } else {
            OnlineLog onlineLog = new OnlineLog();
            onlineLog.setDeviceId(deviceId);
            onlineLog.setStartTime(date);
            onlineLogDao.save(onlineLog);
        }
    }

    public void disConnectAll() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        onlineLogDao.findAllLatestVersion().forEach(onlineLog -> {
            if (onlineLog.getEndTime() == null) {
                onlineLog.setEndTime(simpleDateFormat.format(date));
                onlineLogDao.save(onlineLog);
            }
        });


    }

    public void disconnectOne(String deviceId, String date) throws Exception {
        ConnectionFilter.disconnect(deviceId);
        OnlineLog onlineLog = onlineLogDao.findByLatestVersionAndDeviceId(deviceId);
        if (onlineLog == null) {
            return;
        }
        onlineLog.setEndTime(date);
        onlineLogDao.save(onlineLog);
    }
}
