package cn.hwjcode.straberryguild.post.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hwjcode.straberryguild.common.exception.BusinessException;
import cn.hwjcode.straberryguild.pojo.dto.*;
import cn.hwjcode.straberryguild.pojo.entity.*;
import cn.hwjcode.straberryguild.pojo.vo.CommentVO;
import cn.hwjcode.straberryguild.pojo.vo.PostDetailVO;
import cn.hwjcode.straberryguild.pojo.vo.PostListVO;
import cn.hwjcode.straberryguild.pojo.vo.TagVO;
import cn.hwjcode.straberryguild.post.mapper.*;
import cn.hwjcode.straberryguild.post.service.PostService;
import cn.hwjcode.straberryguild.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCollectMapper postCollectMapper;
    private final UserFollowMapper userFollowMapper;
    private final PostCommentMapper postCommentMapper;

    @Override
    @Transactional
    public PostDetailVO createPost(Long userId, PostCreateRequest request) {
        Post post = new Post();
        post.setAuthorId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(StrUtil.isNotBlank(request.getCategory()) ? request.getCategory() : "推荐");
        post.setCover(StrUtil.nullToDefault(request.getCover(), ""));
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setShareCount(0);
        post.setStatus(1);

        postMapper.insert(post);
        log.info("帖子创建成功: postId={}, authorId={}", post.getId(), userId);

        List<Long> tagIds = resolveTagIds(request.getTagIds(), request.getTags());
        if (!tagIds.isEmpty()) {
            bindTags(post.getId(), tagIds);
        }

        return buildDetailVO(post);
    }

    @Override
    @Transactional
    public PostDetailVO updatePost(Long userId, Long postId, PostUpdateRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            throw BusinessException.forbidden("无权操作此帖子");
        }

        if (StrUtil.isNotBlank(request.getTitle())) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (StrUtil.isNotBlank(request.getCategory())) {
            post.setCategory(request.getCategory());
        }
        if (request.getCover() != null) {
            post.setCover(request.getCover());
        }

        postMapper.updateById(post);

        if (request.getTagIds() != null) {
            postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
            bindTags(postId, request.getTagIds());
        }

        return buildDetailVO(post);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            throw BusinessException.forbidden("无权操作此帖子");
        }

        List<Long> tagIds = getPostTagIds(postId);
        postMapper.deleteById(postId);
        postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
        tagIds.forEach(this::decrementTagPostCount);

        log.info("帖子删除成功: postId={}", postId);
    }

    @Override
    @Transactional
    public PostDetailVO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }

        post.setViewCount(post.getViewCount() == null ? 1 : post.getViewCount() + 1);
        postMapper.updateById(post);

        return buildDetailVO(post);
    }

    @Override
    public List<PostListVO> getPostList(Long userId, Integer page, Integer size, Long tagId,
                                        String keyword, String category, String feed, String sort) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);

        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Post::getCategory, category);
        }

        if ("follow".equals(feed)) {
            if (userId == null) {
                return Collections.emptyList();
            }
            List<UserFollow> follows = userFollowMapper.selectList(
                    new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowerId, userId)
            );
            List<Long> authorIds = follows.stream().map(UserFollow::getFollowingId).toList();
            if (authorIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(Post::getAuthorId, authorIds);
        } else if ("collected".equals(feed)) {
            if (userId == null) {
                return Collections.emptyList();
            }
            List<PostCollect> collects = postCollectMapper.selectList(
                    new LambdaQueryWrapper<PostCollect>().eq(PostCollect::getUserId, userId)
            );
            List<Long> collectedPostIds = collects.stream().map(PostCollect::getPostId).toList();
            if (collectedPostIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(Post::getId, collectedPostIds);
        }

        // 标签筛选放到分页之前，避免先分页再过滤导致每页数量不一致
        if (tagId != null) {
            List<PostTag> postTags = postTagMapper.selectList(
                    new LambdaQueryWrapper<PostTag>().eq(PostTag::getTagId, tagId)
            );
            List<Long> postIds = postTags.stream().map(PostTag::getPostId).toList();
            if (postIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(Post::getId, postIds);
        }

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Post::getLikeCount).orderByDesc(Post::getCreateTime);
        } else {
            wrapper.orderByDesc(Post::getCreateTime);
        }

        Page<Post> result = postMapper.selectPage(new Page<>(page, size), wrapper);
        return buildListVO(result.getRecords(), userId);
    }

    @Override
    public long countPosts(String category) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Post::getCategory, category);
        }
        return postMapper.selectCount(wrapper);
    }

    @Override
    public List<PostListVO> getUserPosts(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getAuthorId, userId);
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> result = postMapper.selectPage(new Page<>(page, size), wrapper);
        return buildListVO(result.getRecords(), userId);
    }

    @Override
    public List<PostListVO> getUserPublicPosts(Long authorId, Long viewerId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getAuthorId, authorId)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime);

        Page<Post> result = postMapper.selectPage(new Page<>(page, size), wrapper);
        return buildListVO(result.getRecords(), viewerId);
    }

    @Override
    public List<PostListVO> getHotPosts(Integer limit, String category) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Post::getCategory, category);
        }
        wrapper.orderByDesc(Post::getViewCount)
                .orderByDesc(Post::getLikeCount)
                .last("LIMIT " + Math.min(Math.max(limit, 1), 100));

        List<Post> posts = postMapper.selectList(wrapper);
        return buildListVO(posts, null);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        checkPostExists(postId);
        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));

        boolean liked;
        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            updatePostCount(postId, "like_count", -1);
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            updatePostCount(postId, "like_count", 1);
            liked = true;
        }

        int count = Optional.ofNullable(postMapper.selectById(postId).getLikeCount()).orElse(0);
        return Map.of("liked", liked, "likeCount", count);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleCollect(Long userId, Long postId) {
        checkPostExists(postId);
        PostCollect existing = postCollectMapper.selectOne(new LambdaQueryWrapper<PostCollect>()
                .eq(PostCollect::getPostId, postId)
                .eq(PostCollect::getUserId, userId));

        boolean collected;
        if (existing != null) {
            postCollectMapper.deleteById(existing.getId());
            updatePostCount(postId, "collect_count", -1);
            collected = false;
        } else {
            PostCollect collect = new PostCollect();
            collect.setPostId(postId);
            collect.setUserId(userId);
            postCollectMapper.insert(collect);
            updatePostCount(postId, "collect_count", 1);
            collected = true;
        }

        int count = Optional.ofNullable(postMapper.selectById(postId).getCollectCount()).orElse(0);
        return Map.of("collected", collected, "collectCount", count);
    }

    @Override
    public List<CommentVO> getComments(Long postId) {
        List<PostComment> comments = postCommentMapper.selectList(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .orderByDesc(PostComment::getCreateTime)
        );
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = comments.stream().map(PostComment::getUserId).distinct().toList();
        Map<Long, PostListVO.AuthorVO> authorMap = fetchAuthors(userIds);

        return comments.stream().map(comment -> CommentVO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .author(authorMap.get(comment.getUserId()))
                .build()).toList();
    }

    @Override
    @Transactional
    public CommentVO createComment(Long userId, Long postId, CommentCreateRequest request) {
        checkPostExists(postId);

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        postCommentMapper.insert(comment);

        updatePostCount(postId, "comment_count", 1);

        Map<Long, PostListVO.AuthorVO> authorMap = fetchAuthors(List.of(userId));
        return CommentVO.builder()
                .id(comment.getId())
                .postId(postId)
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .author(authorMap.get(userId))
                .build();
    }

    private void checkPostExists(Long postId) {
        if (postMapper.selectById(postId) == null) {
            throw BusinessException.notFound("帖子不存在");
        }
    }

    private void updatePostCount(Long postId, String column, int delta) {
        String sql = column + " = GREATEST(COALESCE(" + column + ", 0) + " + delta + ", 0)";
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql(sql));
    }

    /** 标签 ID 与标签名合并，名称不存在时自动创建标签 */
    private List<Long> resolveTagIds(List<Long> tagIds, List<String> tagNames) {
        Set<Long> result = new LinkedHashSet<>();
        if (tagIds != null) {
            result.addAll(tagIds);
        }
        if (tagNames != null) {
            for (String name : tagNames) {
                if (StrUtil.isBlank(name)) {
                    continue;
                }
                String trimmed = name.trim();
                Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, trimmed));
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(trimmed);
                    tag.setPostCount(0);
                    tagMapper.insert(tag);
                }
                result.add(tag.getId());
            }
        }
        return new ArrayList<>(result);
    }

    private void bindTags(Long postId, List<Long> tagIds) {
        List<Tag> validTags = tagMapper.selectBatchIds(tagIds);
        for (Tag tag : validTags) {
            PostTag pt = new PostTag();
            pt.setPostId(postId);
            pt.setTagId(tag.getId());
            postTagMapper.insert(pt);
            incrementTagPostCount(tag.getId());
        }
    }

    private void incrementTagPostCount(Long tagId) {
        tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .setSql("post_count = COALESCE(post_count, 0) + 1"));
    }

    private void decrementTagPostCount(Long tagId) {
        tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .setSql("post_count = GREATEST(COALESCE(post_count, 0) - 1, 0)"));
    }

    private List<Long> getPostTagIds(Long postId) {
        List<PostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId)
        );
        return postTags.stream().map(PostTag::getTagId).toList();
    }

    private Map<Long, List<TagVO>> fetchTagsForPosts(List<Long> postIds) {
        if (postIds.isEmpty()) return Collections.emptyMap();

        List<PostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<PostTag>().in(PostTag::getPostId, postIds)
        );
        if (postTags.isEmpty()) return Collections.emptyMap();

        List<Long> tagIds = postTags.stream().map(PostTag::getTagId).distinct().toList();
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        Map<Long, TagVO> tagMap = tags.stream()
                .collect(Collectors.toMap(Tag::getId, TagVO::fromEntity));

        Map<Long, List<TagVO>> result = new java.util.HashMap<>();
        for (PostTag pt : postTags) {
            result.computeIfAbsent(pt.getPostId(), k -> new ArrayList<>())
                    .add(tagMap.get(pt.getTagId()));
        }
        return result;
    }

    private Map<Long, PostListVO.AuthorVO> fetchAuthors(List<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();

        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(
                User::getId,
                u -> PostListVO.AuthorVO.builder()
                        .id(u.getId())
                        .nickname(u.getNickname())
                        .avatar(u.getAvatar())
                        .build()
        ));
    }

    private PostListVO.AuthorVO withFollowing(PostListVO.AuthorVO author, Set<Long> followedAuthorIds) {
        if (author == null) return null;
        author.setFollowing(followedAuthorIds.contains(author.getId()));
        return author;
    }

    private List<PostListVO> buildListVO(List<Post> posts, Long userId) {
        if (posts.isEmpty()) return Collections.emptyList();

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> userIds = posts.stream().map(Post::getAuthorId).distinct().toList();

        Map<Long, List<TagVO>> tagsMap = fetchTagsForPosts(postIds);
        Map<Long, PostListVO.AuthorVO> authorMap = fetchAuthors(userIds);

        Set<Long> likedPostIds = Collections.emptySet();
        Set<Long> collectedPostIds = Collections.emptySet();
        Set<Long> followedAuthorIds = Collections.emptySet();
        if (userId != null) {
            likedPostIds = postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                            .in(PostLike::getPostId, postIds)
                            .eq(PostLike::getUserId, userId))
                    .stream().map(PostLike::getPostId).collect(Collectors.toSet());
            collectedPostIds = postCollectMapper.selectList(new LambdaQueryWrapper<PostCollect>()
                            .in(PostCollect::getPostId, postIds)
                            .eq(PostCollect::getUserId, userId))
                    .stream().map(PostCollect::getPostId).collect(Collectors.toSet());
            followedAuthorIds = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                            .in(UserFollow::getFollowingId, userIds)
                            .eq(UserFollow::getFollowerId, userId))
                    .stream().map(UserFollow::getFollowingId).collect(Collectors.toSet());
        }

        Set<Long> finalLikedPostIds = likedPostIds;
        Set<Long> finalCollectedPostIds = collectedPostIds;
        Set<Long> finalFollowedAuthorIds = followedAuthorIds;
        return posts.stream().map(post -> PostListVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .cover(post.getCover())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .collectCount(post.getCollectCount())
                .shareCount(post.getShareCount())
                .status(post.getStatus())
                .liked(finalLikedPostIds.contains(post.getId()))
                .collected(finalCollectedPostIds.contains(post.getId()))
                .createTime(post.getCreateTime())
                .author(withFollowing(authorMap.get(post.getAuthorId()), finalFollowedAuthorIds))
                .tags(tagsMap.getOrDefault(post.getId(), Collections.emptyList()))
                .build()).toList();
    }

    private PostDetailVO buildDetailVO(Post post) {
        User author = userMapper.selectById(post.getAuthorId());
        PostListVO.AuthorVO authorVO = author != null
                ? PostListVO.AuthorVO.builder()
                        .id(author.getId())
                        .nickname(author.getNickname())
                        .avatar(author.getAvatar())
                        .build()
                : null;

        List<Long> tagIds = getPostTagIds(post.getId());
        List<TagVO> tags = tagIds.isEmpty()
                ? Collections.emptyList()
                : tagMapper.selectBatchIds(tagIds).stream().map(TagVO::fromEntity).toList();

        return PostDetailVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .cover(post.getCover())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .collectCount(post.getCollectCount())
                .shareCount(post.getShareCount())
                .status(post.getStatus())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .author(authorVO)
                .tags(tags)
                .build();
    }
}
