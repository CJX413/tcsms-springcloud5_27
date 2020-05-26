package com.tcsms.securityserver.Filter;

import com.google.gson.Gson;
import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Entity.OperationLog;
import com.tcsms.securityserver.Service.ServiceImp.DeviceRegistryServiceImp;
import com.tcsms.securityserver.Utils.RequestReaderHttpServletRequestWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;


@Log4j2
@WebFilter(filterName = "OperationLogFilter", urlPatterns = {"/operationLog"})
public class OperationLogFilter implements Filter {
    @Autowired
    DeviceRegistryServiceImp deviceRegistryServiceImp;
    private static Gson gson = new Gson();


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("----------------------->OperationLogFilter过滤器被创建");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {


            String deviceId = ((HttpServletRequest) servletRequest).getHeader("deviceId");
            log.info("header:" + deviceId);
            Boolean isConnect = ConnectionFilter.isConnect(deviceId);
            if (isConnect == null) {
                RequestReaderHttpServletRequestWrapper requestWrapper = new RequestReaderHttpServletRequestWrapper((HttpServletRequest) servletRequest);
                String json = getJson(requestWrapper);
                OperationLog operationLog = gson.fromJson(json, OperationLog.class);
                DeviceRegistry deviceRegistry = new DeviceRegistry();
                deviceRegistry.setDeviceModel(operationLog.getDeviceModel());
                deviceRegistry.setIsRegistered(false);
                deviceRegistry.setDeviceId(operationLog.getDeviceId());
                deviceRegistry.setLatitude(operationLog.getLatitude());
                deviceRegistry.setLongitude(operationLog.getLongitude());
                deviceRegistryServiceImp.insertNewDevice(deviceRegistry);
                ConnectionFilter.disconnect(deviceId);
            } else {
                if (isConnect.equals(true)) {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        System.out.println("----------------------->过滤器被销毁");
    }

    private String getJson(HttpServletRequestWrapper req) {
        try {
            try (BufferedReader reader = req.getReader()) {
                StringBuilder json = new StringBuilder();
                String s;
                while ((s = reader.readLine()) != null) {
                    json.append(s.trim());
                }
                return json.toString();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
