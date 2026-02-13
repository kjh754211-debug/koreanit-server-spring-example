package com.koreanit.spring.post;

import java.util.List;

import org.springframework.stereotype.Service;

import com.koreanit.spring.comment.error.ApiException;
import com.koreanit.spring.comment.error.ErrorCode;

@Service
public class PostService {

    private static final int MAX_LIMIT = 1000;

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    private int normalizeLimit(int limit) {
    if (limit <= 0) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "limit 값이 유효하지 않습니다");
    }
      return Math.min(limit, MAX_LIMIT);
    }

    private int normalizePage(int page) {
      if (page <= 0) {
        throw new ApiException(ErrorCode.INVALID_REQUEST, "page 값이 유효하지 않습니다");
      }
      return page;
    }

    public Post create(long userId, String title, String content) {
        long id = postRepository.save(userId, title, content);
        return PostMapper.toDomain(postRepository.findById(id));
    }

    public List<Post> list(int page, int limit) {

      page = normalizePage(page);
      limit = normalizeLimit(limit);

      int offset = (page - 1) * limit;

      return PostMapper.toDomainList(postRepository.findAll(offset, normalizeLimit(limit)));
    }

    public Post get(long id) {
       int updated = postRepository.increaseViewCount(id);
    if (updated == 0) {
        throw new ApiException(
            ErrorCode.NOT_FOUND_RESOURCE,
            "존재하지 않는 게시글입니다. id=" + id
        );
    }
        return PostMapper.toDomain(postRepository.findById(id));
    }

    public Post update(long id, String title, String content) {
        postRepository.update(id, title, content);
        return PostMapper.toDomain(postRepository.findById(id));
    }

    public void delete(long id) {
        postRepository.delete(id);
    }
}