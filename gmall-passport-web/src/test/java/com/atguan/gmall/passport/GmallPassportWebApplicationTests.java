package com.atguan.gmall.passport;

import com.atguan.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void testJwt() {

        String key = "atguan";

        Map<String,Object> map = new HashMap<>();

        String salt = "192.168.113.1";

        map.put("userId",1001);
        map.put("nickName","admin");

        String token = JwtUtil.encode(key, map, salt);

        System.err.println(token);

        //解密
        Map<String, Object> decode = JwtUtil.decode(token, key, salt);

        System.err.println(decode);
    }

}
