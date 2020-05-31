package com.tcsms.securityserver.Monitor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.tcsms.securityserver.Config.ExceptionInfo;
import com.tcsms.securityserver.Config.WarningInfo;
import com.tcsms.securityserver.Entity.BuildingRegistry;
import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Entity.WarningDetail;
import com.tcsms.securityserver.Entity.WarningLog;
import com.tcsms.securityserver.Service.ServiceImp.RedisServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.RestTemplateServiceImp;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TcsmsMonitor extends Thread {

    private String threadName;
    RestTemplateServiceImp restTemplateServiceImp;
    protected RedisServiceImp redisServiceImp;
    final static long SLEEP_TIME = 500;
    private int lastOperationLogHashCode;
    private boolean pause = false;
    private int notRunningTimes;

    TcsmsMonitor(String threadName) {
        super(threadName);
        this.threadName = threadName;
    }

    abstract List<WarningInfo> isWarning();

    abstract JsonArray getData();

    abstract boolean isRunning();

    abstract boolean isRunningWhenPause();

    void pause() {
        this.pause = true;
    }

    void awake() {
        synchronized (this) {
            this.pause = false;
            this.notify();
        }
    }

    public static boolean isCompleteSafe(DeviceRegistry device1, DeviceRegistry device2) {
        GlobalCoordinates source = new GlobalCoordinates(device1.getLatitude(), device1.getLongitude());
        GlobalCoordinates target = new GlobalCoordinates(device2.getLatitude(), device2.getLongitude());
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        double distance = geodeticCalculator.calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
        if (device1.getBigLength() + device2.getBigLength() < distance) {
            return true;
        }
        return false;
    }

    public static boolean isCompleteSafe(DeviceRegistry device, BuildingRegistry building) {
        GlobalCoordinates source = new GlobalCoordinates(device.getLatitude(), device.getLongitude());
        GlobalCoordinates target = new GlobalCoordinates(building.getLatitude(), building.getLongitude());
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        double distance = geodeticCalculator.calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
        if (device.getBigLength() + building.getLength() < distance) {
            return true;
        }
        return false;
    }


    void sendWarning(WarningInfo warningInfo, JsonArray data) {
        WarningLog warningLog = new WarningLog();
        warningLog.setCode(warningInfo.getCode());
        warningLog.setMessage(warningInfo.getMsg());
        warningLog.setTime(new Timestamp(System.currentTimeMillis()));
        List<WarningDetail> list = new ArrayList<>();
        Gson gson = new Gson();
        Optional.ofNullable(data).ifPresent(jsonArray -> {
            jsonArray.forEach(jsonElement -> {
                list.add(gson.fromJson(jsonElement.toString(), WarningDetail.class));
            });
        });
        warningLog.setWarningDetails(list);
        restTemplateServiceImp.sendWarning(warningLog);
    }

    void sendException(ExceptionInfo exceptionInfo, JsonArray data) {
        WarningLog warningLog = new WarningLog();
        warningLog.setCode(exceptionInfo.getCode());
        warningLog.setMessage(exceptionInfo.getMsg());
        warningLog.setTime(new Timestamp(System.currentTimeMillis()));
        List<WarningDetail> list = new ArrayList<>();
        Gson gson = new Gson();
        Optional.ofNullable(data).ifPresent(jsonArray -> {
            jsonArray.forEach(jsonElement -> {
                list.add(gson.fromJson(jsonElement.toString(), WarningDetail.class));
            });
        });
        warningLog.setWarningDetails(list);
        restTemplateServiceImp.sendWarning(warningLog);
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void isWait() throws InterruptedException {
        synchronized (this) {
            if (pause) {
                this.wait();
            }
        }
    }

    public boolean isPause() {
        return this.pause;
    }

    public int getNotRunningTimes() {
        return notRunningTimes;
    }

    public void setNotRunningTimes(int notRunningTimes) {
        this.notRunningTimes = notRunningTimes;
    }

    /**
     * 判断监听器的状态是否改变
     *
     * @return
     */
    @Override
    public int hashCode() {
        return (this.isInterrupted() ? 1 : 0) + (isPause() ? 1 : 0) * 2;
    }

    public int getLastOperationLogHashCode() {
        return lastOperationLogHashCode;
    }

    public void setLastOperationLogHashCode(int lastOperationLogHashCode) {
        this.lastOperationLogHashCode = lastOperationLogHashCode;
    }
}
