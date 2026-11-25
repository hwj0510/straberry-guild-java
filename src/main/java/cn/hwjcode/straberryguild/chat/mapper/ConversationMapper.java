package cn.hwjcode.straberryguild.chat.mapper;

import cn.hwjcode.straberryguild.pojo.entity.Conversation;
import cn.hwjcode.straberryguild.pojo.vo.ConversationVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 查询当前用户的会话列表（聚合对方信息、最后消息、未读数），按最后消息时间倒序
     */
    @Select("""
            SELECT
              c.id AS conversation_id,
              c.last_message_content AS last_message,
              c.last_message_time AS last_message_time,
              CASE WHEN c.user_a_id = #{me} THEN c.user_b_id ELSE c.user_a_id END AS peer_id,
              u.nickname AS peer_name,
              u.avatar AS peer_avatar,
              (SELECT COUNT(*) FROM private_message m
                 WHERE m.conversation_id = c.id
                   AND m.to_user_id = #{me} AND m.is_read = 0) AS unread
            FROM conversation c
            LEFT JOIN sys_user u
              ON u.id = (CASE WHEN c.user_a_id = #{me} THEN c.user_b_id ELSE c.user_a_id END)
             AND u.deleted = 0
            WHERE c.user_a_id = #{me} OR c.user_b_id = #{me}
            ORDER BY COALESCE(c.last_message_time, c.updated_at, c.created_at) DESC, c.id DESC
            """)
    List<ConversationVO> selectConversations(@Param("me") Long me);
}
