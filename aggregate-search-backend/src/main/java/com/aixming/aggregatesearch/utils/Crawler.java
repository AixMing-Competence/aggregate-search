package com.aixming.aggregatesearch.utils;

import cn.hutool.json.JSONUtil;
import com.aixming.aggregatesearch.common.ErrorCode;
import com.aixming.aggregatesearch.exception.BusinessException;
import com.aixming.aggregatesearch.model.entity.Picture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author AixMing
 * @since 2024-11-13 21:52:26
 */
public class Crawler {

    /**
     * 从百度图片中抓取图片
     *
     * @return
     */
    public static List<Picture> fetchPicture(String searchText, int current, int pageSize) {
        int pageNum = (current - 1) * pageSize;
        String url = String.format("https://cn.bing.com/images/search?q=%s&form=IGRE&first=%s", searchText, pageNum);
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }
        ArrayList<Picture> pictureList = new ArrayList<>();
        Elements elements = document.select(".iuscp.isv");
        for (Element element : elements) {
            String m = element.select(".iusc").attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            Object imgUrl = map.get("murl");
            Object title = map.get("t");
            Picture picture = new Picture();
            picture.setTitle((String) title);
            picture.setUrl((String) imgUrl);
            pictureList.add(picture);
            // 判断不能超过数量
            if (pictureList.size() >= pageSize) {
                break;
            }
        }
        return pictureList;
    }
}
