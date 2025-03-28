package com.aixming.aggregatesearch.datasource;

import com.aixming.aggregatesearch.model.dto.post.PostQueryRequest;
import com.aixming.aggregatesearch.model.entity.Post;
import com.aixming.aggregatesearch.model.vo.PostVO;
import com.aixming.aggregatesearch.service.PostService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 帖子数据源
 *
 * @author AixMing
 */
@Service
@RequiredArgsConstructor
@SearchConfig(type = "post")
public class PostDataSource implements DataSource<PostVO> {

    private final PostService postService;

    @Override
    public Page<PostVO> doSearch(String searchText, int current, int pageSize, HttpServletRequest request) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(current);
        postQueryRequest.setPageSize(pageSize);
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        return postService.getPostVOPage(postPage, request);
    }

}
