package com.tcsms.business.Controller;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Log4j2
@RestController
public class SecurityController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/openSecuritySystem")
    @PreAuthorize(value = "hasAnyAuthority('ADMIN')")
    public String openSecuritySystem() {
        return restTemplate.getForObject("http://security-server/openSecuritySystem", String.class);
    }
    @RequestMapping("/closeSecuritySystem")
    @PreAuthorize(value = "hasAnyAuthority('ADMIN')")
    public String closeSecuritySystem() {
        return restTemplate.getForObject("http://security-server/closeSecuritySystem", String.class);
    }
    @RequestMapping("/restartSecuritySystem")
    @PreAuthorize(value = "hasAnyAuthority('ADMIN')")
    public String restartSecuritySystemSwitch() {
        return restTemplate.getForObject("http://security-server/restartSecuritySystem", String.class);
    }

    @RequestMapping("/monitorStatus")
    @PreAuthorize(value = "hasAnyAuthority('ADMIN')")
    public String monitorStatus() {
        return restTemplate.getForObject("http://security-server/monitorStatus", String.class);
    }

}
