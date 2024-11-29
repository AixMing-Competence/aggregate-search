package com.aixming.aggregatesearch.job.cycle;

import com.aixming.aggregatesearch.esdao.PostEsDao;
import com.aixming.aggregatesearch.mapper.PostMapper;
import com.aixming.aggregatesearch.model.dto.post.PostEsDTO;
import com.aixming.aggregatesearch.model.entity.Post;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 增量同步帖子到 es
 *
 * @author AixMing
 * @since 2024-11-29 14:45:24
 */
// @Component
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void run() {
        // 查询近 5 分钟的数据
        Date fiveMinuteDate = new Date(System.currentTimeMillis() - 5 * 60 * 1000L);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Post::getUpdateTime, fiveMinuteDate);
        List<Post> postList = postMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(postList)) {
            log.info("no inc data.");
            return;
        }
        // 转换成 es 中存储的对象
        List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).toList();
        // 批量存入 es 中
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("IncSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }

}
