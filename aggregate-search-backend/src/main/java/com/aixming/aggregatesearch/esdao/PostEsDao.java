package com.aixming.aggregatesearch.esdao;

import com.aixming.aggregatesearch.model.dto.post.PostEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * ES 帖子操作
 *
 * @author AixMing
 * @since 2024-11-26 21:36:23
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {
    List<PostEsDTO> findByUserId(Long userId);
    
    List<PostEsDTO> findByTitle(String title);
    
    List<PostEsDTO> findByContent(String content);
}
