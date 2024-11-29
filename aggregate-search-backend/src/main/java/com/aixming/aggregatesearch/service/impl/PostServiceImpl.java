package com.aixming.aggregatesearch.service.impl;

import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.constant.CommonConstant;
import com.aixming.aggregatesearch.exception.BusinessException;
import com.aixming.aggregatesearch.exception.ThrowUtils;
import com.aixming.aggregatesearch.mapper.PostFavourMapper;
import com.aixming.aggregatesearch.mapper.PostMapper;
import com.aixming.aggregatesearch.mapper.PostThumbMapper;
import com.aixming.aggregatesearch.model.dto.post.PostEsDTO;
import com.aixming.aggregatesearch.model.dto.post.PostQueryRequest;
import com.aixming.aggregatesearch.model.entity.Post;
import com.aixming.aggregatesearch.model.entity.PostFavour;
import com.aixming.aggregatesearch.model.entity.PostThumb;
import com.aixming.aggregatesearch.model.entity.User;
import com.aixming.aggregatesearch.model.vo.PostVO;
import com.aixming.aggregatesearch.model.vo.UserVO;
import com.aixming.aggregatesearch.service.PostService;
import com.aixming.aggregatesearch.service.UserService;
import com.aixming.aggregatesearch.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author AixMing
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postIdSet);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
        }
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public Page<PostVO> listPostVOByPage(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = page(new Page<>(current, size),
                getQueryWrapper(postQueryRequest));
        Page<PostVO> postVOPage = getPostVOPage(postPage, request);
        return postVOPage;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tags = postQueryRequest.getTags();
        List<String> orTags = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        int current = postQueryRequest.getCurrent() - 1;
        int pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();

        // 过滤
        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
        boolQueryBuilder.filter(TermQuery.of(m -> m.field("isDelete")
                .value(0))._toQuery());
        if (id != null) {
            boolQueryBuilder.filter(TermQuery.of(m -> m.field("id")
                    .value(id))._toQuery());
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(TermQuery.of(m -> m.field("id")
                    .value(notId))._toQuery());
        }
        if (userId != null) {
            boolQueryBuilder.filter(TermQuery.of(m -> m.field("userId")
                    .value(userId))._toQuery());
        }
        // 必须包含所有标签
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                boolQueryBuilder.filter(TermQuery.of(m -> m.field("tags")
                        .value(tag))._toQuery());
            }
        }
        // 包含任何一个标签即可
        if (CollectionUtils.isNotEmpty(orTags)) {
            for (String orTag : orTags) {
                boolQueryBuilder.should(TermQuery.of(m -> m.field("tags").value(orTag))._toQuery());
            }
            // 最少需要满足一个
            boolQueryBuilder.minimumShouldMatch("1");
        }
        // 按关键词搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 满足 title、content 即可
            boolQueryBuilder.should(MatchQuery.of(m -> m.field("title").query(searchText))._toQuery());
            boolQueryBuilder.should(MatchQuery.of(m -> m.field("content").query(searchText))._toQuery());
            boolQueryBuilder.minimumShouldMatch("1");
        }
        // 按标题搜索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.must(MatchQuery.of(m -> m.field("title").query(title))._toQuery());
        }
        // 按内容搜索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.must(MatchQuery.of(m -> m.field("content").query(content))._toQuery());
        }
        // 排序
        SortOptions sortOptions = null;
        if (StringUtils.isNotBlank(sortField)) {
            sortOptions = SortOptionsBuilders.field(f ->
                    f.field(sortField).order(
                            CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.Asc : SortOrder.Desc
                    )
            );
        }
        // 分页
        PageRequest pageRequest = PageRequest.of(current, pageSize);
        // 构造查询
        NativeQuery query;
        if (sortOptions == null) {
            query = NativeQuery.builder()
                    .withQuery(boolQueryBuilder.build()._toQuery())
                    .withPageable(pageRequest)
                    .withSort(Sort.by("createTime").descending())
                    .build();
        } else {
            query = NativeQuery.builder()
                    .withQuery(boolQueryBuilder.build()._toQuery())
                    .withPageable(pageRequest)
                    .withSort(sortOptions)
                    .build();
        }
        ArrayList<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据
        SearchHits<PostEsDTO> searchHits = elasticsearchTemplate.search(query, PostEsDTO.class);
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitsList = searchHits.getSearchHits();
            // 收集 id 列表
            List<Long> idList = searchHitsList.stream().map(searchHit -> searchHit.getContent().getId()).toList();
            // 从数据库中查询数据
            List<Post> postList = listByIds(idList);
            if (CollectionUtils.isNotEmpty(postList)) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                idList.forEach(dbId -> {
                    if (idPostMap.containsKey(dbId)) {
                        resourceList.add(idPostMap.get(dbId).get(0));
                    } else {
                        // 从 es 中删除数据库中已经被删除的记录
                        elasticsearchTemplate.delete(String.valueOf(dbId), PostEsDTO.class);
                    }
                });
            }
        }
        Page<Post> postPage = new Page<>();
        postPage.setTotal(resourceList.size());
        postPage.setRecords(resourceList);
        return postPage;
    }
}
