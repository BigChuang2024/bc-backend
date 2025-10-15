package com.haderacher.bcbackend.common;

import lombok.Getter;

// T 是泛型，代表 data 字段的类型
// @JsonInclude(JsonInclude.Include.NON_NULL) // 这个注解表示如果data为null，则不序列化data字段
@Getter
public class ApiResponse<T> {

    private final String message; // 响应消息
    private final T data;         // 具体的响应数据

    // 私有化构造函数，强制使用静态工厂方法创建实例
    private ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // --- 静态工厂方法 ---

    // 成功，只返回成功状态，不带数据
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("操作成功", null);
    }

    // 成功，并返回数据
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("操作成功", data);
    }

    // 成功，并返回自定义消息和数据
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    // 失败，只返回失败信息
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(message, null);
    }

    // 失败，返回失败信息和一些调试数据（可选）
    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(message, data);
    }

}