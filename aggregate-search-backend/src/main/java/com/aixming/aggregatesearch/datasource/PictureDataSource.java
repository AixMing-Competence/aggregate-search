package com.aixming.aggregatesearch.datasource;

import com.aixming.aggregatesearch.model.dto.picture.PictureQueryRequest;
import com.aixming.aggregatesearch.model.entity.Picture;
import com.aixming.aggregatesearch.service.PictureService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 图片数据源
 *
 * @author AixMing
 */
@Service
@RequiredArgsConstructor
public class PictureDataSource implements DataSource<Picture> {

    private final PictureService pictureService;

    @Override
    public Page<Picture> doSearch(String searchText, int current, int pageSize, HttpServletRequest request) {
        PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
        pictureQueryRequest.setSearchText(searchText);
        pictureQueryRequest.setCurrent(current);
        pictureQueryRequest.setPageSize(pageSize);
        return pictureService.searchPicture(pictureQueryRequest);
    }
}




