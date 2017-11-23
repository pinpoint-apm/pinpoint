/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

/**
 * @author Taejin Koo
 */
public class AgentDownloadInfo {

    private String version;
    private String downloadUrl;


    public AgentDownloadInfo() {
    }

    public AgentDownloadInfo(String version, String downloadUrl) {
        this.version = version;
        this.downloadUrl = downloadUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentDownloadInfo that = (AgentDownloadInfo) o;

        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        return downloadUrl != null ? downloadUrl.equals(that.downloadUrl) : that.downloadUrl == null;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (downloadUrl != null ? downloadUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentDownloadInfo{");
        sb.append("version='").append(version).append('\'');
        sb.append(", downloadUrl='").append(downloadUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
