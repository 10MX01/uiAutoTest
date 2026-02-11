package com.uiauto.aiscript.repository;

import com.uiauto.aiscript.entity.AICallLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI调用日志Repository
 */
@Repository
public interface AICallLogRepository extends JpaRepository<AICallLogEntity, Long> {

    /**
     * 根据调用类型查询日志
     */
    List<AICallLogEntity> findByCallTypeOrderByCreatedTimeDesc(String callType);

    /**
     * 根据成功状态查询日志
     */
    List<AICallLogEntity> findBySuccessOrderByCreatedTimeDesc(Boolean success);

    /**
     * 根据服务配置ID查询日志
     */
    List<AICallLogEntity> findByServiceConfigIdOrderByCreatedTimeDesc(Long serviceConfigId);

    /**
     * 根据调用类型和成功状态查询
     */
    List<AICallLogEntity> findByCallTypeAndSuccessOrderByCreatedTimeDesc(String callType, Boolean success);
}
