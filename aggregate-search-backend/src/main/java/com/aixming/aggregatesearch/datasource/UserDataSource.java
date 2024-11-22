package com.aixming.aggregatesearch.datasource;

import com.aixming.aggregatesearch.model.dto.user.UserQueryRequest;
import com.aixming.aggregatesearch.model.vo.UserVO;
import com.aixming.aggregatesearch.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户数据源
 *
 * @author AixMing
 */
@Service
@RequiredArgsConstructor
public class UserDataSource implements DataSource<UserVO> {

    private final UserService userService;

    @Override
    public Page<UserVO> doSearch(String searchText, int current, int pageSize, HttpServletRequest request) {
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setSearchText(searchText);
        userQueryRequest.setCurrent(current);
        userQueryRequest.setPageSize(pageSize);
        return userService.listUserVOByPage(userQueryRequest);
    }
}
