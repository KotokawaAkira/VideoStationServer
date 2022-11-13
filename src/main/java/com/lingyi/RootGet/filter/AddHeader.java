package com.lingyi.RootGet.filter;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AddHeader extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        String method = request.getMethod();

        response.addHeader("Access-Control-Allow-Origin",origin);
        response.addHeader("Access-Control-Allow-Headers","Content-Type,Sec-Fetch-Site");
        response.addHeader("Access-Control-Allow-Credentials","true");

        if(method.equals("OPTIONS")){
            response.setStatus(200);
            return;
        }
        filterChain.doFilter(request,response);
    }
}
