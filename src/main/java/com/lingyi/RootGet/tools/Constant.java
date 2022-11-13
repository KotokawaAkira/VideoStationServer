package com.lingyi.RootGet.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.RootGet.entry.Video;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Constant {
    /***** 文件不存在*****/
    public static String FileNotExists = "{\"success\":\"false\",\"reason\":\"FileNotExists\"}";
    /***** 文件已存在*****/
    public static String FileExists = "{\"success\":\"false\",\"reason\":\"FileExists\"}";
    /***** 成功*****/
    public static String SUCCESS = "{\"success\":\"true\",\"reason\":\"null\"}";
    /***** 失败*****/
    public static String FAILURE = "{\"success\":\"false\",\"reason\":\"unknown\"}";
    /***** 没有权限*****/
    public static String NoPermission = "{\"success\":\"false\",\"reason\":\"NoPermission\"}";
    /***** 项目(商品)未找到*****/
    public static String ItemNotFound = "{\"success\":\"false\",\"reason\":\"ItemNotFound\"}";
    /***** 商品图片储存路劲*****/
    public static String RepositoryPath = "/opt/Server/DigitalStoreRepository/";
    /***** 账号不存在*****/
    public static String AccountNotExists = "{\"success\":\"false\",\"reason\":\"AccountNotFound\"}";
    /***** 密码不正确****/
    public static String PasswordIncorrect = "{\"success\":\"false\",\"reason\":\"PasswordIncorrect\"}";
    /***** 空json*****/
    public static String EmptyJson = "{}";
    /*****地址已存在*****/
    public static String AddressExists = "{\"success\":\"false\",\"reason\":\"AddressExists\"}";
    /*****物品已经被添加到购物车*****/
    public static String CartExists = "{\"success\":\"false\",\"reason\":\"CartExists\"}";
    /*****请求次数过多*****/
    public static String TooMuchTimes = "{\"success\":\"false\",\"reason\":\"TooMuchTimes\"}";
    /**包含违禁词*/
    public static String WordsForbidden = "{\"success\":\"false\",\"reason\":\"WordsForbidden\"}";
    public static String AccountBanned = "{\"success\":\"false\",\"reason\":\"AccountBanned\"}";
    public static String KeyExists = "{\"success\":\"false\",\"reason\":\"KeyExists\"}";
    public static ArrayList<String> forbiddenWords = new ArrayList<>();

    public static ArrayList<String> getForbiddenWords() {
        if (forbiddenWords.size() != 0)
            return forbiddenWords;
        else{
            forbiddenWords.add("共产党");
            forbiddenWords.add("傻逼");
            forbiddenWords.add("你妈逼");
            forbiddenWords.add("操你妈");
            forbiddenWords.add("日你妈");
            forbiddenWords.add("肏");
            forbiddenWords.add("屄");
        }
        return forbiddenWords;
    }
    /*****token生成器*****/
    /**
     * @param bounds 生成的token长度
     **/
    public static String tokenGenerate(int bounds) {
        String token = "";
        Random random = new Random();
        for (int j = 0; j < bounds; j++) {
            int i = random.nextInt(3);
            switch (i) {
                case 0:
                    token += (char) (random.nextInt(26) + 65);
                    break;
                case 1:
                    token += (char) (random.nextInt(26) + 97);
                    break;
                case 2:
                    token += random.nextInt(10);
                    break;
            }
        }
        return token;
    }

    /*****从浏览器cookie中获取uid*****/
    public static String getUidInCookie(HttpServletRequest request) {
        String id = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return id;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("uid")) {
                id = cookie.getValue();
                return id;
            }
        }
        return id;
    }
    /*****从浏览器cookie中获取token*****/
    public static String getTokenInCookie(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return token;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("token")) {
                token = cookie.getValue();
                return token;
            }
        }
        return token;
    }

    /*****String时间转换long*****/
    public static long StringParseToLong(String time_string) {
        Date date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(time_string);
            return date.getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /*****文件大小bit转换成KB MB GB等*****/
    public static String transformToString(long fileSize) {
        String StringSize = "";
        if (fileSize < 1024) StringSize = fileSize + "B";
        if (fileSize >= 1024 && fileSize < 1048576) {
            double result = fileSize * 1.0 / 1024 * 1.0;
            String result_str = String.valueOf(result);
            String[] split = result_str.split("\\.");
            if (split[1].length() > 2) StringSize = split[0] + "." + split[1].substring(0, 2) + "KB";
            else StringSize = split[0] + "." + split[1] + "KB";
        }
        if (fileSize >= 1048576 && fileSize < 1073741824) {
            double result = fileSize * 1.0 / 1048576 * 1.0;
            String result_str = String.valueOf(result);
            String[] split = result_str.split("\\.");
            if (split[1].length() > 2) StringSize = split[0] + "." + split[1].substring(0, 2) + "MB";
            else StringSize = split[0] + "." + split[1] + "MB";
        }
        if (fileSize >= 1073741824) {
            double result = fileSize * 1.0 / 1073741824 * 1.0;
            String result_str = String.valueOf(result);
            String[] split = result_str.split("\\.");
            if (split[1].length() > 2) StringSize = split[0] + "." + split[1].substring(0, 2) + "GB";
            else StringSize = split[0] + "." + split[1] + "GB";
        }
        return StringSize;
    }

    /*****获取文件后缀*****/
    public static String getFileType(File file) {
        String fileName = file.getName();
        String[] strings = fileName.split("\\.");
        String fileType = strings[strings.length - 1];
        return fileType;
    }
}
