package com.tcsms.securityserver.Monitor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.securityserver.Config.ExceptionInfo;
import com.tcsms.securityserver.Config.WarningInfo;
import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Entity.OperationLog;
import com.tcsms.securityserver.Service.ServiceImp.RedisServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.RestTemplateServiceImp;
import com.tcsms.securityserver.Utils.SpringUtil;
import lombok.extern.log4j.Log4j2;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;


@Log4j2
public class DeviceCollisionMonitor extends TcsmsMonitor {
    private final static String THREAD_PREFIX = "设备碰撞监听器-";

    private final static int SAFE_DISTANCE = 2;//塔吊的安全距离

    private DeviceRegistry device_1;
    private DeviceRegistry device_2;

    private OperationLog operationLog_device_1;
    private OperationLog operationLog_device_2;


    private Double distance;
    private Double bearing;

    private Double bigHeight_1;
    private Double bigLength_1;

    private Double bigHeight_2;
    private Double bigLength_2;

    private Double safe_bigLength_1;
    private Double safe_bigLength_2;

    private Gson gson = new Gson();

    public DeviceCollisionMonitor(DeviceRegistry device_1, DeviceRegistry device_2) {
        super(THREAD_PREFIX + device_1.getDeviceId() + "-" + device_2.getDeviceId());
        this.device_1 = device_1;
        this.device_2 = device_2;
        this.bigHeight_1 = device_1.getBigHeight();
        this.bigLength_1 = device_1.getBigLength();
        this.bigHeight_2 = device_2.getBigHeight();
        this.bigLength_2 = device_2.getBigLength();
        this.safe_bigLength_1 = bigLength_1 + SAFE_DISTANCE;
        this.safe_bigLength_2 = bigLength_2 + SAFE_DISTANCE;
        this.distance = getDistance();
        this.bearing = getBearing();

    }

    public double getDistance() {
        GlobalCoordinates source = new GlobalCoordinates(device_1.getLatitude(), device_1.getLongitude());
        GlobalCoordinates target = new GlobalCoordinates(device_2.getLatitude(), device_2.getLongitude());
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        return geodeticCalculator.calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
    }

    private double getDistance(double[] sourcePoint, double[] targetPoint) {
        GlobalCoordinates source = new GlobalCoordinates(sourcePoint[0], sourcePoint[1]);
        GlobalCoordinates target = new GlobalCoordinates(targetPoint[0], targetPoint[2]);
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        return geodeticCalculator.calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
    }

    /**
     * 以北为0度顺时针为正方向
     *
     * @return
     */
    public double getBearing() {
        double longitudeFrom = device_1.getLongitude();
        double longitudeTo = device_2.getLongitude();
        double latitudeFrom = Math.toRadians(device_1.getLatitude());
        double latitudeTo = Math.toRadians(device_2.getLatitude());
        double longDiff = Math.toRadians(longitudeTo - longitudeFrom);
        double y = Math.sin(longDiff) * Math.cos(latitudeTo);
        double x = Math.cos(latitudeFrom) * Math.sin(latitudeTo) - Math.sin(latitudeFrom) * Math.cos(latitudeTo) * Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }


    /**
     * 0:为Latitude(纬度)
     * 1:为Longitude(经度)
     *
     * @param angle
     * @param longitude
     * @param latitude
     * @param radius
     * @return
     */
    public double[] formRadiusToCoordinate(double angle, double longitude, double latitude, double radius) {
        //将距离转换成经纬度的计算公式
        double P = radius / 6371e3;
        // 转换为radian，否则结果会不正确
        angle = Math.toRadians(angle);
        longitude = Math.toRadians(longitude);
        latitude = Math.toRadians(latitude);

        double latPoint = Math.asin(Math.sin(latitude) * Math.cos(P) + Math.cos(latitude) * Math.sin(P) * Math.cos(angle));
        double lonPoint = longitude + Math.atan2(Math.sin(angle) * Math.sin(P) * Math.cos(latitude), Math.cos(P) - Math.sin(latitude) * Math.sin(latPoint));
        // 转为正常的10进制经纬度

        lonPoint = Math.toDegrees(lonPoint);
        latPoint = Math.toDegrees(latPoint);
        double[] point = new double[2];
        point[0] = latPoint;
        point[1] = lonPoint;
        return point;
    }

    @Override
    List<WarningInfo> isWarning() {
        List<WarningInfo> warning = new ArrayList<>();
        double[] pointOne = formRadiusToCoordinate(operationLog_device_1.getAngle(),
                operationLog_device_1.getLongitude(), operationLog_device_1.getLatitude(),
                operationLog_device_1.getRadius());
        double[] pointTwo = formRadiusToCoordinate(operationLog_device_2.getAngle(),
                operationLog_device_2.getLongitude(), operationLog_device_2.getLatitude(),
                operationLog_device_2.getRadius());
        double[] O1 = new double[]{operationLog_device_1.getLatitude(), operationLog_device_1.getLongitude()};
        double[] O2 = new double[]{operationLog_device_2.getLatitude(), operationLog_device_2.getLongitude()};
        double p1toO2 = getDistance(pointOne, O2);
        double p2toO1 = getDistance(pointTwo, O1);

        if (p1toO2 < safe_bigLength_2 && p2toO1 < safe_bigLength_1) {
            warning.add(WarningInfo.DEVICE_COLLISION_YELLOW_WARNING);
        } else if ((p1toO2 < safe_bigLength_2 && p2toO1 > safe_bigLength_1) && (bigHeight_1 > bigHeight_2)) {
            warning.add(WarningInfo.DEVICE_COLLISION_YELLOW_WARNING);
        } else if ((p1toO2 > safe_bigLength_2 && p2toO1 < safe_bigLength_1) && (bigHeight_1 < bigHeight_2)) {
            warning.add(WarningInfo.DEVICE_COLLISION_YELLOW_WARNING);
        }
        if (p1toO2 < bigLength_2 && p2toO1 < bigLength_1) {
            warning.add(WarningInfo.DEVICE_COLLISION_RED_WARNING);
        } else if ((p1toO2 < bigLength_2 && p2toO1 > bigLength_1) && (bigHeight_1 > bigHeight_2)) {
            warning.add(WarningInfo.DEVICE_COLLISION_RED_WARNING);
        } else if ((p1toO2 > bigLength_2 && p2toO1 < bigLength_1) && (bigHeight_1 < bigHeight_2)) {
            warning.add(WarningInfo.DEVICE_COLLISION_RED_WARNING);
        }
        return warning;
    }



    @Override
    public void run() {
        redisServiceImp = SpringUtil.getBean(RedisServiceImp.class);
        restTemplateServiceImp = SpringUtil.getBean(RestTemplateServiceImp.class);
        Jedis jedis = redisServiceImp.getJedis();
        try {
            while (!Thread.interrupted()) {
                isWait();
                log.info(device_1.getDeviceId() + device_2.getDeviceId() + "正在运行--------------");
                String value1 = jedis.get(this.device_1.getDeviceId());
                String value2 = jedis.get(this.device_2.getDeviceId());
                if (value1 != null && value2 != null) {
                    operationLog_device_1 = gson.fromJson(value1, OperationLog.class);
                    operationLog_device_2 = gson.fromJson(value2, OperationLog.class);
                    if (isRunning()) {
                        setNotRunningTimes(0);
                        List<WarningInfo> warningInfos = isWarning();
                        if (!warningInfos.isEmpty()) {
                            for (WarningInfo warningInfo : warningInfos) {
                                log.info("发送警报----------------");
                                sendWarning(warningInfo, getData());
                            }
                        }
                    } else {
                        int newNotRunningTimes = getNotRunningTimes() + 1;
                        setNotRunningTimes(newNotRunningTimes);
                    }
                } else {
                    setLastOperationLogHashCode(0);
                    int newNotRunningTimes = getNotRunningTimes() + 1;
                    setNotRunningTimes(newNotRunningTimes);
                }
                Thread.sleep(SLEEP_TIME);
            }
        } catch (InterruptedException e) {
            sendException(ExceptionInfo.DEVICE_COLLISION_MONITOR_STOP, null);
        } finally {
            jedis.close();
        }
    }

    @Override
    JsonArray getData() {
        JsonArray data = new JsonArray();
        if (operationLog_device_1 != null && operationLog_device_2 != null) {
            data.add(new Gson().fromJson(operationLog_device_1.toString(), JsonObject.class));
            data.add(new Gson().fromJson(operationLog_device_2.toString(), JsonObject.class));
        }
        return data;
    }

    /**
     * 用于运行时线程内部判断是否在运行
     *
     * @return
     */
    @Override
    public boolean isRunning() {
        if ((operationLog_device_1.hashCode() + operationLog_device_2.hashCode()) != getLastOperationLogHashCode()) {
            setLastOperationLogHashCode(operationLog_device_1.hashCode() + operationLog_device_2.hashCode());
            return true;
        }
        return false;
    }

    /**
     * 用于暂停时线程外部判断是否在运行
     *
     * @param
     * @return
     */
    @Override
    public boolean isRunningWhenPause() {

        int hashCode = 0;
        String value1 = redisServiceImp.get(device_1.getDeviceId());
        String value2 = redisServiceImp.get(device_2.getDeviceId());
        if (value1 != null && value2 != null) {
            OperationLog operationLog1 = gson.fromJson(value1, OperationLog.class);
            OperationLog operationLog2 = gson.fromJson(value2, OperationLog.class);
            hashCode = operationLog1.hashCode() + operationLog2.hashCode();
        }
        return hashCode != getLastOperationLogHashCode();
    }

}
