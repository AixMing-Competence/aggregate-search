package com.aixming.aggregatesearch.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 数据源接口（新接入的数据源必须实现）
 *
 * @author AixMing
 * @since 2024-11-20 11:32:23
 */
public interface DataSource<T> {
    Page<T> doSearch(String searchText, int current, int pageSize, HttpServletRequest request);
}
