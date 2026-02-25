package com.classpets.backend.auth.security;

import com.classpets.backend.entity.Teacher;
import com.classpets.backend.mapper.TeacherMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TeacherMapper teacherMapper;

    public UserDetailsServiceImpl(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUsername, username)
                .last("limit 1"));

        if (teacher == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new User(
                teacher.getUsername(),
                teacher.getPasswordHash() != null ? teacher.getPasswordHash() : "",
                teacher.getStatus() == null || teacher.getStatus() == 1,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")));
    }
}
