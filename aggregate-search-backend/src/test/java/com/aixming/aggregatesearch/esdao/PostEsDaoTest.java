package com.aixming.aggregatesearch.esdao;

import com.aixming.aggregatesearch.model.dto.post.PostEsDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author AixMing
 * @since 2024-11-26 21:46:37
 */
@SpringBootTest
public class PostEsDaoTest {

    @Resource
    private PostEsDao postEsDao;
    
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    void testAdd() {
        PostEsDTO postEsDTO = new PostEsDTO();
        postEsDTO.setId(2L);
        postEsDTO.setTitle("aixming competence");
        postEsDTO.setContent("一名冉冉升起的天才新星");
        postEsDTO.setTags(Arrays.asList("java", "javascript"));
        postEsDTO.setUserId(1L);
        postEsDTO.setCreateTime(new Date());
        postEsDTO.setUpdateTime(new Date());
        postEsDTO.setIsDelete(0);
        postEsDao.save(postEsDTO);
        System.out.println(postEsDTO.getId());
    }

    @Test
    void testSelect() {
        List<PostEsDTO> postEsDTOList = postEsDao.findAll(PageRequest.of(0, 5, Sort.by("createTime"))).getContent();
        // Optional<PostEsDTO> byId = postEsDao.findById(1L);
        System.out.println(postEsDTOList);
    }

    @Test
    void testDelete() {
        postEsDao.deleteById(2L);
    }

    @Test
    void testFindBy() {
        List<PostEsDTO> postEsDTOList = postEsDao.findByContent("升起");
        System.out.println(postEsDTOList);
    }

    @Test
    void testSimpleSearch() {
        
    }
}
