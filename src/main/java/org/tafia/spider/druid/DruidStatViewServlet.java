package org.tafia.spider.druid;

import com.alibaba.druid.support.http.StatViewServlet;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * Created by Dason on 2017/3/28.
 */
@WebServlet(
    urlPatterns = "/druid/*",
    initParams = {
        @WebInitParam(name = "allow", value = "127.0.0.1"),
        //@WebInitParam(name = "deny", value = "127.0.0.1"),
        @WebInitParam(name = "loginUsername", value = "tafia"),
        @WebInitParam(name = "loginPassword", value = "123456"),
        @WebInitParam(name = "resetEnable", value = "false")
    }
)
public class DruidStatViewServlet extends StatViewServlet {
}
