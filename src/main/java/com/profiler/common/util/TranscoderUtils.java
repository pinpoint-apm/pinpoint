package com.profiler.common.util;

public final class TranscoderUtils {
	private final boolean packZeros;

	/**
	 * Get an instance of TranscoderUtils.
	 * 
	 * @param pack
	 *            if true, remove all zero bytes from the MSB of the packed num
	 */
	public TranscoderUtils(boolean pack) {
		super();
		packZeros = pack;
	}

	public byte[] encodeNum(long l, int maxBytes) {
		byte[] rv = new byte[maxBytes];
		for (int i = 0; i < rv.length; i++) {
			int pos = rv.length - i - 1;
			rv[pos] = (byte) ((l >> (8 * i)) & 0xff);
		}
		if (packZeros) {
			int firstNon0 = 0;
			for (; firstNon0 < rv.length && rv[firstNon0] == 0; firstNon0++) {
				// Just looking for what we can reduce
			}
			if (firstNon0 > 0) {
				byte[] tmp = new byte[rv.length - firstNon0];
				System.arraycopy(rv, firstNon0, tmp, 0, rv.length - firstNon0);
				rv = tmp;
			}
		}
		return rv;
	}

	public byte[] encodeLong(long l) {
		return encodeNum(l, 8);
	}

	public long decodeLong(byte[] b) {
		long rv = 0;
		for (byte i : b) {
			rv = (rv << 8) | (i < 0 ? 256 + i : i);
		}
		return rv;
	}

	public byte[] encodeInt(int in) {
		return encodeNum(in, 4);
	}

	public int decodeInt(byte[] in) {
		assert in.length <= 4 : "Too long to be an int (" + in.length + ") bytes";
		return (int) decodeLong(in);
	}

	public byte[] encodeByte(byte in) {
		return new byte[] { in };
	}

	public byte decodeByte(byte[] in) {
		assert in.length <= 1 : "Too long for a byte";
		byte rv = 0;
		if (in.length == 1) {
			rv = in[0];
		}
		return rv;
	}

	public byte[] encodeBoolean(boolean b) {
		byte[] rv = new byte[1];
		rv[0] = (byte) (b ? '1' : '0');
		return rv;
	}

	public boolean decodeBoolean(byte[] in) {
		assert in.length == 1 : "Wrong length for a boolean";
		return in[0] == '1';
	}
}
