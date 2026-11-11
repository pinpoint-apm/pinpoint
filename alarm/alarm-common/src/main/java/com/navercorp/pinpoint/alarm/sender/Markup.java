/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.sender;

/**
 * The markup of a text message: none, or Slack mrkdwn. Every method takes a raw value and
 * escapes it, so each value a message shows is escaped once.
 */
enum Markup {
    PLAIN,
    SLACK;

    static Markup of(boolean mrkdwn) {
        return mrkdwn ? SLACK : PLAIN;
    }

    /** Slack builds links ({@code <url|label>}) and mentions from these characters. */
    String escape(String text) {
        if (this == PLAIN) {
            return text;
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    String bold(String text) {
        return this == SLACK ? "*" + escape(text) + "*" : text;
    }

    String code(String text) {
        return this == SLACK ? "`" + escape(text) + "`" : text;
    }

    /** A Slack link, which shows the label in place of the URL. */
    static String slackLink(String url, String label) {
        return "<" + SLACK.escape(url) + "|" + SLACK.escape(label) + ">";
    }
}
