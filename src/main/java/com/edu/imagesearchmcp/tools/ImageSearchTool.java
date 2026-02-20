package com.edu.imagesearchmcp.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * mcp服务端
 */
@Service
public class ImageSearchTool {

    private static final String API_KEY = "你的API_KEY";

    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query){
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    public List<String> searchMediumImages(String query) {
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        return JSONUtil.parseObj(response)  // 将响应字符串解析为 JSON 对象
                .getJSONArray("photos") // 获取 photos 数组,包含所有图片信息
                .stream()
                .map(photoObj->(JSONObject) photoObj)   // 将流中的每个元素转换为 JSONObject 类型
                .map(photoObj->photoObj.getJSONObject("src"))   // 从每个图片对象中提取 src 对象（包含各种尺寸的图片 URL）
                .map(photo->photo.getStr("medium")) // 从 src 对象中提取 medium 尺寸的图片 URL,getStr()方法返回字符串
                .filter(StrUtil::isNotBlank)    // 过滤掉空字符串（防止 API 返回空值导致后续处理出错）
                .collect(Collectors.toList());  // 将处理后的 Stream 收集为 List<String>
    }

}
