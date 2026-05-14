package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.MobileMomentLike;
import com.kadmin.entity.MobileMomentPost;
import com.kadmin.mapper.MobileMomentLikeMapper;
import com.kadmin.mapper.MobileMomentPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MobileMomentService extends ServiceImpl<MobileMomentPostMapper, MobileMomentPost> {

    private final MobileMomentLikeMapper likeMapper;

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
            likeMapper.insert(like);
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

    public Map<String, Object> getMessageSummary(Long employeeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", 0);
        result.put("likeCount", 0);
        result.put("commentCount", 0);
        result.put("mentionCount", 0);
        return result;
    }

    private String normalizeTab(String tab) {
        if ("hot".equals(tab) || "recommend".equals(tab)) {
            return tab;
        }
        return "latest";
    }
}
