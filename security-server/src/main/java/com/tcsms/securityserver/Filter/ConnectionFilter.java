package com.tcsms.securityserver.Filter;

import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Service.ServiceImp.DeviceRegistryServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.OnlineLogServiceImp;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@Log4j2
@WebFilter(filterName = "ConnectionFilter", urlPatterns = {"/startRunning", "/stopRunning"})
public class ConnectionFilter implements Filter {
    @Autowired
    private DeviceRegistryServiceImp deviceRegistryServiceImp;
    @Autowired
    private OnlineLogServiceImp onlineLogServiceImp;
    private static ConcurrentHashMap<String, Boolean> deviceRegistryHashMap = new ConcurrentHashMap<>();
    private static CopyOnWriteArraySet<String> connectSet = new CopyOnWriteArraySet<>();

    public static void updateDeviceRegistryHashMap(DeviceRegistry device) throws RuntimeException {
        log.info("updateDeviceRegistryHashMap:修改了deviceRegistryHashMap的配置" + device.getDeviceId());
        deviceRegistryHashMap.put(device.getDeviceId(), device.getIsRegistered());
        log.info(deviceRegistryHashMap);
    }

    public static void deleteDeviceRegistryHashMap(DeviceRegistry device) throws RuntimeException {
        log.info("deleteDeviceRegistryHashMap:删除了deviceRegistryHashMap的配置" + device.getDeviceId());
        deviceRegistryHashMap.remove(device.getDeviceId());
        log.info(deviceRegistryHashMap);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("----------------------->ConnectionFilter过滤器被创建");
        log.info("----------------------->断开所有设备连接");
        onlineLogServiceImp.disConnectAll();
        log.info("----------------------->初始化deviceRegistryHashMap");
        List<DeviceRegistry> list = deviceRegistryServiceImp.getDao().findAll();
        for (DeviceRegistry deviceRegistry : list) {
            deviceRegistryHashMap.put(deviceRegistry.getDeviceId(), deviceRegistry.getIsRegistered());
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            String deviceId = ((HttpServletRequest) servletRequest).getHeader("deviceId");
            String deviceModel = ((HttpServletRequest) servletRequest).getHeader("deviceModel");
            String longitude = ((HttpServletRequest) servletRequest).getHeader("longitude");
            String latitude = ((HttpServletRequest) servletRequest).getHeader("deviceModel");
            log.info("deviceId:{}**deviceModel{}**longitude{}**deviceModel{}", deviceId, deviceModel, longitude, latitude);
            Boolean value = deviceRegistryHashMap.getOrDefault(deviceId, null);
            if (value != null) {
                if (value.equals(true)) {
                    log.info("deviceId:{}被放行", deviceId);
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    log.info("deviceId:{}被拒绝", deviceId);
                    disconnect(deviceId);
                }
            } else {
                log.info("deviceId:{}新设备存入数据库", deviceId);
                DeviceRegistry deviceRegistry = new DeviceRegistry();
                deviceRegistry.setDeviceModel(deviceModel);
                deviceRegistry.setIsRegistered(false);
                deviceRegistry.setDeviceId(deviceId);
                deviceRegistry.setLatitude(Double.valueOf(latitude));
                deviceRegistry.setLongitude(Double.valueOf(longitude));
                deviceRegistryServiceImp.insertNewDevice(deviceRegistry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        log.info("----------------------->ConnectionFilter过滤器被销毁");

    }

    public static void connect(String deviceId) {
        try {
            connectSet.add(deviceId);
            log.info(connectSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(String deviceId) {
        try {
            connectSet.remove(deviceId);
            log.info(connectSet);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static Boolean isConnect(String deviceId) {
        log.info(connectSet);
        return connectSet.contains(deviceId);
    }
}
