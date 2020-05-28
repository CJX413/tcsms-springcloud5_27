package com.tcsms.securityserver.Service.ServiceImp;

import com.tcsms.securityserver.Dao.DeviceRegistryDao;
import com.tcsms.securityserver.Dao.OperationLogDao;
import com.tcsms.securityserver.Dao.SqlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class OperationLogDateServiceImp {
    @Autowired
    SqlMapper sqlMapper;
    @Autowired
    DeviceRegistryDao deviceRegistryDao;
    @Autowired
    OperationLogDao operationLogDao;

    public SqlMapper getMapper() {
        return sqlMapper;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean refreshOperationLogTab() {
        try {
            sqlMapper.dropOperationLogClone();
            sqlMapper.cloneTabOfOperationLog();
            System.out.println("克隆表结构成功");
            SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
            String dateTime = df.format(new Date());
            //operation_log_date表存在
            if (sqlMapper.existsOperationLogDate(dateTime) > 0) {
                return false;
            }
            sqlMapper.renameOperationLogToOperationLogDate(dateTime);
            System.out.println("重命名表operation_log为operation_log_date成功");
            sqlMapper.renameOperationLogCloneToOperationLog();
            System.out.println("重命名表operation_log_clone为operation_log成功");
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//关键
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
