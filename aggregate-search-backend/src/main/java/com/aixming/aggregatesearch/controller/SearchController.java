package com.aixming.aggregatesearch.controller;

import com.aixming.aggregatesearch.common.BaseResponse;
import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.common.ResultUtils;
import com.aixming.aggregatesearch.datasource.DataSource;
import com.aixming.aggregatesearch.datasource.PictureDataSource;
import com.aixming.aggregatesearch.datasource.PostDataSource;
import com.aixming.aggregatesearch.datasource.UserDataSource;
import com.aixming.aggregatesearch.exception.BusinessException;
import com.aixming.aggregatesearch.exception.ThrowUtils;
import com.aixming.aggregatesearch.model.dto.search.SearchRequest;
import com.aixming.aggregatesearch.model.entity.Picture;
import com.aixming.aggregatesearch.model.enums.SearchTypeEnum;
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

    private final PictureDataSource pictureDataSource;

    private final PostDataSource postDataSource;

    private final UserDataSource userDataSource;

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
        // 要获取的数据类型
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);

        String searchText = searchRequest.getSearchText();
        int current = searchRequest.getCurrent();
        int pageSize = searchRequest.getPageSize();


        SearchVO searchVO = new SearchVO();

        if (searchTypeEnum == null) {
            // 并发查询全部数据
            CompletableFuture<Void> pictureTask = CompletableFuture.runAsync(() -> {
                if (StringUtils.isNotBlank(searchText)) {
                    Page<Picture> picturePage = pictureDataSource.doSearch(searchText, current, pageSize,request);
                    searchVO.setPictureList(picturePage.getRecords());
                }
            });

            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                Page<UserVO> userVOPage = userDataSource.doSearch(searchText, current, pageSize,request);
                return userVOPage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize,request);
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
        } else {
            // 查询特殊类别的数据
            DataSource dataSource = null;
            switch (searchTypeEnum) {
                case POST -> {
                    dataSource = postDataSource;
                }
                case USER -> {
                    dataSource = userDataSource;
                }
                case PICTURE -> {
                    dataSource = pictureDataSource;
                }
                default -> {
                }
            }
            Page<?> page = dataSource.doSearch(searchText, current, pageSize,request);
            searchVO.setDataList(page.getRecords());
        }
        return ResultUtils.success(searchVO);
    }

}
