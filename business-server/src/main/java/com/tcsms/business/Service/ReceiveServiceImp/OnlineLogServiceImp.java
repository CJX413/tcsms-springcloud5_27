package com.tcsms.business.Service.ReceiveServiceImp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.business.Dao.DeviceRegistryDao;
import com.tcsms.business.Dao.OnlineLogDao;
import com.tcsms.business.Entity.DeviceRegistry;
import com.tcsms.business.Entity.OnlineLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OnlineLogServiceImp {
    @Autowired
    private OnlineLogDao runningLogDao;
    @Autowired
    private DeviceRegistryDao deviceRegistryDao;

    public OnlineLogDao getDao() {
        return runningLogDao;
    }

    public JsonArray queryAllLatestVersionRunningLog() throws Exception {
        List<DeviceRegistry> list = deviceRegistryDao.findByIsRegistered(true);
        JsonArray jsonArray = new JsonArray();
        for (DeviceRegistry device : list) {
            OnlineLog onlineLog = runningLogDao.findByLatestVersionAndDeviceId(device.getDeviceId());
            if (onlineLog != null) {
                jsonArray.add(formToJson(device, onlineLog));
            }
        }
        return jsonArray;
    }

    private JsonObject formToJson(DeviceRegistry device, OnlineLog onlineLog) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("deviceId", device.getDeviceId());
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

}
