package cn.hwjcode.straberryguild.post.mapper;

import cn.hwjcode.straberryguild.pojo.entity.PostLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 帖子点赞 Mapper */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
}
