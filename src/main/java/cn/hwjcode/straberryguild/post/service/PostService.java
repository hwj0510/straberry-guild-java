package cn.hwjcode.straberryguild.post.service;

import cn.hwjcode.straberryguild.pojo.dto.CommentCreateRequest;
import cn.hwjcode.straberryguild.pojo.vo.CommentVO;
import cn.hwjcode.straberryguild.pojo.dto.PostCreateRequest;
import cn.hwjcode.straberryguild.pojo.vo.PostDetailVO;
import cn.hwjcode.straberryguild.pojo.vo.PostListVO;
import cn.hwjcode.straberryguild.pojo.dto.PostUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * 帖子服务接口
 */
public interface PostService {

    PostDetailVO createPost(Long userId, PostCreateRequest request);

    PostDetailVO updatePost(Long userId, Long postId, PostUpdateRequest request);

    void deletePost(Long userId, Long postId);

    /** 根据 ID 获取帖子详情（会增加浏览量） */
    PostDetailVO getPostDetail(Long postId);

    /** 获取帖子列表，支持分类、标签、关键词、关注流筛选和排序（latest/hot） */
    List<PostListVO> getPostList(Long userId, Integer page, Integer size, Long tagId,
                                 String keyword, String category, String feed, String sort);

    /** 统计帖子数量，支持按分类筛选 */
    long countPosts(String category);

    List<PostListVO> getHotPosts(Integer limit, String category);

    List<PostListVO> getUserPosts(Long userId, Integer page, Integer size);

    /** 获取指定用户公开发布的帖子（status=1），viewerId 用于计算点赞/收藏/关注态，可为 null */
    List<PostListVO> getUserPublicPosts(Long authorId, Long viewerId, Integer page, Integer size);

    /** 点赞/取消点赞 */
    Map<String, Object> toggleLike(Long userId, Long postId);

    /** 收藏/取消收藏 */
    Map<String, Object> toggleCollect(Long userId, Long postId);

    List<CommentVO> getComments(Long postId);

    CommentVO createComment(Long userId, Long postId, CommentCreateRequest request);
}
