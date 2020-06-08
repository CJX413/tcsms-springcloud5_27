package com.tcsms.business.Service.ReceiveServiceImp;

import com.tcsms.business.Dao.RoleApplyDao;
import com.tcsms.business.Entity.RoleApply;
import com.tcsms.business.Exception.CustomizeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleApplyServiceImp {
    @Autowired
    private RoleApplyDao roleApplyDao;

    @Autowired
    private UserServiceImp userServiceImp;

    public RoleApplyDao getDao() {
        return roleApplyDao;
    }

    public void applyRole(RoleApply roleApply) throws Exception {
        if (userServiceImp.getRoleByUsername(roleApply.getUsername()).equals(roleApply.getRole())) {
            throw new CustomizeException("您已经拥有该权限了！");
        }
        if (isApplyRole(roleApply.getUsername())) {
            throw new CustomizeException("你已经发出过申请了！");
        }
        if (!UserServiceImp.roleSet.contains(roleApply.getRole())) {
            throw new CustomizeException("申请的角色不存在！");
        }
        roleApplyDao.save(roleApply);

    }

    public void agreeApplyRole(RoleApply roleApply) throws RuntimeException {
        userServiceImp.changeRoleByUsername(roleApply.getUsername(), roleApply.getRole());
        roleApplyDao.deleteById(roleApply.getUsername());
    }

    public void refuseApplyRole(RoleApply roleApply) throws RuntimeException {
        roleApplyDao.deleteById(roleApply.getUsername());
    }

    public boolean isApplyRole(String username) {
        return roleApplyDao.findById(username).isPresent();
    }
}
