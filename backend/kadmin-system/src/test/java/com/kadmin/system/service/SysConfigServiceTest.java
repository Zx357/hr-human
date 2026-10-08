package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.system.domain.SysConfig;
import com.kadmin.system.mapper.SysConfigMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统参数配置服务单元测试：
 * 新增唯一键校验 / 公开接口过滤（非公开、停用、空值不返回）/ keys 精确匹配忽略未知键。
 */
@ExtendWith(MockitoExtension.class)
class SysConfigServiceTest {

    private static final Long CONFIG_ID = 100L;

    @Mock
    private SysConfigMapper configMapper;

    private SysConfigService service;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        // 纯单元测试无 MyBatis 启动流程，手动注册实体元数据，
        // 否则 LambdaQueryWrapper 解析列名时缺少 lambda cache
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, SysConfig.class);
    }

    @BeforeEach
    void setUp() {
        service = new SysConfigService();
        // ServiceImpl 基类的 baseMapper 字段注入 mock（save/count/list 均走该 mapper）
        ReflectionTestUtils.setField(service, "baseMapper", configMapper);
    }

    // ==================== 构造测试数据 ====================

    private SysConfig config(String key, String name, String value, String group,
            Integer isPublic, Integer status, Integer sortOrder) {
        SysConfig config = new SysConfig();
        config.setId(CONFIG_ID);
        config.setConfigKey(key);
        config.setConfigName(name);
        config.setConfigValue(value);
        config.setConfigGroup(group);
        config.setIsPublic(isPublic);
        config.setStatus(status);
        config.setSortOrder(sortOrder);
        return config;
    }

    /** 覆盖全部过滤分支的公开配置候选集 */
    private List<SysConfig> publicCandidates() {
        return List.of(
                // 正常公开配置：返回
                config(SysConfigService.KEY_AMAP, "高德地图Key", " amap-key-123 ", "map_config", 1, 1, 1),
                // 非公开配置（is_public=0）：不返回，防敏感配置泄露
                config("secret.key", "敏感配置", "secret-value", "basic", 0, 1, 2),
                // 停用配置（status=0）：不返回
                config("map.test.disabled", "停用配置", "disabled-value", "map_config", 1, 0, 3),
                // 空值配置：不返回（前端按未配置降级）
                config(SysConfigService.KEY_WECHAT_APPID, "微信小程序AppID", "  ", "wechat_config", 1, 1, 4),
                // null 值配置：不返回
                config("empty.key", "空值配置", null, "basic", 1, 1, 5));
    }

    // ==================== 新增：唯一键校验 ====================

    @Test
    @DisplayName("新增：配置键已存在时拒绝并提示唯一键冲突")
    void addConfig_duplicateKeyRejected() {
        SysConfig config = config("map.amap.key", "高德地图Key", "value", "map_config", 1, 1, 1);
        config.setId(null);
        when(configMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.addConfig(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("配置键已存在")
                .hasMessageContaining("map.amap.key");
        verify(configMapper, never()).insert(any(SysConfig.class));
    }

    @Test
    @DisplayName("新增：键不存在时保存成功并补齐默认值（分组basic/公开/启用/排序0）")
    void addConfig_newKeySavedWithDefaults() {
        SysConfig config = config(" mail.smtp.host ", "邮件服务器", "smtp.example.com", null, null, null, null);
        config.setId(null);
        when(configMapper.selectCount(any())).thenReturn(0L);
        when(configMapper.insert(any(SysConfig.class))).thenReturn(1);

        boolean success = service.addConfig(config);

        assertThat(success).isTrue();
        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(configMapper).insert(captor.capture());
        SysConfig saved = captor.getValue();
        // 键去空白
        assertThat(saved.getConfigKey()).isEqualTo("mail.smtp.host");
        // 默认分组/公开/启用/排序
        assertThat(saved.getConfigGroup()).isEqualTo(SysConfigService.GROUP_BASIC);
        assertThat(saved.getIsPublic()).isEqualTo(1);
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getSortOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("新增：配置键或名称为空时拒绝")
    void addConfig_blankKeyOrNameRejected() {
        SysConfig noKey = config("  ", "名称", null, null, null, null, null);
        noKey.setId(null);
        assertThatThrownBy(() -> service.addConfig(noKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("配置键不能为空");

        SysConfig noName = config("some.key", " ", null, null, null, null, null);
        noName.setId(null);
        assertThatThrownBy(() -> service.addConfig(noName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("配置名称不能为空");
        verify(configMapper, never()).insert(any(SysConfig.class));
    }

    // ==================== 修改：配置键不可改 ====================

    @Test
    @DisplayName("修改：尝试修改配置键时拒绝")
    void updateConfig_keyChangeRejected() {
        when(configMapper.selectById(CONFIG_ID))
                .thenReturn(config("map.amap.key", "高德地图Key", "v", "map_config", 1, 1, 1));
        SysConfig update = config("map.amap.key2", "高德地图Key", "v2", "map_config", 1, 1, 1);

        assertThatThrownBy(() -> service.updateConfig(update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("配置键不允许修改");
        verify(configMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("修改：按 id 更新允许字段且不携带配置键")
    void updateConfig_appliesAllowedFieldsWithoutKey() {
        when(configMapper.selectById(CONFIG_ID))
                .thenReturn(config("map.amap.key", "高德地图Key", "old", "map_config", 1, 1, 1));
        when(configMapper.update(any(), any())).thenReturn(1);
        SysConfig update = new SysConfig();
        update.setId(CONFIG_ID);
        // 键原样回传（未变更）应被允许
        update.setConfigKey("map.amap.key");
        update.setConfigValue("new-value");
        update.setIsPublic(0);
        update.setStatus(0);
        update.setSortOrder(9);

        boolean success = service.updateConfig(update);

        assertThat(success).isTrue();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysConfig>> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class);
        verify(configMapper).update(any(), captor.capture());
        String sqlSet = captor.getValue().getSqlSet();
        // 更新列不包含配置键，键不可被覆盖
        assertThat(sqlSet).contains("config_value").contains("is_public").contains("status").contains("sort_order");
        assertThat(sqlSet).doesNotContain("config_key");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue("new-value");
    }

    @Test
    @DisplayName("修改：配置值传空白即清空（置 NULL），未传的字段不受影响")
    void updateConfig_blankValueClearsStoredValue() {
        when(configMapper.selectById(CONFIG_ID))
                .thenReturn(config("wechat.appid", "微信小程序AppID", "wx123", "wechat_config", 1, 1, 1));
        when(configMapper.update(any(), any())).thenReturn(1);
        SysConfig update = new SysConfig();
        update.setId(CONFIG_ID);
        update.setConfigValue("   ");

        boolean success = service.updateConfig(update);

        assertThat(success).isTrue();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysConfig>> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class);
        verify(configMapper).update(any(), captor.capture());
        // 仅更新值列，且参数值为 null（清空）而非空白字符串
        assertThat(captor.getValue().getSqlSet()).contains("config_value");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue(null);
    }

    // ==================== 删除 ====================

    @Test
    @DisplayName("删除：配置不存在时拒绝")
    void deleteConfig_notFoundRejected() {
        when(configMapper.selectById(CONFIG_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.deleteConfig(CONFIG_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("配置不存在");
        verify(configMapper, never()).deleteById(CONFIG_ID);
    }

    // ==================== 公开读取：过滤与匹配 ====================

    @Test
    @DisplayName("公开读取：仅返回 存在+公开+启用+值非空 的配置，值去除首尾空白")
    void publicConfigs_filtersNonPublicDisabledAndBlankValues() {
        when(configMapper.selectList(any())).thenReturn(publicCandidates());

        Map<String, String> result = service.getPublicConfigs(null);

        // 仅高德Key满足全部条件；敏感/停用/空值/null值配置均不返回
        assertThat(result).containsOnlyKeys(SysConfigService.KEY_AMAP);
        assertThat(result.get(SysConfigService.KEY_AMAP)).isEqualTo("amap-key-123");
    }

    @Test
    @DisplayName("公开读取：keys 精确匹配，未知键直接忽略（不报错）")
    void publicConfigs_exactKeyMatchIgnoresUnknownKeys() {
        List<SysConfig> candidates = List.of(
                config(SysConfigService.KEY_AMAP, "高德地图Key", "amap-key", "map_config", 1, 1, 1),
                config(SysConfigService.KEY_WECHAT_APPID, "微信小程序AppID", "wx-123", "wechat_config", 1, 1, 2));
        when(configMapper.selectList(any())).thenReturn(candidates);

        Map<String, String> result = service.getPublicConfigs(
                "map.amap.key, unknown.key , wechat.appid");

        // 未知键 unknown.key 被忽略；命中的两个键正常返回
        assertThat(result).containsOnlyKeys(SysConfigService.KEY_AMAP, SysConfigService.KEY_WECHAT_APPID);
        assertThat(result.get(SysConfigService.KEY_AMAP)).isEqualTo("amap-key");
        assertThat(result.get(SysConfigService.KEY_WECHAT_APPID)).isEqualTo("wx-123");
    }

    @Test
    @DisplayName("公开读取：keys 指定的键为公开但停用时同样不返回")
    void publicConfigs_requestedButDisabledKeyNotReturned() {
        List<SysConfig> candidates = List.of(
                config("map.test.disabled", "停用配置", "disabled-value", "map_config", 1, 0, 1));
        when(configMapper.selectList(any())).thenReturn(candidates);

        Map<String, String> result = service.getPublicConfigs("map.test.disabled");

        assertThat(result).isEmpty();
    }
}
