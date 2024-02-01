package com.itheima.reggie.filter;

//检查用户是否完成登录

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到的请求为：{}", requestURI);

        //要放行的请求,登录和退出的controller，前端的静态资源，没有数据的
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",  //移动端发送短信
                "/user/login"      //移动端登录

        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);


        //3、如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求无需处理，直接放行：{}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4、判断登录状态，如果已登录，则直接放行
        //为什么是已经登录？因为登录成功后，会将用户信息存入session中
        if(request.getSession().getAttribute("employee")!=null){
            Long id = (Long) request.getSession().getAttribute("employee");
            log.info("用户已经登录，用户id为：{}", id);
            //将用户id存入ThreadLocal中
            BaseContext.setCurrentId(id);

            //获取当前线程id
            long cid = Thread.currentThread().getId();
            log.info("当前线程id为：{}", cid);


            filterChain.doFilter(request, response);
            return;
        }


        if(request.getSession().getAttribute("user")!=null){
            Long id = (Long) request.getSession().getAttribute("user");
            log.info("用户已经登录，用户id为：{}", id);
            //将用户id存入ThreadLocal中
            BaseContext.setCurrentId(id);

            //获取当前线程id
            long cid = Thread.currentThread().getId();
            log.info("当前线程id为：{}", cid);


            filterChain.doFilter(request, response);
            return;
        }





        //5、如果未登录则返回未登录结果，通过输出流的方式，向前端页面响应数据
        log.info("用户未登录，拦截请求：{}", requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }

    //路径匹配，检查是否需要放行
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            //如果匹配成功，则返回true
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        //如果匹配失败，则返回false
        return false;
    }
}
