package com.tcsms.business.Service.ReceiveServiceImp;

import com.tcsms.business.Dao.UserInfoDao;
import com.tcsms.business.Entity.UserInfo;
import com.tcsms.business.Exception.CustomizeException;
import com.tcsms.business.Service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoServiceImp implements UserInfoService {
    @Autowired
    UserInfoDao userInfoDao;
    @Autowired
    private RedisServiceImp redisServiceImp;

    public UserInfoDao getDao() {
        return userInfoDao;
    }

    public UserInfo findByUserName(String username) {
        Optional<UserInfo> optional = userInfoDao.findById(username);
        return optional.orElse(null);
    }

    public void updateAdminInfo(UserInfo userInfo) throws RuntimeException {
        userInfoDao.save(userInfo);
    }

    public void updateUserInfo(UserInfo userInfo) throws RuntimeException {
        userInfoDao.findById(userInfo.getUsername()).ifPresent(info -> {
            info.setName(userInfo.getName());
            info.setWorkerId(userInfo.getWorkerId());
            info.setSex(userInfo.getSex());
            userInfoDao.save(info);
        });
    }

    public void updatePhone(String username, String phone, String verificationCode) throws Exception {
        if (!redisServiceImp.checkVerificationCode(phone, verificationCode)) {
            throw new CustomizeException("验证码错误！");
        }
        if (userInfoDao.existsByPhoneNumber(phone)) {
            throw new CustomizeException("该号码已存在!");
        }
        userInfoDao.updatePhoneNimberByUsername(username, phone);
    }

}
