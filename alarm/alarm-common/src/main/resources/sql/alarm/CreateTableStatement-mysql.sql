-- Requires MySQL 5.7 or later: the config columns are JSON.

CREATE TABLE alarm_template (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name        VARCHAR(127)    NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    deleted             TINYINT(1)      NOT NULL DEFAULT 0,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_template_service (service_name, deleted)
);

CREATE TABLE alarm_template_item (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id         BIGINT          NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    description         VARCHAR(1000),
    severity            VARCHAR(20)     NOT NULL,
    data_source         VARCHAR(50)     NOT NULL,
    check_interval_sec  INT             NOT NULL DEFAULT 600,
    action_interval_sec INT             NOT NULL DEFAULT 1800,
    conditions          JSON            NOT NULL,
    filters             JSON,
    deleted             TINYINT(1)      NOT NULL DEFAULT 0,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_item_template_datasource (template_id, deleted, data_source)
);

-- A rule targets an application by service, name and type rather than by an id,
-- so it is not tied to any single registry's identifier space; the type decides
-- which registry resolves it. Name and description live in
-- alarm_rule_local_config with the other per-rule overrides.
CREATE TABLE alarm_rule_v2 (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name        VARCHAR(127)    NOT NULL,
    application_name    VARCHAR(127)    NOT NULL,
    application_type    VARCHAR(30)     NOT NULL,
    data_source         VARCHAR(50)     NOT NULL,
    template_item_id    BIGINT,
    enabled             TINYINT(1)      NOT NULL DEFAULT 1,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_rule_template_item_enabled (template_item_id, enabled),
    -- The rule list and the application cleanup both filter on these three.
    INDEX idx_rule_application (service_name, application_name, application_type)
);

-- Everything a rule overrides. For a template-linked rule a NULL column means
-- "inherit the bundle item's value"; a standalone rule stores its whole config here.
CREATE TABLE alarm_rule_local_config (
    rule_id             BIGINT PRIMARY KEY,
    name                VARCHAR(255),
    description         VARCHAR(1000),
    severity            VARCHAR(20),
    check_interval_sec  INT,
    action_interval_sec INT,
    conditions          JSON,
    filters             JSON
);

CREATE TABLE alarm_notification_channel (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name        VARCHAR(127)    NOT NULL,
    channel_name        VARCHAR(100)    NOT NULL,
    method_type         VARCHAR(10)     NOT NULL,
    destination         VARCHAR(256)    NOT NULL,
    config              JSON,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_channel_service (service_name)
);

CREATE TABLE alarm_channel_binding (
    owner_type          VARCHAR(20)     NOT NULL,
    owner_id            BIGINT          NOT NULL,
    channel_id          BIGINT          NOT NULL,

    PRIMARY KEY (owner_type, owner_id, channel_id),
    INDEX idx_channel_binding_channel_owner (channel_id)
);

CREATE TABLE alarm_state (
    rule_id             BIGINT PRIMARY KEY,
    status              VARCHAR(20)     NOT NULL DEFAULT 'NORMAL',
    last_checked_at     DATETIME,
    last_fired_at       DATETIME,
    last_notification_enqueued_at DATETIME,
    last_notified_at    DATETIME,
    next_check_at       DATETIME,

    INDEX idx_alarm_state_due (next_check_at)
);

CREATE TABLE alarm_history_v2 (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id             BIGINT          NOT NULL,
    event_type          VARCHAR(20)     NOT NULL,
    message             VARCHAR(2000),
    context             JSON,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_history_rule (rule_id),
    INDEX idx_history_created (created_at)
);

CREATE TABLE alarm_notification_outbox (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    history_id          BIGINT          NOT NULL,
    channel_id          BIGINT          NOT NULL,
    method_type         VARCHAR(10)     NOT NULL,
    -- utf8mb4: payloads quote browser error messages and use emoji
    payload             MEDIUMTEXT      CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    attempt_count       TINYINT UNSIGNED NOT NULL DEFAULT 0,
    available_at        DATETIME,
    claim_token         CHAR(32),

    UNIQUE KEY uk_outbox_history_channel (history_id, channel_id),
    INDEX idx_outbox_dispatch (status, available_at),
    INDEX idx_outbox_claim_token (claim_token)
);
