package org.example.expert.domain.manager.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
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
class ManagerServiceTest {

  @Mock
  private ManagerRepository managerRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private TodoRepository todoRepository;
  @InjectMocks
  private ManagerService managerService;

  @Test
  public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
    // given
    long todoId = 1L;
    given(todoRepository.findById(todoId)).willReturn(Optional.empty());

    // when & then
    InvalidRequestException exception = assertThrows(InvalidRequestException.class,
            () -> managerService.getManagers(todoId));
    assertEquals("Todo not found", exception.getMessage());
  }

  @Test
  void todo의_user가_null인_경우_예외가_발생한다() {
    // given
    AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
    long todoId = 1L;
    long managerUserId = 2L;

    Todo todo = new Todo();
    ReflectionTestUtils.setField(todo, "user", null);

    ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

    // when & then
    InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
    );

    assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
  }

  @Test
  void 담당자_등록을_시도하는_유저가_작성자가_아니면_예외가_발생한다() {
    // given
    Long writerUserId = 1L;
    Long managerUserId = 2L;
    Long todoId = 1L;

    AuthUser authUser = new AuthUser(managerUserId, "e@e.com", UserRole.USER);
    User writer = new User();
    ReflectionTestUtils.setField(writer, "id", writerUserId);

    Todo todo = new Todo("제목", "내용", "Sunny", writer);
    ReflectionTestUtils.setField(todo, "id", todoId);

    ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);

    given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));

    // when
    // then
    assertThatThrownBy(() -> managerService.saveManager(authUser, todoId, request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.");
  }

  @Test
  void 본인을_매니저로_지정할_경우_예외가_발생한다() {
    // given
    AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
    long todoId = 1L;
    long managerUserId = 1L;

    ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

    User user = new User();
    ReflectionTestUtils.setField(user, "id", 1L);

    Todo todo = new Todo();
    ReflectionTestUtils.setField(todo, "user", user);

    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
    given(userRepository.findById(managerUserId)).willReturn(Optional.of(user));

    // when & then
    assertThatThrownBy(() -> managerService.saveManager(authUser, todoId, managerSaveRequest))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
  }

  @Test
  void 매니저로_정상_등록된다() {
    // given
    AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
    long todoId = 1L;
    long managerUserId = 2L;

    ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);

    User user = User.fromAuthUser(authUser);
    User managerUser = new User();
    ReflectionTestUtils.setField(managerUser, "id", managerUserId);

    Todo todo = new Todo();
    ReflectionTestUtils.setField(todo, "user", user);

    Manager savedManager = new Manager(managerUser, todo);
    ReflectionTestUtils.setField(savedManager, "id", 1L);

    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
    given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
    given(managerRepository.save(any())).willReturn(savedManager);

    // when
    ManagerSaveResponse response = managerService.saveManager(authUser, todoId, request);

    // then
    Assertions.assertThat(response.getId()).isEqualTo(savedManager.getId());
    Assertions.assertThat(response.getUser().getId()).isEqualTo(managerUser.getId());
    Assertions.assertThat(response.getUser().getEmail()).isEqualTo(managerUser.getEmail());
  }

  @Test // 테스트코드 샘플
  public void manager_목록_조회에_성공한다() {
    // given
    long todoId = 1L;
    User user = new User("user1@example.com", "password", UserRole.USER);
    Todo todo = new Todo("Title", "Contents", "Sunny", user);
    ReflectionTestUtils.setField(todo, "id", todoId);

    Manager mockManager = new Manager(todo.getUser(), todo);
    List<Manager> managerList = List.of(mockManager);

    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
    given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

    // when
    List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

    // then
    assertEquals(1, managerResponses.size());
    assertEquals(mockManager.getId(), managerResponses.get(0).getId());
    assertEquals(mockManager.getUser().getEmail(),
            managerResponses.get(0).getUser().getEmail());
  }

  @Test
    // 테스트코드 샘플
  void todo가_정상적으로_등록된다() {
    // given
    AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
    User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

    long todoId = 1L;
    Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

    long managerUserId = 2L;
    User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
    ReflectionTestUtils.setField(managerUser, "id", managerUserId);

    ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(
            managerUserId); // request dto 생성

    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
    given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
    given(managerRepository.save(any(Manager.class))).willAnswer(
            invocation -> invocation.getArgument(0));

    // when
    ManagerSaveResponse response = managerService.saveManager(authUser, todoId,
            managerSaveRequest);

    // then
    assertNotNull(response);
    assertEquals(managerUser.getId(), response.getUser().getId());
    assertEquals(managerUser.getEmail(), response.getUser().getEmail());
  }

  @Test
  void 매니저_삭제시_todo의_user가_null인_경우_예외가_발생한다() {
    // given
    long userId = 1L;
    long todoId = 1L;

    AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

    Todo todo = new Todo();
    ReflectionTestUtils.setField(todo, "user", null);

    given(userRepository.findById(userId)).willReturn(Optional.of(User.fromAuthUser(authUser)));
    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

    // when & then
    assertThatThrownBy(() -> managerService.deleteManager(authUser.getId(), todoId, 2L))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("해당 일정을 만든 유저가 유효하지 않습니다.");
  }

  @Test
  void 매니저_삭제_시도_유저가_작성자가_아니면_예외가_발생() {
    // given
    long userId = 1L;
    long writerId = 2L;
    long todoId = 1L;

    AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

    User writer = new User();
    ReflectionTestUtils.setField(writer, "id", writerId);

    Todo todo = new Todo();
    ReflectionTestUtils.setField(todo, "user", writer);

    given(userRepository.findById(userId)).willReturn(Optional.of(User.fromAuthUser(authUser)));
    given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

    // when & then
    assertThatThrownBy(() -> managerService.deleteManager(authUser.getId(), todoId, 2L))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("해당 일정을 만든 유저가 유효하지 않습니다.");
  }

  @Test
  void 담당자가_아닌_유저를_해제하면_예외가_발생() {
    // given
    long userId = 1L;
    long managerUserId = 2L;
    long todoId = 1L;

    AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

    User writer = new User();
    ReflectionTestUtils.setField(writer, "id", userId);

    User managerUser = new User();
    ReflectionTestUtils.setField(managerUser, "id", managerUserId);

    Todo todo = new Todo();
    ReflectionTestUtils.setField(todo, "user", writer);
    ReflectionTestUtils.setField(todo, "id", todoId);

    Todo anotherTodo = new Todo();
    ReflectionTestUtils.setField(todo, "id", 3L);

    Manager manager = new Manager(managerUser, anotherTodo);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(User.fromAuthUser(authUser)));
    given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
    given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

    // when & then
    assertThatThrownBy(() -> managerService.deleteManager(authUser.getId(), todoId, 2L))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("해당 일정에 등록된 담당자가 아닙니다.");
  }
}
