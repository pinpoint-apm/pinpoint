/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.agentdir;

import com.navercorp.pinpoint.bootstrap.BootLogger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
final class FileUtils {

    private static final BootLogger logger = BootLogger.getLogger(FileUtils.class.getName());

    private FileUtils() {
    }

    public static File[] listFiles(final File path, final List<String> fileExtensionList) {
        if (path == null) {
            throw new NullPointerException("path must not be null");
        }
        if (fileExtensionList == null) {
            throw new NullPointerException("fileExtensionList must not be null");
        }
        return path.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getName();
                for (String extension : fileExtensionList) {
                    if (path.lastIndexOf(extension) != -1) {
                        return true;
                    }
                }
                return false;
            }
        });
    }


    public static boolean isEmpty(File[] files) {
        return files == null || files.length == 0;
    }


    public static String toCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.warn(file.getPath() + " getCanonicalPath() error. Error:" + e.getMessage(), e);
            return file.getAbsolutePath();
        }
    }



}
