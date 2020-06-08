package com.tcsms.business.Service.ReceiveServiceImp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.business.Dao.DeviceRegistryDao;
import com.tcsms.business.Dao.OnlineLogDao;
import com.tcsms.business.Entity.DeviceRegistry;
import com.tcsms.business.Entity.OnlineLog;
import com.tcsms.business.Exception.CustomizeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OnlineLogServiceImp {
    @Autowired
    private OnlineLogDao onlineLogDao;
    @Autowired
    private DeviceRegistryDao deviceRegistryDao;

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

    public boolean isOnline(String deviceId) {
        OnlineLog onlineLog = onlineLogDao.findByLatestVersionAndDeviceId(deviceId);
        if (onlineLog == null) {
            return false;
        }
        if (onlineLog.getEndTime() == null) {
            return true;
        }
        return false;
    }

    public JsonObject runningLogBar(String date) throws Exception {
        JsonObject jsonObject = new JsonObject();
        List<OnlineLog> list = onlineLogDao.runningLogBar(date);
        if (list == null || list.size() == 0) {
            throw new CustomizeException("没有数据！");
        }
        JsonArray startTimeArray = new JsonArray();
        JsonArray endTimeArray = new JsonArray();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (OnlineLog onlineLog : list) {
            Date startTime = simpleDateFormat.parse(onlineLog.getStartTime());
            Date endTime = simpleDateFormat.parse(onlineLog.getEndTime());
            Long time1 = startTime.getTime();
            Long time2 = endTime.getTime() - startTime.getTime();
            startTimeArray.add(time1);
            endTimeArray.add(time2);
        }
        jsonObject.add("startTime", startTimeArray);
        jsonObject.add("endTime", endTimeArray);
        return jsonObject;
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

}
