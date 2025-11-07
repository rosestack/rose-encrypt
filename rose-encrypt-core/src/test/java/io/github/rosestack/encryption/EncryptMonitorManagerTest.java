package io.github.rosestack.encryption;

import static org.junit.jupiter.api.Assertions.*;

import io.github.rosestack.encrypt.EncryptUtils;
import io.github.rosestack.encrypt.enums.EncryptType;
import io.github.rosestack.encrypt.monitor.EncryptMonitorManager;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 加密监控功能测试
 *
 * @author chensoul
 * @since 1.0.0
 */
class EncryptMonitorManagerTest {

    private static final String TEST_SECRET_KEY = "MySecretKey12345MySecretKey12345"; // 32字符
    private static final String TEST_PLAIN_TEXT = "Hello, World! 这是一个测试文本。";

    @BeforeEach
    void setUp() {
        // 清空统计数据
        EncryptMonitorManager.getInstance().clearCache();
    }

    @Test
    @DisplayName("基础性能统计测试")
    void testBasicPerformanceStats() {
        // 执行一些加密操作
        EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);
        EncryptUtils.encrypt("Short", EncryptType.AES, TEST_SECRET_KEY);

        // 获取性能统计
        Map<String, Long> stats = EncryptMonitorManager.getInstance().getPerformanceStats();

        assertNotNull(stats);
        assertFalse(stats.isEmpty());

        // 验证成功计数
        assertTrue(stats.containsKey("encrypt_AES_success"));
        assertEquals(2L, stats.get("encrypt_AES_success").longValue());

        // 验证时间统计
        assertTrue(stats.containsKey("encrypt_AES_total_time"));
        assertTrue(stats.get("encrypt_AES_total_time") > 0);
    }

    @Test
    @DisplayName("算法使用统计测试")
    void testAlgorithmUsageStats() {
        // 使用不同算法
        EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);
        EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.DES, TEST_SECRET_KEY);
        EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);

        Map<String, Long> algorithmStats = EncryptMonitorManager.getInstance().getAlgorithmUsageStats();

        assertNotNull(algorithmStats);
        assertEquals(2L, algorithmStats.get("AES").longValue());
        assertEquals(1L, algorithmStats.get("DES").longValue());
    }

    @Test
    @DisplayName("错误统计测试")
    void testErrorStats() {
        try {
            // 故意使用无效密文触发错误
            EncryptUtils.decrypt("invalid_cipher_text", EncryptType.AES, TEST_SECRET_KEY);
        } catch (Exception e) {
            // 预期的异常
        }

        Map<String, Long> errorStats = EncryptMonitorManager.getInstance().getErrorStats();

        assertNotNull(errorStats);
        assertFalse(errorStats.isEmpty());

        // 验证错误计数
        assertTrue(errorStats.containsKey("decrypt_total_errors"));
        assertEquals(1L, errorStats.get("decrypt_total_errors").longValue());
    }

    @Test
    @DisplayName("数据大小统计测试")
    void testDataSizeStats() {
        // 不同大小的数据
        EncryptUtils.encrypt("Small", EncryptType.AES, TEST_SECRET_KEY); // small (5字符)
        EncryptUtils.encrypt(
                "This is a medium length text for testing crypto algorithms",
                EncryptType.AES,
                TEST_SECRET_KEY); // medium (>100字符)

        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            largeText.append("Large text ");
        }
        EncryptUtils.encrypt(largeText.toString(), EncryptType.AES, TEST_SECRET_KEY); // large (>1000字符)

        Map<String, Long> dataSizeStats = EncryptMonitorManager.getInstance().getDataSizeStats();

        assertNotNull(dataSizeStats);
        assertFalse(dataSizeStats.isEmpty());

        // 验证数据大小统计
        assertTrue(dataSizeStats.containsKey("encrypt_small"), "应该有小数据统计");
        assertTrue(dataSizeStats.containsKey("encrypt_large"), "应该有大数据统计");

        assertEquals(2L, dataSizeStats.get("encrypt_small").longValue(), "小数据应该有2次");
        assertEquals(1L, dataSizeStats.get("encrypt_large").longValue(), "大数据应该有1次");
    }

    @Test
    @DisplayName("完整监控报告测试")
    void testMonitoringReport() {
        // 执行一些操作
        String encrypted = EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);
        EncryptUtils.decrypt(encrypted, EncryptType.AES, TEST_SECRET_KEY);

        // 触发一个错误
        try {
            EncryptUtils.decrypt("invalid", EncryptType.AES, TEST_SECRET_KEY);
        } catch (Exception e) {
            // 预期的异常
        }

        Map<String, Object> report = EncryptMonitorManager.getInstance().getMonitoringReport();

        assertNotNull(report);
        assertTrue(report.containsKey("performance"));
        assertTrue(report.containsKey("algorithmUsage"));
        assertTrue(report.containsKey("errors"));
        assertTrue(report.containsKey("dataSizeDistribution"));
        assertTrue(report.containsKey("successRates"));
        assertTrue(report.containsKey("systemInfo"));

        // 验证系统信息
        @SuppressWarnings("unchecked")
        Map<String, Object> systemInfo = (Map<String, Object>) report.get("systemInfo");
        assertNotNull(systemInfo.get("timestamp"));
        assertNotNull(systemInfo.get("supportedAlgorithms"));
        assertNotNull(systemInfo.get("totalOperations"));

        // 验证成功率
        @SuppressWarnings("unchecked")
        Map<String, Double> successRates = (Map<String, Double>) report.get("successRates");
        assertNotNull(successRates);

        // 加密成功率应该是100%
        if (successRates.containsKey("encrypt_AES")) {
            assertEquals(100.0, successRates.get("encrypt_AES"), 0.01);
        }

        // 解密成功率应该是50%（1成功1失败）
        if (successRates.containsKey("decrypt_AES")) {
            assertEquals(50.0, successRates.get("decrypt_AES"), 0.01);
        }
    }

    @Test
    @DisplayName("缓存统计信息测试")
    void testCacheStats() {
        // 执行一些操作
        EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);

        String cacheStats = EncryptMonitorManager.getInstance().getCacheStats();

        assertNotNull(cacheStats);
        assertTrue(cacheStats.contains("性能计数器"));
        assertTrue(cacheStats.contains("算法使用统计"));
        assertTrue(cacheStats.contains("错误统计"));
        assertTrue(cacheStats.contains("数据大小统计"));
    }

    @Test
    @DisplayName("清空缓存测试")
    void testClearCache() {
        // 执行一些操作
        EncryptUtils.encrypt(TEST_PLAIN_TEXT, EncryptType.AES, TEST_SECRET_KEY);

        // 验证有统计数据
        assertFalse(EncryptMonitorManager.getInstance().getPerformanceStats().isEmpty());

        // 清空缓存
        EncryptMonitorManager.getInstance().clearCache();

        // 验证统计数据已清空
        assertTrue(EncryptMonitorManager.getInstance().getPerformanceStats().isEmpty());
        assertTrue(EncryptMonitorManager.getInstance().getAlgorithmUsageStats().isEmpty());
        assertTrue(EncryptMonitorManager.getInstance().getErrorStats().isEmpty());
        assertTrue(EncryptMonitorManager.getInstance().getDataSizeStats().isEmpty());
    }

    @Test
    @DisplayName("批量操作监控测试")
    void testBatchOperationMonitoring() {
        String[] plainTexts = {"Text1", "Text2", "Text3"};

        // 批量加密
        String[] encrypted = EncryptUtils.encryptBatch(plainTexts, EncryptType.AES, TEST_SECRET_KEY);

        // 批量解密
        EncryptUtils.decryptBatch(encrypted, EncryptType.AES, TEST_SECRET_KEY);

        Map<String, Long> algorithmStats = EncryptMonitorManager.getInstance().getAlgorithmUsageStats();

        // 批量操作应该记录每个单独的操作
        assertEquals(6L, algorithmStats.get("AES").longValue()); // 3次加密 + 3次解密
    }
}
