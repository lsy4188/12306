package com.itlsy.config;

import lombok.Data;

import java.util.List;

/**
 * 过滤器配置(接收配置文件对应的配置项->blackPathPre参数)
 */
@Data
public class Config {

    /**
     * 黑名单前置路径
     */
    private List<String> blackPathPre;
}
