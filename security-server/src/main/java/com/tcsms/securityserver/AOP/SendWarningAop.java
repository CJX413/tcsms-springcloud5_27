package com.tcsms.securityserver.AOP;


import com.tcsms.securityserver.Entity.WarningDetail;
import com.tcsms.securityserver.Entity.WarningLog;
import com.tcsms.securityserver.Service.ServiceImp.WarningLogServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.WarningRankingServiceImp;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;


@Aspect
@Component
@Log4j2
public class SendWarningAop {

    @Autowired
    private WarningLogServiceImp warningLogServiceImp;
    @Autowired
    private WarningRankingServiceImp warningRankingServiceImp;

    private static CopyOnWriteArraySet<Integer> warningSet = new CopyOnWriteArraySet<>();

    @Pointcut("execution(* com.tcsms.securityserver.Service.ServiceImp.RestTemplateServiceImp.sendWarning(..))")
    public void sendWarning() {
    }


    @Around(value = "sendWarning()")
    public void warningAround(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        WarningLog warningLog = (WarningLog) args[0];
        if (!warningSet.contains(warningLog.hashCode())) {
            log.info("还未发送过");
            warningSet.add(warningLog.hashCode());
            warningLogServiceImp.save(warningLog);
            addWarning(warningLog);
            point.proceed();
        } else {
            log.info("已经发送过");
        }

    }

    private void addWarning(WarningLog warningLog) {
        List<WarningDetail> list = warningLog.getWarningDetails();
        if (list == null) {
            return;
        }
        if (warningLog.getCode() % 2 == 0) {
            list.forEach(warningDetail -> {
                warningRankingServiceImp.addRedWarningById(warningDetail.getDeviceId());
            });
        } else {
            list.forEach(warningDetail -> {
                warningRankingServiceImp.addYellowWarningById(warningDetail.getDeviceId());
            });
        }

    }

    public static void clearWarningSet() {
        warningSet.clear();
    }
}
