/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.security.login;

import com.navercorp.pinpoint.web.config.BasicLoginConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class JwtService {

    private static final String KEY_CLAIMS_USER_ID = "userId";
    private static final String KEY_CLAIMS_USER_ROLE = "userRole";

    private final JwtParser jwtParser;

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    private final String secretKey;

    private final long expirationTimeMillis;

    public JwtService(BasicLoginConfig basicLoginConfig) {
        String secretKey = basicLoginConfig.getJwtSecretKey();
        Assert.hasLength(secretKey, "secretKey must not be empty");

        this.secretKey = secretKey;

        Assert.isTrue(basicLoginConfig.getExpirationTimeSeconds() > 0, "expirationTimeSeconds must be '>= 0'");
        this.expirationTimeMillis = TimeUnit.SECONDS.toMillis(basicLoginConfig.getExpirationTimeSeconds());

        JwtParser jwtParser = Jwts.parser();
        jwtParser.setSigningKey(secretKey);
        this.jwtParser = jwtParser;
    }

    public String createToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        List<String> collect = authorities.stream().map(e -> ((GrantedAuthority) e).getAuthority()).collect(Collectors.toList());

        claims.put(KEY_CLAIMS_USER_ID, userDetails.getUsername());
        claims.put(KEY_CLAIMS_USER_ROLE, collect);

        return createToken(claims);
    }

    private String createToken(Map<String, Object> claims) {
        JwtBuilder jwtBuilder = Jwts.builder();

        jwtBuilder.setClaims(claims);
        jwtBuilder.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis));
        jwtBuilder.signWith(signatureAlgorithm, secretKey);

        return jwtBuilder.compact();
    }

    public String getUserId(String token) {
        Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        Claims body = claimsJws.getBody();

        return body.get(KEY_CLAIMS_USER_ID, String.class);
    }

    public Date getExpirationDate(String token) {
        Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        Claims body = claimsJws.getBody();
        return body.getExpiration();
    }

    public long getExpirationTimeMillis() {
        return expirationTimeMillis;
    }

}