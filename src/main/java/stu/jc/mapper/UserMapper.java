package stu.jc.mapper;

import org.apache.ibatis.annotations.Param;
import stu.jc.entity.User;

public interface UserMapper {
    User findUserById(@Param("id") Integer id);
}
