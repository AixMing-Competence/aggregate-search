package com.aixming.aggregatesearch.datasource;

import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.exception.ThrowUtils;
import com.aixming.aggregatesearch.model.enums.SearchTypeEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author AixMing
 * @since 2024-11-26 13:34:36
 */
@Component
@RequiredArgsConstructor
public class SearchStrategyExecutor {

    private final List<DataSource<?>> dataSourceList;

    public Page<?> doExecutor(String searchText, int current, int pageSize, HttpServletRequest request, String type) {
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(searchTypeEnum == null, ErrorCode.PARAMS_ERROR, "搜索类型错误");
        // 遍历 dataSourceList
        for (DataSource<?> dataSource : dataSourceList) {
            if (dataSource.getClass().getAnnotation(SearchConfig.class).type().equals(searchTypeEnum.getValue())) {
                return dataSource.doSearch(searchText, current, pageSize, request);
            }
        }
        return null;
    }
}
