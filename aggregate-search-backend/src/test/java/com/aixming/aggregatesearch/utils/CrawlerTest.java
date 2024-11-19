package com.aixming.aggregatesearch.utils;

import cn.hutool.json.JSONUtil;
import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.exception.BusinessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @author AixMing
 * @since 2024-11-17 19:35:32
 */
class CrawlerTest {

    @Test
    void fetchPicture() {
        String url = String.format("https://cn.bing.com/images/search?q=%s&form=IGRE&first=20", "小黑子");
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }
        Elements elements = document.select(".iuscp.isv");
        for (Element element: elements) {
            String m = element.select(".iusc").attr("m");
            Map<String,Object> map = JSONUtil.toBean(m, Map.class);
            Object imgUrl = map.get("murl");
            Object title = map.get("t");
            System.out.println(imgUrl);
            System.out.println(title);
        }
    }
}
