package com.navercorp.pinpoint.web.frontend.export;

import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

@Component
public class UserServiceConfigExporter implements FrontendConfigExporter {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final UserService userService;

    public UserServiceConfigExporter(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    @Override
    public void export(Map<String, Object> export) {
        String userId = userService.getUserIdFromSecurity();
        if (StringUtils.hasLength(userId)) {
            User user = userService.selectUserByUserId(userId);
            if (user == null) {
                logger.info("User({}) info don't saved database.", userId);
            } else {
                export.put("userId", user.getUserId());
                export.put("userName", user.getName());
                export.put("userDepartment", user.getDepartment());
            }
        }
    }
}
