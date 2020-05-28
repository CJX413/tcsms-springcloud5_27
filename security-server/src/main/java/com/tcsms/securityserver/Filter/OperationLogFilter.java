package com.tcsms.securityserver.Filter;

import lombok.extern.log4j.Log4j2;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Log4j2
@WebFilter(filterName = "OperationLogFilter", urlPatterns = {"/operationLog"})
public class OperationLogFilter implements Filter {
//    @Autowired
//    DeviceRegistryServiceImp deviceRegistryServiceImp;
//    private static Gson gson = new Gson();


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("----------------------->OperationLogFilter过滤器被创建");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            String deviceId = ((HttpServletRequest) servletRequest).getHeader("deviceId");
            if (ConnectionFilter.isConnect(deviceId)) {
                log.info("deviceId:{}被放行了", deviceId);
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                log.info("deviceId:{}被拒绝了", deviceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        System.out.println("----------------------->过滤器被销毁");
    }

//    private String getJson(HttpServletRequestWrapper req) {
//        try {
//            try (BufferedReader reader = req.getReader()) {
//                StringBuilder json = new StringBuilder();
//                String s;
//                while ((s = reader.readLine()) != null) {
//                    json.append(s.trim());
//                }
//                return json.toString();
//            }
//        } catch (IOException e) {
//            return null;
//        }
//    }
}
