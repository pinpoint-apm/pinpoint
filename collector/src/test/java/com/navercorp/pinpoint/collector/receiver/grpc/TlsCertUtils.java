/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TlsCertUtils {

    public static File loadCert(String name) throws IOException {
        InputStream in = new BufferedInputStream(TlsCertUtils.class.getResourceAsStream("/certs/" + name));
        File tmpFile = File.createTempFile(name, "");
        tmpFile.deleteOnExit();

        OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile));
        try {
            int b;
            while ((b = in.read()) != -1) {
                os.write(b);
            }
            os.flush();
        } finally {
            in.close();
            os.close();
        }

        return tmpFile;
    }

    public static List<String> preferredTestCiphers() {
        String[] ciphers;
        try {
            ciphers = SSLContext.getDefault().getDefaultSSLParameters().getCipherSuites();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        List<String> ciphersMinusGcm = new ArrayList<>();
        for (String cipher : ciphers) {
            // The GCM implementation in Java is _very_ slow (~1 MB/s)
            if (cipher.contains("_GCM_")) {
                continue;
            }
            ciphersMinusGcm.add(cipher);
        }
        return Collections.unmodifiableList(ciphersMinusGcm);
    }

    /**
     * Loads an X.509 certificate from the classpath resources in src/main/resources/certs.
     *
     * @param fileName name of a file in src/main/resources/certs.
     */
    public static X509Certificate loadX509Cert(String fileName)
            throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream in = TlsCertUtils.class.getResourceAsStream("/certs/" + fileName);
        try {
            return (X509Certificate) cf.generateCertificate(in);
        } finally {
            in.close();
        }
    }
}
