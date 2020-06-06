package com.xuecheng.framework.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *  密码的加密和密码校验工具类
 */
public class BCryptUtil {
    /**
     * 对原始密码进行编码。一般来说，一个好的编码算法应用一个SHA-1或更大的散列和一个8字节或更大的随机生成的salt
     * @param password
     * @return
     */
    public static String encode(String password){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashPass = passwordEncoder.encode(password);
        return hashPass;
    }

    /**
     * 验证从存储中获取的编码密码在编码后是否与提交的原始密码匹配。如果密码匹配，则返回true；如果密码不匹配，则返回false。存储的密码本身不会被解码。
     * @param password
     * @param hashPass
     * @return
     */
    public static boolean matches(String password,String hashPass){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean f = passwordEncoder.matches(password, hashPass);
        return f;
    }
}
