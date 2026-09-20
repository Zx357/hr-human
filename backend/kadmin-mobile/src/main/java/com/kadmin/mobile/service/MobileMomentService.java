package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.mobile.domain.MobileMomentComment;
import com.kadmin.mobile.domain.MobileMomentLike;
import com.kadmin.mobile.domain.MobileMomentPost;
import com.kadmin.mobile.mapper.MobileMomentCommentMapper;
import com.kadmin.mobile.mapper.MobileMomentLikeMapper;
import com.kadmin.mobile.mapper.MobileMomentPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MobileMomentService extends ServiceImpl<MobileMomentPostMapper, MobileMomentPost> {

    private final MobileMomentLikeMapper likeMapper;
    private final MobileMomentCommentMapper commentMapper;
    private final EmployeeMapper employeeMapper;
    private final JdbcTemplate jdbcTemplate;

    public Page<MobileMomentPost> pagePosts(int pageNum, int pageSize, String tab, Long employeeId) {
        return baseMapper.selectPostPage(new Page<>(pageNum, pageSize), normalizeTab(tab), employeeId);
    }

    public MobileMomentPost getPostDetail(Long id, Long employeeId) {
        return baseMapper.selectPostDetail(id, employeeId);
    }

    @Transactional
    public MobileMomentPost createPost(MobileMomentPost post, Long employeeId) {
        if (!StringUtils.hasText(post.getContent()) && !StringUtils.hasText(post.getImages())) {
            throw new IllegalArgumentException("动态内容不能为空");
        }
        post.setId(null);
        post.setEmployeeId(employeeId);
        post.setStatus(1);
        post.setVisibility(post.getVisibility() == null ? 1 : post.getVisibility());
        post.setCommentCount(0);
        post.setLikeCount(0);
        save(post);
        return getPostDetail(post.getId(), employeeId);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long postId, Long employeeId) {
        MobileMomentPost post = getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() != 1)) {
            throw new IllegalArgumentException("动态不存在");
        }

        MobileMomentLike existing = likeMapper.selectOne(new LambdaQueryWrapper<MobileMomentLike>()
                .eq(MobileMomentLike::getPostId, postId)
                .eq(MobileMomentLike::getEmployeeId, employeeId)
                .last("LIMIT 1"));

        boolean liked;
        if (existing == null) {
            MobileMomentLike like = new MobileMomentLike();
            like.setPostId(postId);
            like.setEmployeeId(employeeId);
            try {
                likeMapper.insert(like);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发重复点赞被唯一键拦截：按已点赞幂等处理，不重复计数
                MobileMomentPost refreshed = getById(postId);
                Map<String, Object> result = new HashMap<>();
                result.put("liked", true);
                result.put("likeCount", refreshed != null && refreshed.getLikeCount() != null ? refreshed.getLikeCount() : 0);
                return result;
            }
            update(new LambdaUpdateWrapper<MobileMomentPost>()
                    .eq(MobileMomentPost::getId, postId)
                    .setSql("like_count = IFNULL(like_count, 0) + 1"));
            liked = true;
        } else {
            likeMapper.deleteById(existing.getId());
            update(new LambdaUpdateWrapper<MobileMomentPost>()
                    .eq(MobileMomentPost::getId, postId)
                    .setSql("like_count = GREATEST(IFNULL(like_count, 0) - 1, 0)"));
            liked = false;
        }

        MobileMomentPost refreshed = getById(postId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", refreshed != null && refreshed.getLikeCount() != null ? refreshed.getLikeCount() : 0);
        return result;
    }

    // ==================== 评论 ====================

    /**
     * 动态评论列表（时间正序）
     */
    public List<MobileMomentComment> listComments(Long postId) {
        List<MobileMomentComment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<MobileMomentComment>()
                        .eq(MobileMomentComment::getPostId, postId)
                        .orderByAsc(MobileMomentComment::getId));
        fillCommentAuthor(comments);
        return comments;
    }

    /**
     * 发表评论（同时更新动态评论数）
     */
    @Transactional
    public MobileMomentComment addComment(Long postId, Long employeeId, String content) {
        MobileMomentPost post = getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() != 1)) {
            throw new IllegalArgumentException("动态不存在");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        MobileMomentComment comment = new MobileMomentComment();
        comment.setPostId(postId);
        comment.setEmployeeId(employeeId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);

        update(new LambdaUpdateWrapper<MobileMomentPost>()
                .eq(MobileMomentPost::getId, postId)
                .setSql("comment_count = IFNULL(comment_count, 0) + 1"));

        fillCommentAuthor(List.of(comment));
        return comment;
    }

    /**
     * 删除自己的动态（软删除：状态置0）
     */
    @Transactional
    public void deletePost(Long postId, Long employeeId) {
        MobileMomentPost post = getById(postId);
        if (post == null) {
            throw new IllegalArgumentException("动态不存在");
        }
        if (!post.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("只能删除自己的动态");
        }
        removeById(postId);
    }

    /**
     * 删除评论（仅评论作者本人，同步递减评论数）
     */
    @Transactional
    public void deleteComment(Long commentId, Long employeeId) {
        MobileMomentComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!comment.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }
        commentMapper.deleteById(commentId);
        update(new LambdaUpdateWrapper<MobileMomentPost>()
                .eq(MobileMomentPost::getId, comment.getPostId())
                .setSql("comment_count = GREATEST(IFNULL(comment_count, 0) - 1, 0)"));
    }

    private void fillCommentAuthor(List<MobileMomentComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Long> authorIds = comments.stream()
                .map(MobileMomentComment::getEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (authorIds.isEmpty()) {
            return;
        }
        Map<Long, HrEmployee> authors = employeeMapper.selectBatchIds(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(HrEmployee::getId, e -> e, (a, b) -> a));
        for (MobileMomentComment comment : comments) {
            HrEmployee author = authors.get(comment.getEmployeeId());
            if (author != null) {
                comment.setAuthorName(author.getName());
                comment.setAuthorAvatar(author.getAvatar());
            }
        }
    }

    // ==================== 消息中心 ====================

    /**
     * 消息中心统计：自上次查看以来我的动态收到的新点赞/新评论数
     */
    public Map<String, Object> getMessageSummary(Long employeeId) {
        LocalDateTime lastView = getLastViewTime(employeeId);

        Long newLikes = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM mobile_moment_like l
                JOIN mobile_moment_post p ON l.post_id = p.id
                WHERE p.employee_id = ? AND p.status = 1 AND l.employee_id != ?
                  AND (l.created_time > ? OR ? IS NULL)
                """, Long.class, employeeId, employeeId, lastView, lastView);

        Long newComments = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM mobile_moment_comment c
                JOIN mobile_moment_post p ON c.post_id = p.id
                WHERE p.employee_id = ? AND p.status = 1 AND c.employee_id != ?
                  AND (c.created_time > ? OR ? IS NULL)
                """, Long.class, employeeId, employeeId, lastView, lastView);

        long likeCount = newLikes != null ? newLikes : 0;
        long commentCount = newComments != null ? newComments : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", likeCount + commentCount);
        result.put("likeCount", likeCount);
        result.put("commentCount", commentCount);
        result.put("mentionCount", 0);
        return result;
    }

    /**
     * 消息列表：我的动态最近收到的点赞与评论（合并按时间倒序）
     */
    public List<Map<String, Object>> listMessages(Long employeeId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();

        jdbcTemplate.query("""
                SELECT l.employee_id, l.created_time, p.id AS post_id, p.content
                FROM mobile_moment_like l
                JOIN mobile_moment_post p ON l.post_id = p.id
                WHERE p.employee_id = ? AND p.status = 1 AND l.employee_id != ?
                ORDER BY l.created_time DESC LIMIT ?
                """, rs -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "like");
            item.put("operatorId", rs.getLong("employee_id"));
            item.put("postId", rs.getLong("post_id"));
            item.put("postContent", truncate(rs.getString("content"), 50));
            item.put("time", rs.getTimestamp("created_time"));
            result.add(item);
        }, employeeId, employeeId, limit);

        jdbcTemplate.query("""
                SELECT c.id AS comment_id, c.employee_id, c.content AS comment_content, c.created_time,
                       p.id AS post_id, p.content AS post_content
                FROM mobile_moment_comment c
                JOIN mobile_moment_post p ON c.post_id = p.id
                WHERE p.employee_id = ? AND p.status = 1 AND c.employee_id != ?
                ORDER BY c.created_time DESC LIMIT ?
                """, rs -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "comment");
            item.put("commentId", rs.getLong("comment_id"));
            item.put("operatorId", rs.getLong("employee_id"));
            item.put("content", rs.getString("comment_content"));
            item.put("postId", rs.getLong("post_id"));
            item.put("postContent", truncate(rs.getString("post_content"), 50));
            item.put("time", rs.getTimestamp("created_time"));
            result.add(item);
        }, employeeId, employeeId, limit);

        // 补充操作人姓名/头像
        List<Long> operatorIds = result.stream()
                .map(item -> (Long) item.get("operatorId"))
                .distinct()
                .toList();
        if (!operatorIds.isEmpty()) {
            Map<Long, HrEmployee> operators = employeeMapper.selectBatchIds(operatorIds).stream()
                    .collect(java.util.stream.Collectors.toMap(HrEmployee::getId, e -> e, (a, b) -> a));
            for (Map<String, Object> item : result) {
                HrEmployee operator = operators.get((Long) item.get("operatorId"));
                if (operator != null) {
                    item.put("operatorName", operator.getName());
                    item.put("operatorAvatar", operator.getAvatar());
                }
            }
        }

        result.sort((a, b) -> {
            Object ta = a.get("time");
            Object tb = b.get("time");
            if (ta instanceof java.sql.Timestamp t1 && tb instanceof java.sql.Timestamp t2) {
                return t2.compareTo(t1);
            }
            return 0;
        });
        if (result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    /**
     * 标记消息中心已读（推进查看时间）
     */
    public void markMessagesRead(Long employeeId) {
        jdbcTemplate.update("""
                INSERT INTO mobile_moment_view (employee_id, last_view_time)
                VALUES (?, NOW())
                ON DUPLICATE KEY UPDATE last_view_time = NOW()
                """, employeeId);
    }

    private LocalDateTime getLastViewTime(Long employeeId) {
        List<LocalDateTime> times = jdbcTemplate.query(
                "SELECT last_view_time FROM mobile_moment_view WHERE employee_id = ?",
                (rs, rowNum) -> rs.getTimestamp("last_view_time").toLocalDateTime(), employeeId);
        return times.isEmpty() ? null : times.get(0);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "…";
    }

    private String normalizeTab(String tab) {
        if ("hot".equals(tab) || "recommend".equals(tab)) {
            return tab;
        }
        return "latest";
    }
}
