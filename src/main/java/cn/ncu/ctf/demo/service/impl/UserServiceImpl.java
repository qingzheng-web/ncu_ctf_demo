package cn.ncu.ctf.demo.service.impl;

import cn.ncu.ctf.demo.entities.User;
import cn.ncu.ctf.demo.mapper.UserMapper;
import cn.ncu.ctf.demo.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

/**
 * @Author: jinge
 * @Date: 2022/10/25 21:38
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //Q1ng 2023/8/15 16:40
    @Override
    public PageInfo<User> listWithPage(Integer pageNum, Integer pageSize) {
        return null;
    }
}
