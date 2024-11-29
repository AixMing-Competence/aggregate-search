package com.aixming.aggregatesearch.job.once;

import com.aixming.aggregatesearch.esdao.PostEsDao;
import com.aixming.aggregatesearch.mapper.PostMapper;
import com.aixming.aggregatesearch.model.dto.post.PostEsDTO;
import com.aixming.aggregatesearch.model.entity.Post;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

/**
 * 全量同步帖子到 es
 *
 * @author AixMing
 * @since 2024-11-29 16:04:12
 */
// @Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 项目启动时执行一次
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) {
        // 从数据库中查出所有数据
        List<Post> postList = postMapper.selectList(null);
        if (CollectionUtils.isEmpty(postList)) {
            log.info("no full sync post data.");
            return;
        }
        // 转换插入到 es 中的数据对象
        List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).toList();
        // 批量插入到 es 中
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("FullSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("FullSyncPostToEs end.");
    }
}
