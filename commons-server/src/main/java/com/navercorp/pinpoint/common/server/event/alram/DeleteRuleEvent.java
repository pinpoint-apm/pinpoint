package com.navercorp.pinpoint.common.server.event.alram;

import java.util.Objects;

public class DeleteRuleEvent {
    private final String roleId;
    private final boolean webhookSend;

    public DeleteRuleEvent(String roleId, boolean webhookSend) {
        this.roleId = Objects.requireNonNull(roleId, "roleId");
        this.webhookSend = webhookSend;
    }

    public String getRoleId() {
        return roleId;
    }

    public boolean isWebhookSend() {
        return webhookSend;
    }

    @Override
    public String toString() {
        return "DeleteRuleEvent{" +
                "roleId='" + roleId + '\'' +
                ", webhookSend=" + webhookSend +
                '}';
    }
}
