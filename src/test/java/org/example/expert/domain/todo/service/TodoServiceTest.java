package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

  @Mock
  private WeatherClient weatherClient;

  @Mock
  private TodoRepository todoRepository;

  @InjectMocks
  private TodoService todoService;

  @Test
  void Todo가_정상적으로_등록된다() {
    // given
    AuthUser authUser = new AuthUser(1L, "ex@ex.com", UserRole.USER);
    TodoSaveRequest todoSaveRequest = new TodoSaveRequest("제목", "내용");
    String todayWeather = "Sunny";
    Todo todo = new Todo(
            todoSaveRequest.getTitle(), todoSaveRequest.getContents(), todayWeather,
            User.fromAuthUser(authUser));

    given(todoRepository.save(any())).willReturn(todo);
    given(weatherClient.getTodayWeather()).willReturn(todayWeather);

    // when
    TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

    // then
    assertThat(todoSaveResponse.getTitle()).isEqualTo(todoSaveRequest.getTitle());
    assertThat(todoSaveResponse.getContents()).isEqualTo(todoSaveRequest.getContents());
    assertThat(todoSaveResponse.getWeather()).isEqualTo(todayWeather);
    assertThat(todoSaveResponse.getUser().getId()).isEqualTo(authUser.getId());
    assertThat(todoSaveResponse.getUser().getEmail()).isEqualTo(authUser.getEmail());
  }
}