package com.aixming.aggregatesearch.model.dto.picture;

import com.aixming.aggregatesearch.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询图片请求
 *
 * @author AixMing
 * @since 2024-11-17 19:14:18
 */
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    private static final long serialVersionUID = -5638002952511004527L;

}
