package io.github.rosestack.spring.boot.encrypt.config;

import io.github.rosestack.encrypt.DefaultFieldEncryptor;
import io.github.rosestack.encrypt.FieldEncryptor;
import io.github.rosestack.encrypt.hash.HashService;
import io.github.rosestack.encrypt.rotation.KeyRotationManager;
import io.github.rosestack.encrypt.rotation.RotationFieldEncryptor;
import io.github.rosestack.spring.boot.encrypt.AutoKeyRotationScheduler;
import io.github.rosestack.spring.boot.encrypt.controller.KeyRotationController;
import io.github.rosestack.spring.factory.YmlPropertySourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@RequiredArgsConstructor
@PropertySource(value = "classpath:application-rose-crypto.yaml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CryptoProperties.class)
@Import({KeyRotationController.class, CryptoMonitorConfig.class})
@AutoConfiguration
public class CryptoAutoConfig {
    private final CryptoProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public FieldEncryptor fieldEncryptor() {
        // 验证配置
        properties.validateConfiguration();
        return new DefaultFieldEncryptor(properties.getSecretKey(), properties.isFailOnError());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.crypto.hash", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HashService hashService() {
        return new HashService(properties.getHash(), properties.isFailOnError());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.crypto.key-rotation", name = "enabled", havingValue = "true")
    public KeyRotationManager keyRotationManager() {
        return new KeyRotationManager();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.crypto.key-rotation", name = "enabled", havingValue = "true")
    public RotationFieldEncryptor rotationAwareFieldEncryptor(KeyRotationManager keyRotationManager) {
        return new RotationFieldEncryptor(keyRotationManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rose.crypto.key-rotation", name = "enabled", havingValue = "true")
    public AutoKeyRotationScheduler autoKeyRotationScheduler(KeyRotationManager keyRotationManager) {
        return new AutoKeyRotationScheduler(keyRotationManager, properties.getKeyRotation());
    }
}
