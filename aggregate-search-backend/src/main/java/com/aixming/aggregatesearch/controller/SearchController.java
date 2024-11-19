package com.aixming.aggregatesearch.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aixming.aggregatesearch.common.BaseResponse;
import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.common.ResultUtils;
import com.aixming.aggregatesearch.exception.BusinessException;
import com.aixming.aggregatesearch.exception.ThrowUtils;
import com.aixming.aggregatesearch.model.dto.picture.PictureQueryRequest;
import com.aixming.aggregatesearch.model.dto.post.PostQueryRequest;
import com.aixming.aggregatesearch.model.dto.search.SearchRequest;
import com.aixming.aggregatesearch.model.dto.user.UserQueryRequest;
import com.aixming.aggregatesearch.model.entity.Picture;
import com.aixming.aggregatesearch.model.vo.PostVO;
import com.aixming.aggregatesearch.model.vo.SearchVO;
import com.aixming.aggregatesearch.model.vo.UserVO;
import com.aixming.aggregatesearch.service.PictureService;
import com.aixming.aggregatesearch.service.PostService;
import com.aixming.aggregatesearch.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * 聚合搜索接口
 *
 * @author AixMing
 * @since 2024-11-19 14:22:05
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final PictureService pictureService;

    private final UserService userService;

    private final PostService postService;

    /**
     * 根据 searchText 分页查询多个数据源
     *
     * @param searchRequest
     * @param request
     * @return
     */
    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);
        SearchVO searchVO = new SearchVO();
        if (StringUtils.isNotBlank(searchRequest.getSearchText())) {
            PictureQueryRequest pictureQueryRequest = BeanUtil.copyProperties(searchRequest, PictureQueryRequest.class);
            Page<Picture> picturePage = pictureService.searchPicture(pictureQueryRequest);
            searchVO.setPictureList(picturePage.getRecords());
        }
        UserQueryRequest userQueryRequest = BeanUtil.copyProperties(searchRequest, UserQueryRequest.class);
        Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
        PostQueryRequest postQueryRequest = BeanUtil.copyProperties(searchRequest, PostQueryRequest.class);
        Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);

        searchVO.setUserList(userVOPage.getRecords());
        searchVO.setPostList(postVOPage.getRecords());
        return ResultUtils.success(searchVO);
    }

    /**
     * 根据 searchText 分页查询多个数据源（并发）
     *
     * @param searchRequest
     * @param request
     * @return
     */
    @PostMapping("/all/fast")
    public BaseResponse<SearchVO> searchAllFast(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);
        SearchVO searchVO = new SearchVO();

        CompletableFuture<Void> pictureTask = CompletableFuture.runAsync(() -> {
            if (StringUtils.isNotBlank(searchRequest.getSearchText())) {
                PictureQueryRequest pictureQueryRequest = BeanUtil.copyProperties(searchRequest, PictureQueryRequest.class);
                Page<Picture> picturePage = pictureService.searchPicture(pictureQueryRequest);
                searchVO.setPictureList(picturePage.getRecords());
            }
        });

        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            UserQueryRequest userQueryRequest = BeanUtil.copyProperties(searchRequest, UserQueryRequest.class);
            Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
            return userVOPage;
        });

        CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
            PostQueryRequest postQueryRequest = BeanUtil.copyProperties(searchRequest, PostQueryRequest.class);
            Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);
            return postVOPage;
        });

        // 等待任务完成
        CompletableFuture.allOf(pictureTask, userTask, postTask).join();
        Page<UserVO> userVOPage;
        Page<PostVO> postVOPage;
        try {
            userVOPage = userTask.get();
            postVOPage = postTask.get();
        } catch (Exception e) {
            log.error("搜索异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "搜索异常");
        }
        searchVO.setUserList(userVOPage.getRecords());
        searchVO.setPostList(postVOPage.getRecords());
        return ResultUtils.success(searchVO);
    }

}
