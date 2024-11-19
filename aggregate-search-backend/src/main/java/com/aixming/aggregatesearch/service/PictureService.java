package com.aixming.aggregatesearch.service;

import com.aixming.aggregatesearch.model.dto.picture.PictureQueryRequest;
import com.aixming.aggregatesearch.model.entity.Picture;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 图片服务
 *
 * @author AixMing
 */
public interface PictureService {
    Page<Picture> searchPicture(PictureQueryRequest pictureQueryRequest);
}
