package com.legalknowledge.service;

import com.legalknowledge.dto.response.UserInfoDTO;
import com.legalknowledge.mapper.UserMapper;
import com.legalknowledge.entity.User;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<UserInfoDTO> listAll() {
        return userMapper.selectList(null)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserInfoDTO updateRole(Long userId, String newRole) {
        if (!User.ROLE_ADMIN.equals(newRole) && !User.ROLE_USER.equals(newRole)) {
            throw new RuntimeException("无效角色：" + newRole);
        }
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setRole(newRole);
        userMapper.updateById(user);
        return toDTO(user);
    }

    private UserInfoDTO toDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().format(FMT) : null);
        return dto;
    }
}
