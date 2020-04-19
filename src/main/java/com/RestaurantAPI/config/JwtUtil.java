package com.RestaurantAPI.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

//@EnableAutoConfiguration
@Component
public class JwtUtil {



    @Value("${jwt.secret}")
    private String secret;

    public String extractUserName(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    //retrieve expiration date from JWT
    public Date extractExpiration (String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token)
    {
        if (token.contains("{\"jwt\":\""))
        {
            System.out.println("test");
            token = token.substring(8, token.length()-2);
        }
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired (String token)
    {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken (UserDetails userDetails)
    {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject)
    {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails)
    {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


}
