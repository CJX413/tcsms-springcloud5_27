package com.tcsms.securityserver.Filter;

import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Service.ServiceImp.DeviceRegistryServiceImp;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Log4j2
@WebFilter(filterName = "ConnectionFilter", urlPatterns = {"/startRunning", "/stopRunning"})
public class ConnectionFilter implements Filter {
    @Autowired
    DeviceRegistryServiceImp deviceRegistryServiceImp;
    private static ConcurrentHashMap<String, Boolean> deviceRegistryHashMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> connectHashMap = new ConcurrentHashMap<>();

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
        List<DeviceRegistry> list = deviceRegistryServiceImp.getDao().findAll();
        for (DeviceRegistry deviceRegistry : list) {
            deviceRegistryHashMap.put(deviceRegistry.getDeviceId(), deviceRegistry.getIsRegistered());
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String deviceId = ((HttpServletRequest) servletRequest).getHeader("deviceId");
        log.info("header:" + deviceId);
        log.info(deviceId + "---------------------------------");
        Boolean value = deviceRegistryHashMap.getOrDefault(deviceId, null);
        if (value != null) {
            if (value.equals(true)) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                disconnect(deviceId);
            }
        }
    }

    @Override
    public void destroy() {
        System.out.println("----------------------->ConnectionFilter过滤器被销毁");
    }

    public static void connect(String deviceId) {
        try {
            connectHashMap.put(deviceId, true);
            log.info(connectHashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(String deviceId) {
        try {
            connectHashMap.put(deviceId, false);
            log.info(connectHashMap);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static Boolean isConnect(String deviceId) {
        log.info(connectHashMap.toString());
        return connectHashMap.getOrDefault(deviceId, null);
    }
}
