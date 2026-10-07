package com.wang.back.common;

import lombok.Data;

@Data
//统一返回结果类，统一数据
public class Result<T> {    //<T>是泛型，表示"类型待定"，用的时候才确定T是什么

    private Integer code;
    private String message;
    private T data;

    //成功响应
    public static <T> Result<T> success(T data) {   //<T>:方法自己的泛型声明，Result<T>:返回类型
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    //失败响应
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = message;
        return r;
    }
}
