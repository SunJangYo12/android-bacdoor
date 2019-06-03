package com.google.play.ngrok;

import java.nio.ByteBuffer;

public class BytesUtil {

	public static byte[] myaddBytes(byte[] dest, int pos, byte[] src, int len) {

		for (int i = 0; i < len; i++) {
			dest[pos + i] = src[i];
		}

		return dest;
	}

	public static byte[] addBytesnew(int maxlength, byte[]... src) {
		int length = 0;
		int index = 0;
		byte[] dest = new byte[maxlength];
		for (int i = 0; i < src.length; i++) {
			length = src[i].length;
			System.arraycopy(src[i], 0, dest, index, length);
			index = index + length;
		}
		return dest;
	}

	public static byte[] cutOutByte(byte[] b, int start, int len) {
		if (b.length == 0 || len == 0 || start >= b.length) {
			return null;
		}
		byte[] bjq = new byte[len];
		for (int i = 0; i < len; i++) {
			bjq[i] = b[start + i];
		}
		return bjq;
	}

	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(0, x);
		return buffer.array();
	}

	public static byte[] longToBytes(long x, int pos) {

		byte[] bytes = longToBytes(x);
		byte[] back = new byte[8];
		for (int i = 0; i < 8; i++) {
			back[i] = bytes[(7 - i)];
		}
		return back;
	}

	public static byte[] leTobe(byte[] src, int len) {
		byte[] back = new byte[len];
		for (int i = 0; i < len; i++) {
			back[i] = src[(len - 1 - i)];
		}
		return back;
	}

	public static String printHexString(byte[] b) {
		String hexecho = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);

			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			hexecho = hexecho + hex.toUpperCase() + ":";
		}
		return hexecho;
	}

	public static String printHexString(byte[] b, int len) {
		String hexecho = "";
		for (int i = 0; i < len; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);

			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			hexecho = hexecho + hex.toUpperCase() + ":";
		}
		return hexecho;
	}

	public static byte[] short2Byte(short l) {

		byte[] b = new byte[2];
		b[0] = new Integer(l >> 8).byteValue();
		b[1] = new Integer(l).byteValue();
		return b;
	}

	public static byte[] int2Byte(int n) {
		byte[] buf = new byte[4];
		buf[0] = new Integer(n >> 24).byteValue(); // (byte) (n >> 24);
		buf[1] = new Integer(n >> 16).byteValue();
		buf[2] = new Integer(n >> 8).byteValue();
		buf[3] = new Integer(n).byteValue();
		return buf;
	}

	public static int bytesToInt(byte[] bytes) {

		int addr = bytes[0] & 0xFF;

		addr |= ((bytes[1] << 8) & 0xFF00);

		addr |= ((bytes[2] << 16) & 0xFF0000);

		addr |= ((bytes[3] << 24) & 0xFF000000);

		return addr;

	}

	public static int bytesToInt(byte b[], int offset) {
		if (b == null) {
			return 0;
		}
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8
				| (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
	}

	public static long bytes2long(byte[] b) {

		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 8; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static long bytes2long(byte[] array, int offset) {
		if (array.length < 8) {
			return 0;
		}

		return ((((long) array[offset + 0] & 0xff) << 56)
				| (((long) array[offset + 1] & 0xff) << 48)
				| (((long) array[offset + 2] & 0xff) << 40)
				| (((long) array[offset + 3] & 0xff) << 32)
				| (((long) array[offset + 4] & 0xff) << 24)
				| (((long) array[offset + 5] & 0xff) << 16)
				| (((long) array[offset + 6] & 0xff) << 8) | (((long) array[offset + 7] & 0xff) << 0));
	}

	public static short bytesToShort(byte[] b) {
		return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
	}

	public static short bytesToShort(byte[] b, int offset) {
		return (short) (b[offset + 1] & 0xff | (b[offset] & 0xff) << 8);
	}

	public static short bytesTonum(byte[] b) {
		return (short) ((b[0] & 0xff));
	}

	public static byte[] numtobytes(int i) {
		byte[] xx = new byte[1];
		xx[0] = (byte) i;
		return xx;
	}

	public static String byte2hex(byte[] b) {

		String hs = "";
		String tmp = "";
		for (int n = 0; n < b.length; n++) {
			tmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (tmp.length() == 1) {
				hs = hs + "0" + tmp;
			} else {
				hs = hs + tmp;
			}
		}
		tmp = null;
		return hs.toUpperCase();
	}

}
