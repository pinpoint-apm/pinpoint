/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.log.vo;

import java.text.ParseException;

/**
 * @author youngjin.kim2
 */
public record FileKey(HostKey hostKey, String fileName) {

    public static FileKey of(String hostGroupName, String hostName, String fileName) {
        return new FileKey(HostKey.of(hostGroupName, hostName), fileName);
    }

    public static FileKey parse(String s) throws ParseException {
        String[] words = s.split(":", 3);
        if (words.length != 3) {
            throw new ParseException(s, s.length());
        }
        HostKey hostKey = HostKey.of(words[0], words[1]);
        return new FileKey(hostKey, words[2]);
    }

    @Override
    public String toString() {
        return hostKey.toString() + ':' + fileName;
    }

}
