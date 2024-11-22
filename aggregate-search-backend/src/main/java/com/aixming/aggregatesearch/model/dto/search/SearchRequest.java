package com.aixming.aggregatesearch.model.dto.search;

import com.aixming.aggregatesearch.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 聚合搜索请求
 *
 * @author AixMing
 * @since 2024-11-19 14:25:27
 */
@Data
public class SearchRequest extends PageRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 类型
     */
    private String type;

    private static final long serialVersionUID = 434304861987020593L;
}
