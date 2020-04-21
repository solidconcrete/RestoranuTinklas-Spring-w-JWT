package com.RestaurantAPI.filters;

import com.RestaurantAPI.Services.MyUserDetailsService;
import com.RestaurantAPI.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.jws.soap.SOAPBinding;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException
    {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        //check if authentication header is of right format
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
        {
            //extract username
            System.out.println("AUTHORIZATION SUBSTRING" + authorizationHeader);
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT IN HEADER" + jwt);
            username = jwtUtil.extractUserName(jwt);
        }
        //extract user details
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            //get userDetails by username
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            //check whether the token matches userDetails and not expired
            if (jwtUtil.validateToken(jwt, userDetails))
            {
                //create default token for spring security
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                //set token into security context
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
