package com.koreanit.spring.user;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.koreanit.spring.comment.error.ApiException;
import com.koreanit.spring.comment.error.ErrorCode;

@Service
public class UserService {

  private static final int MAX_LIMIT = 1000;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  private int normalizeLimit(int limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException("limit 은 1 이상 입력해주세요");
    }
    return Math.min(limit, MAX_LIMIT);
  }

  private String toDuplicateMessage(DuplicateKeyException e) {
    String m = (e.getMessage() == null) ? "" : e.getMessage();

    // MySQL 기준: "Duplicate entry ... for key '...'"
    // DB/드라이버에 따라 메시지 포맷은 달라질 수 있으므로
    // key 이름 기반으로만 판단한다.
    if (m.contains("for key") && (m.contains("users.username") || m.contains("'username'") || m.contains("username"))) {
      return "이미 존재하는 username입니다";
    }
    if (m.contains("for key") && (m.contains("users.email") || m.contains("'email'") || m.contains("email"))) {
      return "이미 존재하는 email입니다";
    }

    return "이미 존재하는 값입니다";
  }

  public Long create(String username, String password, String nickname, String email) {
    username = username.trim().toLowerCase();
    nickname = nickname.trim().toLowerCase();

    String normalizedEmail = (email == null) ? null : email.toLowerCase();

    String hash = passwordEncoder.encode(password);
    try {
      return userRepository.save(username, hash, nickname,
          normalizedEmail);
    } catch (DuplicateKeyException e) {
      throw new ApiException(ErrorCode.DUPLICATE_RESOURCE,
          toDuplicateMessage(e));
    }
  }

  public User get(Long id) {
    try {
      UserEntity e = userRepository.findById(id);
      return UserMapper.toDomain(e);

    } catch (EmptyResultDataAccessException e) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  public List<User> list(int limit) {
    int safeLimit = normalizeLimit(limit);
    return UserMapper.toDomainList(userRepository.findAll(safeLimit));
  }

  public void changeNickname(Long id, String nickname) {
    nickname = nickname.trim().toLowerCase();

    // 1) 대상 존재 여부 확인 (없으면 여기서 404)
    User user = get(id);

    // 2) 값이 동일하면 변경 없음 → 정상 처리
    if (user.getNickname().equals(nickname)) {
      return;
    }

    // 3) 실제 변경
    int updated = userRepository.updateNickname(id, nickname);

    if (updated == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public void changePassword(Long id, String password) {
    password = password.trim().toLowerCase();
    User user = get(id);
    boolean ok = passwordEncoder.matches(password, user.getPassword());

    if (!ok) {
      throw new ApiException(
          ErrorCode.INVALID_REQUEST,
          "기존 비밀번호가 일치하지 않습니다");
    }
  }

  public void delete(Long id) {
    int deleted = userRepository.deleteById(id);

    if (deleted == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  public Long login(String username, String password) {
    try {
      UserEntity e = userRepository.findByUsername(username);
      boolean ok = passwordEncoder.matches(password, e.getPassword());
      if (!ok) {
        throw new ApiException(ErrorCode.INTERNAL_ERROR, "비밀번호 검증 실패");
      }

      return e.getId();
    } catch (EmptyResultDataAccessException e) {
      throw new ApiException(ErrorCode.NOT_FOUND_RESOURCE, "존재하지 않는 사용자입니다");
    }

  }

  public void changeEmail(Long id, String email) {
    // TODO Auto-generated method stub
    email = email.trim().toLowerCase();
    userRepository.updateEmail(id, email);
  }

  public boolean isSelf(Long userId) {
    Long currentUserId = SecurityUtils.currentUserId();
    return currentUserId != null && userId != null && currentUserId.equals(userId);

  }

}