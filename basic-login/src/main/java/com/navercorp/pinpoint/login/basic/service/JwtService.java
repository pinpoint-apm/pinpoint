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

package com.navercorp.pinpoint.login.basic.service;

import com.navercorp.pinpoint.login.basic.config.BasicLoginProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class JwtService {

    private static final String KEY_CLAIMS_USER_ID = "userId";
    private static final String KEY_CLAIMS_USER_ROLE = "userRole";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    private final long expirationTimeMillis;

    public JwtService(BasicLoginProperties basicLoginProperties) {
        String secretKeyStr = basicLoginProperties.getJwtSecretKey();
        Assert.hasLength(secretKeyStr, "secretKey must not be empty");

        Assert.isTrue(basicLoginProperties.getExpirationTimeSeconds() > 0, "expirationTimeSeconds must be '>= 0'");
        this.expirationTimeMillis = TimeUnit.SECONDS.toMillis(basicLoginProperties.getExpirationTimeSeconds());

        byte[] keyBytes = Base64.getDecoder().decode(secretKeyStr);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        JwtParserBuilder jwtParser = Jwts.parser();
        jwtParser.verifyWith(secretKey);
        this.jwtParser = jwtParser.build();
    }

    public String createToken(UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        List<String> collect = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Claims claims = Jwts.claims()
                .add(KEY_CLAIMS_USER_ID, userDetails.getUsername())
                .add(KEY_CLAIMS_USER_ROLE, collect)
                .build();

        return createToken(claims);
    }

    private String createToken(Claims claims) {
        JwtBuilder jwtBuilder = Jwts.builder();

        jwtBuilder.claims(claims);
        jwtBuilder.issuedAt(new Date(System.currentTimeMillis()));
        jwtBuilder.expiration(new Date(System.currentTimeMillis() + expirationTimeMillis));
        jwtBuilder.signWith(secretKey);

        return jwtBuilder.compact();
    }

    public String getUserId(String token) {
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims body = claimsJws.getPayload();

        return body.get(KEY_CLAIMS_USER_ID, String.class);
    }

    public Date getExpirationDate(String token) {
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims body = claimsJws.getPayload();
        return body.getExpiration();
    }

    public long getExpirationTimeMillis() {
        return expirationTimeMillis;
    }

}