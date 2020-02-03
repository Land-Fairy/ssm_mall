package com.mmall.util;

import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 由于存储到 Redis时，使用的是 String
 * 因此，就需要 JsonUtil 来做 obj => string 或者 string => obj
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        /* 序列化时，所有的字段全部列入 */
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
        /* 取消 时间 默认转换 timestamp 形式 */
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        /* 忽略 空 bean 转 json 错误 */
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        /* 所有的时间格式都统一下面的形式 */
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        /* 忽略 在 json字符串中存在，但是 java对象中不存在对应属性的情况，防止错误 */
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String)obj: objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse object to String error", e);
            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String)obj: objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse object to String error", e);
            return null;
        }
    }

    /**
     * 通用的 字符串 转 具体类型
     * @param str
     * @param tTypeReference
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, TypeReference<T> tTypeReference) {
        if (StringUtils.isEmpty(str) || tTypeReference == null) {
            return null;
        }

        try {
            return (T) (tTypeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, tTypeReference));
        } catch (IOException e) {
            log.warn("Parse String to Objet error", e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<T> collectionClass,
                                   Class<?>... elementCLasses) {
        JavaType javaType = objectMapper.getTypeFactory().
                constructParametricType(collectionClass, elementCLasses);

        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    /**
     * 有问题的反序列化方法 如果 类型是 List<<User>> 则序列化错误
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }

        try {
            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            log.warn("Parse String to Objet error", e);
            return null;
        }
    }

    public static void main(String[] args) {
        User u1 = new User();
        u1.setId(1);
        u1.setUsername("aaa");

        User u2 = new User();
        u2.setId(2);
        u2.setEmail("111.com");

        String s = JsonUtil.obj2String(u1);
        String s1 = JsonUtil.obj2StringPretty(u1);

        User user = JsonUtil.string2Obj(s, User.class);

        System.out.println(user);

        List<User> users = Arrays.asList(u1, u2);

        String s2 = JsonUtil.obj2String(users);

        List<User> users1 = JsonUtil.string2Obj(s2, new TypeReference<List<User>>() {
        });
        System.out.println(users1);

        List list = JsonUtil.string2Obj(s2, List.class, User.class);

    }
}
