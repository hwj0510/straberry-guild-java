package cn.hwjcode.straberryguild.chat.mapper;

import cn.hwjcode.straberryguild.pojo.entity.PrivateMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 查询当前用户与某 peer 的消息（按时间倒序取一页，调用方再翻转为正序展示）
     */
    @Select("""
            SELECT * FROM private_message
            WHERE (from_user_id = #{me} AND to_user_id = #{peer})
               OR (from_user_id = #{peer} AND to_user_id = #{me})
            ORDER BY created_at DESC, id DESC
            LIMIT #{offset}, #{size}
            """)
    List<PrivateMessage> selectConversationMessages(@Param("me") Long me,
                                                    @Param("peer") Long peer,
                                                    @Param("offset") long offset,
                                                    @Param("size") int size);

    /** 当前用户未读消息总数 */
    @Select("SELECT COUNT(*) FROM private_message WHERE to_user_id = #{me} AND is_read = 0")
    long countUnread(@Param("me") Long me);

    /** 标记当前用户收到的来自 peer 的消息为已读 */
    @Update("UPDATE private_message SET is_read = 1 WHERE to_user_id = #{me} AND from_user_id = #{peer} AND is_read = 0")
    int markRead(@Param("me") Long me, @Param("peer") Long peer);
}
