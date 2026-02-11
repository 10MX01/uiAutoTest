package com.uiauto.aiscript.repository;

import com.uiauto.aiscript.entity.AIServiceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI服务配置Repository
 */
@Repository
public interface AIServiceConfigRepository extends JpaRepository<AIServiceConfigEntity, Long> {

    /**
     * 根据状态查询AI服务配置
     */
    List<AIServiceConfigEntity> findByStatus(String status);

    /**
     * 查询默认的AI服务配置
     */
    Optional<AIServiceConfigEntity> findByIsDefaultTrue();

    /**
     * 根据provider查询配置
     */
    List<AIServiceConfigEntity> findByProvider(String provider);

    /**
     * 根据provider和status查询
     */
    List<AIServiceConfigEntity> findByProviderAndStatus(String provider, String status);
}
