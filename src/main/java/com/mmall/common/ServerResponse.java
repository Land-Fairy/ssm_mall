package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 通用响应对象
 * @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL) 用来忽略值为 null的字段 （json序列化后不返回)
 * @param <T>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    /* 将构造方法都设置为 private，提供 static 方法供使用 */
    private ServerResponse(int status) {
        this.status = status;
    }

    /* 注意 这个和下面一个方法 的区别
    *
    * ServerResponse(1, new Object()) 会调用该方法
    * ServerResponse(1, "aa") 会调用下面的重载 (精确优先)
    * ==> 如果 data 为 string 类型，则会造成歧义(本来赋值给 data， 结果赋值给了 msg)
    *
    * */
    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 使用 @JsonIgnore, 不序列化 isSuccess 字段
     * @return
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),
                ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),
                msg);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int code, String msg) {
        return new ServerResponse<T>(code, msg);
    }


    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
