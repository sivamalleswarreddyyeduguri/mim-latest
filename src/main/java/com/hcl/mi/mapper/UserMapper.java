package com.hcl.mi.mapper;

import org.springframework.stereotype.Component;

import com.hcl.mi.entities.User;
import com.hcl.mi.requestdtos.NewUser;

@Component
public class UserMapper {

    public static NewUser convertEntityToDto(User user) {
        NewUser dto = new NewUser();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword()); 
        dto.setEmail(user.getEmail());
        dto.setMobileNum(user.getMobileNum());
        dto.setRole(user.getRole());
        return dto;
    }

    public static User convertDtoToEntity(NewUser dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setMobileNum(dto.getMobileNum());
        user.setRole(dto.getRole());
        return user;
    }
}