package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.annotation.Admin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

  private final CommentRepository commentRepository;

  @Admin
  @Transactional
  public void deleteComment(long commentId) {
    commentRepository.deleteById(commentId);
  }
}
