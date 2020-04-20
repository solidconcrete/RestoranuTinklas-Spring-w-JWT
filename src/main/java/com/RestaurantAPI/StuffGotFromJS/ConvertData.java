package com.RestaurantAPI.StuffGotFromJS;

import com.RestaurantAPI.config.JwtUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ConvertData {

    private static JwtUtil jwtUtil;

    public static String[] GetLoginDataFromPost(String user) throws UnsupportedEncodingException {
        user = URLDecoder.decode(user, "ISO-8859-1");
        user = user.substring(0, user.length()-1);
        Object obj = JSONValue.parse(user);
        JSONObject jsonObject = (JSONObject) obj;
        String[] loginData = {(String) jsonObject.get("username"), (String) jsonObject.get("password")};
        return loginData;
    }

    public static String ExtractJwtFromAuthorizationHeader(String header)
    {
        return jwtUtil.extractUserName(header.substring(15, header.length()-2));
    }
}
