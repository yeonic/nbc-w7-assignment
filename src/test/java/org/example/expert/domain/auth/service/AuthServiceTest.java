package org.example.expert.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private AuthService authService;

  @Test
  void 회원가입이_정상적으로_처리됨() {
    // given
    SignupRequest request = new SignupRequest("example@example.com", "1234", "USER");
    String hashedPassword = "hashed1234";
    String jwtToken = "jwtToken";

    given(passwordEncoder.encode(anyString())).willReturn(hashedPassword);

    given(jwtUtil.createToken(anyLong(), anyString(), any())).willReturn(jwtToken);

    given(userRepository.save(any())).willAnswer(invocation -> {
      User savedUser = invocation.getArgument(0);
      ReflectionTestUtils.setField(savedUser, "id", 1L);
      return savedUser;
    });

    // when
    SignupResponse response = authService.signup(request);

    // then
    assertThat(response.getBearerToken()).isEqualTo(jwtToken);
  }

  @Test
  void 회원가입시_이미_이메일이_존재하면_예외를_던진다() {
    // given
    SignupRequest request = new SignupRequest("example@example.com", "1234", "USER");
    given(userRepository.existsByEmail(anyString())).willReturn(true);

    // when
    // then
    assertThatThrownBy(() -> authService.signup(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("이미 존재하는 이메일입니다.");
  }

  @Test
  void 로그인이_정상적으로_처리됨() {
    // given
    SigninRequest request = new SigninRequest("example@example.com", "1234");
    String jwtToken = "jwtToken";

    given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
    given(jwtUtil.createToken(anyLong(), anyString(), any())).willReturn(jwtToken);
    given(userRepository.findByEmail(anyString())).willAnswer(invoction -> {
      User findUser = new User("example@example.com", "1234", UserRole.USER);
      ReflectionTestUtils.setField(findUser, "id", 1L);
      return Optional.of(findUser);
    });

    // when
    SigninResponse response = authService.signin(request);

    // then
    assertThat(response.getBearerToken()).isEqualTo(jwtToken);
  }

  @Test
  void 로그인_중_비밀번호가_일치하지_않으면_예외_발생() {
    // given
    SigninRequest request = new SigninRequest("example@example.com", "1234");

    given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
    given(userRepository.findByEmail(anyString())).willAnswer(invoction -> {
      User findUser = new User("example@example.com", "1234", UserRole.USER);
      ReflectionTestUtils.setField(findUser, "id", 1L);
      return Optional.of(findUser);
    });

    // when
    // then
    assertThatThrownBy(() -> authService.signin(request))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("잘못된 비밀번호입니다.");
  }
}