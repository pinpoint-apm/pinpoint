package com.navercorp.pinpoint.web.filter;
/*
 * Copyright 2019 NAVER Corp.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * copy org.apache.hadoop.hbase.util.Base64
 *
 */

public class Base64 {
    private static final Log LOG = LogFactory.getLog(Base64.class);
    private static final byte[] _STANDARD_ALPHABET = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte[] _STANDARD_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9};
    private static final byte[] _URL_SAFE_ALPHABET = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
    private static final byte[] _URL_SAFE_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9};
    private static final byte[] _ORDERED_ALPHABET = new byte[]{45, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 95, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};
    private static final byte[] _ORDERED_DECODABET = new byte[]{-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 0, -9, -9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -9, -9, -9, -1, -9, -9, -9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, -9, -9, -9, -9, 37, -9, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -9, -9, -9, -9};

    protected static byte[] getAlphabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_ALPHABET;
        } else {
            return (options & 32) == 32 ? _ORDERED_ALPHABET : _STANDARD_ALPHABET;
        }
    }

    protected static byte[] getDecodabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_DECODABET;
        } else {
            return (options & 32) == 32 ? _ORDERED_DECODABET : _STANDARD_DECODABET;
        }
    }

    private Base64() {
    }

    private static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java Base64 -e|-d inputfile outputfile");
    }

    protected static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options) {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
        return b4;
    }

    protected static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options) {
        byte[] ALPHABET = getAlphabet(options);
        int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0) | (numSigBytes > 1 ? source[srcOffset + 1] << 24 >>> 16 : 0) | (numSigBytes > 2 ? source[srcOffset + 2] << 24 >>> 24 : 0);
        switch(numSigBytes) {
            case 1:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
                destination[destOffset + 2] = 61;
                destination[destOffset + 3] = 61;
                return destination;
            case 2:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
                destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 63];
                destination[destOffset + 3] = 61;
                return destination;
            case 3:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
                destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 63];
                destination[destOffset + 3] = ALPHABET[inBuff & 63];
                return destination;
            default:
                return destination;
        }
    }

    public static String encodeObject(Serializable serializableObject) {
        return encodeObject(serializableObject, 0);
    }

    public static String encodeObject(Serializable serializableObject, int options) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = null;
        ObjectOutputStream oos = null;

        String var6;
        try {
            b64os = new Base64.Base64OutputStream(baos, 1 | options);
            oos = (options & 2) == 2 ? new ObjectOutputStream(new GZIPOutputStream(b64os)) : new ObjectOutputStream(b64os);
            oos.writeObject(serializableObject);
            String var5 = new String(baos.toByteArray(), "UTF-8");
            return var5;
        } catch (UnsupportedEncodingException var28) {
            var6 = new String(baos.toByteArray());
            return var6;
        } catch (IOException var29) {
            LOG.error("error encoding object", var29);
            var6 = null;
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception var27) {
                    LOG.error("error closing ObjectOutputStream", var27);
                }
            }

            if (b64os != null) {
                try {
                    b64os.close();
                } catch (Exception var26) {
                    LOG.error("error closing Base64OutputStream", var26);
                }
            }

            try {
                baos.close();
            } catch (Exception var25) {
                LOG.error("error closing ByteArrayOutputStream", var25);
            }

        }

        return var6;
    }

    public static String encodeBytes(byte[] source) {
        return encodeBytes(source, 0, source.length, 0);
    }

    public static String encodeBytes(byte[] source, int options) {
        return encodeBytes(source, 0, source.length, options);
    }

    public static String encodeBytes(byte[] source, int off, int len) {
        return encodeBytes(source, off, len, 0);
    }

    public static String encodeBytes(byte[] source, int off, int len, int options) {
        if ((options & 2) == 2) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = null;

            String var33;
            try {
                gzos = new GZIPOutputStream(new Base64.Base64OutputStream(baos, 1 | options));
                gzos.write(source, off, len);
                gzos.close();
                gzos = null;
                String var32 = new String(baos.toByteArray(), "UTF-8");
                return var32;
            } catch (UnsupportedEncodingException var27) {
                var33 = new String(baos.toByteArray());
            } catch (IOException var28) {
                LOG.error("error encoding byte array", var28);
                var33 = null;
                return var33;
            } finally {
                if (gzos != null) {
                    try {
                        gzos.close();
                    } catch (Exception var25) {
                        LOG.error("error closing GZIPOutputStream", var25);
                    }
                }

                try {
                    baos.close();
                } catch (Exception var24) {
                    LOG.error("error closing ByteArrayOutputStream", var24);
                }

            }

            return var33;
        } else {
            boolean breakLines = (options & 8) == 0;
            int len43 = len * 4 / 3;
            byte[] outBuff = new byte[len43 + (len % 3 > 0 ? 4 : 0) + (breakLines ? len43 / 76 : 0)];
            int d = 0;
            int e = 0;
            int len2 = len - 2;

            for(int lineLength = 0; d < len2; e += 4) {
                encode3to4(source, d + off, 3, outBuff, e, options);
                lineLength += 4;
                if (breakLines && lineLength == 76) {
                    outBuff[e + 4] = 10;
                    ++e;
                    lineLength = 0;
                }

                d += 3;
            }

            if (d < len) {
                encode3to4(source, d + off, len - d, outBuff, e, options);
                e += 4;
            }

            try {
                return new String(outBuff, 0, e, "UTF-8");
            } catch (UnsupportedEncodingException var26) {
                return new String(outBuff, 0, e);
            }
        }
    }

    protected static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options) {
        byte[] DECODABET = getDecodabet(options);
        int outBuff;
        if (source[srcOffset + 2] == 61) {
            outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12;
            destination[destOffset] = (byte)(outBuff >>> 16);
            return 1;
        } else if (source[srcOffset + 3] == 61) {
            outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12 | (DECODABET[source[srcOffset + 2]] & 255) << 6;
            destination[destOffset] = (byte)(outBuff >>> 16);
            destination[destOffset + 1] = (byte)(outBuff >>> 8);
            return 2;
        } else {
            try {
                outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12 | (DECODABET[source[srcOffset + 2]] & 255) << 6 | DECODABET[source[srcOffset + 3]] & 255;
                destination[destOffset] = (byte)(outBuff >> 16);
                destination[destOffset + 1] = (byte)(outBuff >> 8);
                destination[destOffset + 2] = (byte)outBuff;
                return 3;
            } catch (Exception var7) {
                LOG.error("error decoding bytes at " + source[srcOffset] + ": " + DECODABET[source[srcOffset]] + ", " + source[srcOffset + 1] + ": " + DECODABET[source[srcOffset + 1]] + ", " + source[srcOffset + 2] + ": " + DECODABET[source[srcOffset + 2]] + ", " + source[srcOffset + 3] + ": " + DECODABET[source[srcOffset + 3]], var7);
                return -1;
            }
        }
    }

    public static byte[] decode(byte[] source, int off, int len, int options) {
        byte[] DECODABET = getDecodabet(options);
        int len34 = len * 3 / 4;
        byte[] outBuff = new byte[len34];
        int outBuffPosn = 0;
        byte[] b4 = new byte[4];
        int b4Posn = 0;

        for(int i = off; i < off + len; ++i) {
            byte sbiCrop = (byte)(source[i] & 127);
            byte sbiDecode = DECODABET[sbiCrop];
            if (sbiDecode < -5) {
                LOG.error("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
                return null;
            }

            if (sbiDecode >= -1) {
                b4[b4Posn++] = sbiCrop;
                if (b4Posn > 3) {
                    outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options);
                    b4Posn = 0;
                    if (sbiCrop == 61) {
                        break;
                    }
                }
            }
        }

        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }

    public static byte[] decode(String s) {
        return decode(s, 0);
    }

    public static byte[] decode(String s, int options) {
        byte[] bytes;
        try {
            bytes = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException var21) {
            bytes = s.getBytes();
        }

        bytes = decode(bytes, 0, bytes.length, options);
        if (bytes != null && bytes.length >= 4) {
            int head = bytes[0] & 255 | bytes[1] << 8 & '\uff00';
            if (35615 == head) {
                GZIPInputStream gzis = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                    gzis = new GZIPInputStream(new ByteArrayInputStream(bytes));
                    byte[] buffer = new byte[2048];

                    int length;
                    while((length = gzis.read(buffer)) >= 0) {
                        baos.write(buffer, 0, length);
                    }

                    bytes = baos.toByteArray();
                } catch (IOException var22) {
                    ;
                } finally {
                    try {
                        baos.close();
                    } catch (Exception var20) {
                        LOG.error("error closing ByteArrayOutputStream", var20);
                    }

                    if (gzis != null) {
                        try {
                            gzis.close();
                        } catch (Exception var19) {
                            LOG.error("error closing GZIPInputStream", var19);
                        }
                    }

                }
            }
        }

        return bytes;
    }

    public static class Base64OutputStream extends FilterOutputStream {
        private boolean encode;
        private int position;
        private byte[] buffer;
        private int bufferLength;
        private int lineLength;
        private boolean breakLines;
        private byte[] b4;
        private boolean suspendEncoding;
        private int options;
        private byte[] decodabet;

        public Base64OutputStream(OutputStream out) {
            this(out, 1);
        }

        public Base64OutputStream(OutputStream out, int options) {
            super(out);
            this.breakLines = (options & 8) != 8;
            this.encode = (options & 1) == 1;
            this.bufferLength = this.encode ? 3 : 4;
            this.buffer = new byte[this.bufferLength];
            this.position = 0;
            this.lineLength = 0;
            this.suspendEncoding = false;
            this.b4 = new byte[4];
            this.options = options;
            this.decodabet = Base64.getDecodabet(options);
        }

        public void write(int theByte) throws IOException {
            if (this.suspendEncoding) {
                super.out.write(theByte);
            } else {
                if (this.encode) {
                    this.buffer[this.position++] = (byte)theByte;
                    if (this.position >= this.bufferLength) {
                        this.out.write(Base64.encode3to4(this.b4, this.buffer, this.bufferLength, this.options));
                        this.lineLength += 4;
                        if (this.breakLines && this.lineLength >= 76) {
                            this.out.write(10);
                            this.lineLength = 0;
                        }

                        this.position = 0;
                    }
                } else if (this.decodabet[theByte & 127] > -5) {
                    this.buffer[this.position++] = (byte)theByte;
                    if (this.position >= this.bufferLength) {
                        int len = Base64.decode4to3(this.buffer, 0, this.b4, 0, this.options);
                        this.out.write(this.b4, 0, len);
                        this.position = 0;
                    }
                } else if (this.decodabet[theByte & 127] != -5) {
                    throw new IOException("Invalid character in Base64 data.");
                }

            }
        }

        public void write(byte[] theBytes, int off, int len) throws IOException {
            if (this.suspendEncoding) {
                super.out.write(theBytes, off, len);
            } else {
                for(int i = 0; i < len; ++i) {
                    this.write(theBytes[off + i]);
                }

            }
        }

        public void flushBase64() throws IOException {
            if (this.position > 0) {
                if (!this.encode) {
                    throw new IOException("Base64 input not properly padded.");
                }

                this.out.write(Base64.encode3to4(this.b4, this.buffer, this.position, this.options));
                this.position = 0;
            }

        }

        public void close() throws IOException {
            this.flushBase64();
            super.close();
            this.buffer = null;
            this.out = null;
        }

        public void suspendEncoding() throws IOException {
            this.flushBase64();
            this.suspendEncoding = true;
        }

        public void resumeEncoding() {
            this.suspendEncoding = false;
        }
    }

}
