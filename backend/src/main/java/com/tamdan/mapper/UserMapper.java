package com.tamdan.mapper;

import com.tamdan.dto.request.AdminUserRequest;
import com.tamdan.dto.request.ManagedUserVM;
import com.tamdan.dto.response.AdminUserDTO;
import com.tamdan.dto.response.UserDTO;
import com.tamdan.entities.Authority;
import com.tamdan.entities.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toResponse(User user) {
        return new UserDTO(user.getId(), user.getLogin());
    }

    public AdminUserDTO toAdminResponse(User user) {
        return new AdminUserDTO(
            user.getId(), user.getLogin(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getImageUrl(),
            user.isActivated(), user.getLangKey(), user.getCreatedBy(), user.getCreatedDate(), user.getLastModifiedBy(),
            user.getLastModifiedDate(),
            user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toUnmodifiableSet())
        );
    }

    public User toEntity(AdminUserRequest request, String encodedPassword) {
        return User.builder()
            .id(request.id()).login(request.login()).firstName(request.firstName()).lastName(request.lastName())
            .email(request.email()).imageUrl(request.imageUrl()).activated(request.activated()).langKey(request.langKey())
            .password(encodedPassword).build();
    }

    public User toEntity(ManagedUserVM request, String encodedPassword) {
        return User.builder()
            .login(request.login()).firstName(request.firstName()).lastName(request.lastName()).email(request.email())
            .imageUrl(request.imageUrl()).langKey(request.langKey()).password(encodedPassword).build();
    }

}
