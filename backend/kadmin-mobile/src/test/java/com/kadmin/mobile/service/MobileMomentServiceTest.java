package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.domain.MobileMomentLike;
import com.kadmin.mobile.domain.MobileMomentPost;
import com.kadmin.mobile.mapper.MobileMomentCommentMapper;
import com.kadmin.mobile.mapper.MobileMomentLikeMapper;
import com.kadmin.mobile.mapper.MobileMomentPostMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 动态点赞幂等单元测试：MobileMomentService.toggleLike 的首赞/取消/唯一键冲突转幂等分支。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MobileMomentServiceTest {

    private static final Long POST_ID = 10L;
    private static final Long EMPLOYEE_ID = 20L;

    @Mock
    private MobileMomentLikeMapper likeMapper;
    @Mock
    private MobileMomentCommentMapper commentMapper;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private MobileMomentPostMapper postMapper;

    private MobileMomentService service;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        // 纯单元测试手动注册实体元数据，供 LambdaUpdateWrapper 解析列名
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, MobileMomentPost.class);
        TableInfoHelper.initTableInfo(assistant, MobileMomentLike.class);
    }

    @BeforeEach
    void setUp() {
        service = new MobileMomentService(likeMapper, commentMapper, employeeMapper, jdbcTemplate);
        ReflectionTestUtils.setField(service, "baseMapper", postMapper);
    }

    private MobileMomentPost post(Integer likeCount) {
        MobileMomentPost post = new MobileMomentPost();
        post.setId(POST_ID);
        post.setEmployeeId(30L);
        post.setStatus(1);
        post.setLikeCount(likeCount);
        return post;
    }

    @Test
    @DisplayName("点赞：动态不存在时抛出异常")
    void toggleLike_postNotFound() {
        when(postMapper.selectById(POST_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.toggleLike(POST_ID, EMPLOYEE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("动态不存在");
    }

    @Test
    @DisplayName("点赞：动态已下架（状态非1）时抛出异常")
    void toggleLike_postNotActive() {
        MobileMomentPost removed = post(0);
        removed.setStatus(0);
        when(postMapper.selectById(POST_ID)).thenReturn(removed);
        assertThatThrownBy(() -> service.toggleLike(POST_ID, EMPLOYEE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("动态不存在");
    }

    @Test
    @DisplayName("首赞：插入点赞记录并将 like_count 加一，返回 liked=true")
    void toggleLike_firstLikeIncrementsCount() {
        when(postMapper.selectById(POST_ID)).thenReturn(post(0), post(1));
        when(likeMapper.selectOne(any())).thenReturn(null);

        Map<String, Object> result = service.toggleLike(POST_ID, EMPLOYEE_ID);

        assertThat(result.get("liked")).isEqualTo(true);
        assertThat(result.get("likeCount")).isEqualTo(1);
        verify(likeMapper).insert(any(MobileMomentLike.class));
        ArgumentCaptor<Wrapper<MobileMomentPost>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).update(isNull(), wrapperCaptor.capture());
        // 计数递增语句
        LambdaUpdateWrapper<MobileMomentPost> wrapper =
                (LambdaUpdateWrapper<MobileMomentPost>) wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSet()).contains("like_count = IFNULL(like_count, 0) + 1");
    }

    @Test
    @DisplayName("取消赞：删除点赞记录并将 like_count 减一（不为负），返回 liked=false")
    void toggleLike_cancelLikeDecrementsCount() {
        MobileMomentLike existing = new MobileMomentLike();
        existing.setId(55L);
        existing.setPostId(POST_ID);
        existing.setEmployeeId(EMPLOYEE_ID);
        when(postMapper.selectById(POST_ID)).thenReturn(post(3), post(2));
        when(likeMapper.selectOne(any())).thenReturn(existing);

        Map<String, Object> result = service.toggleLike(POST_ID, EMPLOYEE_ID);

        assertThat(result.get("liked")).isEqualTo(false);
        assertThat(result.get("likeCount")).isEqualTo(2);
        verify(likeMapper).deleteById(55L);
        ArgumentCaptor<Wrapper<MobileMomentPost>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).update(isNull(), wrapperCaptor.capture());
        LambdaUpdateWrapper<MobileMomentPost> wrapper =
                (LambdaUpdateWrapper<MobileMomentPost>) wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSet()).contains("GREATEST(IFNULL(like_count, 0) - 1, 0)");
    }

    @Test
    @DisplayName("并发重复点赞：唯一键冲突转为幂等结果（liked=true，不重复计数）")
    void toggleLike_duplicateKeyTreatedAsAlreadyLiked() {
        MobileMomentPost current = post(7);
        when(postMapper.selectById(POST_ID)).thenReturn(current);
        when(likeMapper.selectOne(any())).thenReturn(null);
        when(likeMapper.insert(any(MobileMomentLike.class)))
                .thenThrow(new DuplicateKeyException("uk_post_employee duplicate"));
        // 冲突后按"已点赞"返回最新计数，刷新查询不应改变计数
        when(postMapper.selectById(POST_ID)).thenReturn(current);

        Map<String, Object> result = service.toggleLike(POST_ID, EMPLOYEE_ID);

        assertThat(result.get("liked")).isEqualTo(true);
        assertThat(result.get("likeCount")).isEqualTo(7);
        // 冲突路径不更新计数，避免重复加一
        verify(postMapper, never()).update(isNull(), any(Wrapper.class));
    }
}
