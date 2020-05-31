package com.tcsms.securityserver.Monitor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.securityserver.Config.ExceptionInfo;
import com.tcsms.securityserver.Config.WarningInfo;
import com.tcsms.securityserver.Entity.BuildingRegistry;
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
public class BuildingCollisionMonitor extends TcsmsMonitor {
    private final static String THREAD_PREFIX = "建筑碰撞监听器-";

    private final static int SAFE_DISTANCE = 2;//塔吊的安全距离

    private DeviceRegistry device;
    private BuildingRegistry building;

    private OperationLog operationLog;


    private Double distance;

    private Double bigHeight;
    private Double bigLength;

    private Double height;
    private Double length;

    private Gson gson = new Gson();

    public BuildingCollisionMonitor(DeviceRegistry device, BuildingRegistry building) {
        super(THREAD_PREFIX + device.getDeviceId() + "-" + building.getBuildingId());
        this.device = device;
        this.building = building;
        this.bigHeight = device.getBigHeight();
        this.bigLength = device.getBigLength();
        this.height = building.getHeight();
        this.length = building.getLength();
        this.distance = getDistance();

    }

    private double getDistance() {
        GlobalCoordinates source = new GlobalCoordinates(device.getLatitude(), device.getLongitude());
        GlobalCoordinates target = new GlobalCoordinates(building.getLatitude(), building.getLongitude());
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        return geodeticCalculator.calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
    }

    private double getDistance(double[] sourcePoint, double[] targetPoint) {
        GlobalCoordinates source = new GlobalCoordinates(sourcePoint[0], sourcePoint[1]);
        GlobalCoordinates target = new GlobalCoordinates(targetPoint[0], targetPoint[1]);
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        return geodeticCalculator.calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
    }


    public boolean isCompleteSafe() {
        if (bigLength + length < distance) {
            return true;
        }
        return false;
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
        double[] pointOne = formRadiusToCoordinate(operationLog.getAngle(),
                operationLog.getLongitude(), operationLog.getLatitude(),
                operationLog.getRadius());

        double[] Ob = new double[]{building.getLatitude(), building.getLongitude()};
        double[] Od = new double[]{device.getLatitude(), device.getLongitude()};

        double p1toOb = getDistance(pointOne, Ob);

        double safe_length = length + SAFE_DISTANCE;

        if (p1toOb < safe_length && bigHeight < height) {
            warning.add(WarningInfo.BUILDING_COLLISION_YELLOW_WARNING);
        } else {
            double lineToOb = getDistanceOfLineToOb(pointOne, Od, Ob);
            if (lineToOb < safe_length && bigHeight < height) {
                warning.add(WarningInfo.BUILDING_COLLISION_YELLOW_WARNING);
            }
        }
        if (p1toOb < length && bigHeight < height) {
            warning.add(WarningInfo.BUILDING_COLLISION_RED_WARNING);
        } else {
            double lineToOb = getDistanceOfLineToOb(pointOne, Od, Ob);
            if (lineToOb < length && bigHeight < height) {
                warning.add(WarningInfo.BUILDING_COLLISION_RED_WARNING);
            }
        }
        return warning;
    }

    /**
     * 点C到直线AB的距离
     *
     * @param PA
     * @param PB
     * @param PC
     * @return
     */
    private double getDistanceOfLineToOb(double[] PA, double[] PB, double[] PC) {
        double a, b, c;
        a = getDistance(PA, PB);//经纬坐标系中求两点的距离公式
        b = getDistance(PB, PC);//经纬坐标系中求两点的距离公式
        c = getDistance(PA, PC);//经纬坐标系中求两点的距离公式
        double l = (a + b + c) / 2;     //周长的一半
        double s = Math.sqrt(l * (l - a) * (l - b) * (l - c));  //海伦公式求面积
        return 2 * s / a;
    }


    @Override
    public void run() {
        redisServiceImp = SpringUtil.getBean(RedisServiceImp.class);
        restTemplateServiceImp = SpringUtil.getBean(RestTemplateServiceImp.class);
        Jedis jedis = redisServiceImp.getJedis();
        try {
            while (!Thread.interrupted()) {
                isWait();
                log.info(device.getDeviceId() + "正在运行--------------");
                String value = jedis.get(this.device.getDeviceId());
                if (value != null) {
                    operationLog = gson.fromJson(value, OperationLog.class);
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
        if (operationLog != null) {
            data.add(new Gson().fromJson(operationLog.toString(), JsonObject.class));
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
        if (operationLog.hashCode() != getLastOperationLogHashCode()) {
            setLastOperationLogHashCode(operationLog.hashCode());
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
        String value = redisServiceImp.get(device.getDeviceId());
        if (value != null) {
            OperationLog operationLog = gson.fromJson(value, OperationLog.class);
            hashCode = operationLog.hashCode();
        }
        return hashCode != getLastOperationLogHashCode();
    }

}

