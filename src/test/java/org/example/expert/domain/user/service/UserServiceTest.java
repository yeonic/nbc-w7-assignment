package org.example.expert.domain.user.service;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  @Test
  void 새_비밀번호가_기존_비밀번호와_같을_시_예외_발생() {
    // given
    String newPassword = "newPassword";
    User findUser = new User("e@e.com", newPassword, UserRole.USER);
    UserChangePasswordRequest request =
            new UserChangePasswordRequest(newPassword, newPassword);

    given(passwordEncoder.matches(eq(newPassword), eq(findUser.getPassword()))).willReturn(true);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(findUser));

    // when
    // then
    assertThatThrownBy(() -> userService.changePassword(1L, request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
  }

  @Test
  void 기존_비밀번호_인증에_실패할_시_예외_발생() {
    // given
    String newPassword = "newPassword";
    String currentPassword = "currentPassword";
    User findUser = new User("e@e.com", currentPassword, UserRole.USER);
    UserChangePasswordRequest request =
            new UserChangePasswordRequest(newPassword, newPassword);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(findUser));
    given(passwordEncoder.matches(eq(request.getOldPassword()), eq(findUser.getPassword())))
            .willReturn(false);

    // when
    // then
    assertThatThrownBy(() -> userService.changePassword(1L, request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("잘못된 비밀번호입니다.");

  }
}