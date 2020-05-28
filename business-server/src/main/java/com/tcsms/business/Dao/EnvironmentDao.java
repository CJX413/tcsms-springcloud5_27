package com.tcsms.business.Dao;

import com.tcsms.business.Entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface EnvironmentDao extends JpaRepository<Environment, Integer>, JpaSpecificationExecutor<Environment>, Serializable {

}
