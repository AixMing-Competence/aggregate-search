package com.aixming.aggregatesearch.service.impl;

import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.exception.ThrowUtils;
import com.aixming.aggregatesearch.model.dto.picture.PictureQueryRequest;
import com.aixming.aggregatesearch.model.entity.Picture;
import com.aixming.aggregatesearch.service.PictureService;
import com.aixming.aggregatesearch.utils.Crawler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图片服务实现
 *
 * @author AixMing
 */
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

    @Override
    public Page<Picture> searchPicture(PictureQueryRequest pictureQueryRequest) {
        String searchText = pictureQueryRequest.getSearchText();
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 拉取数据
        List<Picture> pictureList = Crawler.fetchPicture(searchText, current, pageSize);
        Page<Picture> picturePage = new Page<>(current, pageSize);
        picturePage.setRecords(pictureList);
        return picturePage;
    }
}




