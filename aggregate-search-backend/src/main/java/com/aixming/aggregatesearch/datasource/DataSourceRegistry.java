package com.aixming.aggregatesearch.datasource;

import com.aixming.aggregatesearch.model.enums.SearchTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据源注册器
 *
 * @author AixMing
 * @since 2024-11-26 13:15:11
 */
@Component
@RequiredArgsConstructor
public class DataSourceRegistry {

    private final PictureDataSource pictureDataSource;

    private final PostDataSource postDataSource;

    private final UserDataSource userDataSource;

    private Map<String, DataSource<?>> dataSourceMap;

    @PostConstruct
    public void doInit() {
        dataSourceMap = Map.of(
                SearchTypeEnum.USER.getValue(), userDataSource,
                SearchTypeEnum.POST.getValue(), postDataSource,
                SearchTypeEnum.PICTURE.getValue(), pictureDataSource
        );
    }

    public DataSource<?> getDataSourceByType(String type) {
        if (dataSourceMap == null) {
            return null;
        }
        return dataSourceMap.get(type);
    }
}
