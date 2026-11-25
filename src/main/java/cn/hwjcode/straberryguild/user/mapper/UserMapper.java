package cn.hwjcode.straberryguild.user.mapper;

import cn.hwjcode.straberryguild.pojo.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * MyBatis-Plus 自动提供 CRUD 方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 可以在这里定义自定义 SQL 方法
}
