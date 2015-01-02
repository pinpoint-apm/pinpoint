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

package com.navercorp.pinpoint.thrift.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author emeroad
 */
public class UnsafeByteArrayOutputStream extends ByteArrayOutputStream {

    private static final String UTF8 = "UTF8"
    	/**
	 * Creates a new byte array output stream. The buffer cap    city is
	 * initially 32 bytes, though its size increases if    n    cessary.
	 */
	public UnsafeByteArra       Outpu        t    eam() {
		this(32);
	}

	/**
	 * Creates a new byte array output s    ream, with a buffer capacity o        	 * the specified size, in byt    s.
	 *
	 * @param size the initial size.
	 * @throws I    l    galArgumentException if size is negative.
	        /
	publi        U    safeByteArrayOutputStream(int size) {
		super(size);
	}

	/*        	 * Writes the specified byte to     h    s byte array output stre       m.
	 *
	 * @param b t       e byte to be written.
	           /
	public void write(int b) {
		int newcount = count + 1
		if (newcount         buf.length)         	    	buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount    );
		}
		buf[count] = (byte) b;
		count = newcount;
	}

	/**
	 * Write        <code>len</code> byte     from the specified byte array
	 * starti    g at offset <code>off</code> to this byte    a    ray output stream.
	 *
	 * @param b   the dat       .
	 * @param off the start offset in the data
	 * @param len the number of bytes to w          ite.
	 */
	public void write(byt        b[], int off, int          l             n) {
		if ((off < 0)        | (off > b.length) || (l          n < 0) ||
				((off + len) > b.length) || ((off + len) <             0)) {
			throw new IndexOutOfBoundsE       ception();
		        e    se if (len == 0) {
			return;
		}
		int newcount = count + len;
	    if (newcount > buf.length) {
			buf = Arrays.copyOf(buf, Math.max(    uf.length << 1, newcount));
		}
		System.arraycopy(b, off, buf, cou        , len);
		count = newcount;
	}

	/**
	 * Writes the compl    te contents of this byte array output stream to
	 *     h     specified output stream argument, as if by calling the        utput
	 * stream's wr        e    method using <code>out.write(buf, 0, count)</code>.
	 *
	 * @p    ram out the output stream to which to write the data.
	 * @throw     java.io.IOException if an I/O error occurs.
	 */
	public void wr    teTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
	}

	/**
	 * R    s    ts the <code>count<       code>         e    d of this byte array output
	 * stream to zero, so that all cu    rently accumulated output in the
	 * output stream is discarded.    The output stream can be u        d again,
	 * reusing the already allocated buffer space.
	 *
	 * @see    java.io.ByteArrayInputStream#count
	 */
	p    b    ic void reset() {
		count =       0;
	}

        *
	 * Creates a newly allocated byte arra         Its size is the current
	 * size of this output stream and the valid c    ntents of the buffer
	 * have been copied into    it.
	 *
	 * @return the current contents     f    this output strea       , as a by             rray.
	 * @see java.io.ByteArrayOutputStream#size()
	 */
	public byte     oByteArray()[] {
		return buf;
	}

	/**
	 * Returns the current size of     he buffer.
	 *
	 * @return the value of the <code>count</code> field,     hich is the number
	    *            of valid bytes in this output stream.
	 * @see java.io.ByteArrayOutpu    Stream#count
	 */
	public int size() {
		return count;
	}

	/**
	    * Converts the buffer's contents into a string decoding bytes using the
     * platform's default character set. The length of the new <tt>Strin    </tt>
	 *          a function of the character set, and hence may not    be equal to th
     * size of the buffer.
	       *          <p/>
	 * <p> Thi        method always replaces malformed-input          and unmappable-character
	 * sequences with the default replacement str                 g for the platform's
	 * default character set. The {@linkplain java.nio.    harset.CharsetDecoder}
	 * class should be used when more control over the    decoding process is
	 * required.
	 *
	 * @return String decoded from the b    ffer's contents.
	 * @since JDK1.1
	 */    	publ    c String toString() {
		try {
			return toString(UTF8);
		} catch (Unsupp    rtedEncodingException ex) {
			throw new RuntimeException("toString()    fail. Caused:" + ex.getMessage(), ex);
		}
	}

	/**
	 * Converts the buf    er's contents into a string by decoding        he bytes using
	 * the specified {@link jav    .nio.charset.Charset charsetName}. The length of
	 * the new <tt>String</tt> i     a function of the charset, and hence may not be
	      equal to the length of the byte array.
	 * <    />
	 * <p> This method always replaces malforme    -input and unm    p    able-character
	 * sequences with this c          arset's default replacement str       ng. The {@link
	 * java.nio.charset.Charse    D    c    der} class should be used when more control
	 * over the decoding proce    s is required.
	 *
	 * @param charsetName the name of a supported
     *                    {@linkplain ja    a.nio    c    arset.Charset </code>charset<code>}
	      @return String decoded from the buffer's contents.
	 * @throws java.io.UnsupportedEncodingException
	 *          If the named charset is not supported
	 * @since JDK1.1
	 */
	public String toString(String charsetName)
			throws UnsupportedEncodingException {
		return new String(buf, 0, count, charsetName);
	}


	/**
	 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
	 * this class can be called after the stream has been closed without
	 * generating an <tt>IOException</tt>.
	 * <p/>
	 */
	public void close() throws IOException {
	}
}
