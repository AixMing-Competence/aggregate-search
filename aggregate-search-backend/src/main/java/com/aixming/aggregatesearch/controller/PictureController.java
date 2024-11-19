package com.aixming.aggregatesearch.controller;

import com.aixming.aggregatesearch.common.BaseResponse;
import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.common.ResultUtils;
import com.aixming.aggregatesearch.exception.ThrowUtils;
import com.aixming.aggregatesearch.model.dto.picture.PictureQueryRequest;
import com.aixming.aggregatesearch.model.entity.Picture;
import com.aixming.aggregatesearch.service.PictureService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图片接口
 *
 * @author AixMing
 * @since 2024-11-17 19:17:29
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;
    
    @PostMapping("/list/page")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest){
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<Picture> picturePage = pictureService.searchPicture(pictureQueryRequest);
        return ResultUtils.success(picturePage);
    }

}
