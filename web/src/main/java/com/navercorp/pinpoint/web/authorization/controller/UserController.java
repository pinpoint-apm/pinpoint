/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.util.ValueValidator;
import com.navercorp.pinpoint.web.vo.User;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public final static String USER_ID = "userid";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    @PostMapping
    public Response insertUser(@RequestBody User user) {
        if (!ValueValidator.validateUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User information validation failed to creating user information."
            );
        }
        userService.insertUser(user);
        return SimpleResponse.ok();
    }

    @DeleteMapping
    public Response deleteUser(@RequestBody User user) {
        if (StringUtils.isEmpty(user.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "there is not userId in params to delete user"
            );
        }
        userService.deleteUser(user.getUserId());
        return SimpleResponse.ok();
    }

    @GetMapping(params = "userId")
    public List<User> getUserByUserId(@RequestParam("userId") @NotBlank String userId) {
        try {
            final User user = userService.selectUserByUserId(userId);
            return List.of(user);
        } catch (Exception e) {
            logger.error("Cannot select user", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "This api need to collect condition for search."
            );
        }
    }

    @GetMapping(params = "searchKey")
    public List<User> getUserBySearchKey(@RequestParam("searchKey") @NotBlank String searchKey) {
        try {
            return userService.searchUser(searchKey);
        } catch (Exception e) {
            logger.error("Cannot select user", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "This api need to collect condition for search."
            );
        }
    }

    @GetMapping
    public List<User> getUsers() {
        try {
            return userService.selectUser();
        } catch (Exception e) {
            logger.error("Cannot select user", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "This api need to collect condition for search."
            );
        }
    }

    @PutMapping
    public Response updateUser(@RequestBody User user) {
        if (!ValueValidator.validateUser(user)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User information validation failed to creating user information"
            );
        }
        userService.updateUser(user);
        return SimpleResponse.ok();
    }

}
