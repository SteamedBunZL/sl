package com.tuixin11sms.tx.core;

import java.io.UnsupportedEncodingException;

import com.tuixin11sms.tx.utils.Utils;

/**
 * This is a simple implementation of the RC4 (tm) encryption algorithm. The
 * author implemented this class for some simple applications that don't
 * need/want/require the Sun's JCE framework.
 * <p>
 * But if you are looking for encryption algorithms for a full-blown
 * application, it would be better to stick with Sun's JCE framework. You can
 * find a *free* JCE implementation with RC4 (tm) at Cryptix
 * (http://www.cryptix.org/).
 * <p>
 * Note that RC4 (tm) is a trademark of RSA Data Security, Inc. Also, if you are
 * within USA, you may need to acquire licenses from RSA to use RC4. Please
 * check your local law. The author is not responsible for any illegal use of
 * this code.
 * <p>
 * 
 * @author Clarence Ho
 */
public class RC4R {
	private byte state[] = new byte[256];
	private int x;
	private int y;
	private byte link;

	public RC4R() {
	}

	/**
	 * Initializes the class with a string key. The length of a normal key
	 * should be between 1 and 2048 bits. But this method doens't check the
	 * length at all.
	 * 
	 * @param key
	 *            the encryption/decryption key
	 */
	public RC4R(String key) throws NullPointerException {
		InitKey(key.getBytes());
	}

	/**
	 * Initializes the class with a byte array key. The length of a normal key
	 * should be between 1 and 2048 bits. But this method doens't check the
	 * length at all.
	 * 
	 * @param key
	 *            the encryption/decryption key
	 */
	public RC4R(byte[] key) throws NullPointerException {
		InitKey(key);
	}

	public synchronized void InitKey(byte[] key) {
		for (int i = 0; i < 256; i++) {
			state[i] = (byte) i;
		}

		x = 0;
		y = 0;
		link = 0;

		int index1 = 0;
		int index2 = 0;

		byte tmp;

		if (key == null || key.length == 0) {
			throw new NullPointerException();
		}

		for (int i = 0; i < 256; i++) {

			index2 = ((key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;

			tmp = state[i];
			state[i] = state[index2];
			state[index2] = tmp;

			index1 = (index1 + 1) % key.length;
		}
	}

	public synchronized byte[] encrypt(String data) {
		if (data == null) {
			return null;
		}
		byte[] tmp;
		try {
			tmp = data.getBytes("UTF-8");
			tmp = this.encrypt(tmp);
			return tmp;
		} catch (UnsupportedEncodingException e) {
			if(Utils.debug)e.printStackTrace();
		}
		return null;
	}

	public synchronized byte[] encrypt(byte[] buf) {
		byte[] tmp = this.rc4(buf);
		for (int i = 0; i < buf.length; i++) {
			tmp[i] = (byte) (tmp[i] ^ link);
			link = tmp[i];
		}
		return tmp;
	}

	public synchronized byte[] decrypt(String data) {
		if (data == null) {
			return null;
		}
		byte[] tmp = data.getBytes();
		tmp = this.decrypt(tmp);
		return tmp;
	}

	public synchronized byte[] decrypt(byte[] buf) {
		byte[] tmp = buf;
		for (int i = 0; i < buf.length; i++) {
			byte t = tmp[i];
			tmp[i] = (byte) (tmp[i] ^ link);
			link = t;
		}
		tmp = this.rc4(buf);

		return tmp;
	}

	/**
	 * RC4 encryption/decryption.
	 * 
	 * @param buf
	 *            the data to be encrypted/decrypted
	 * @return the result of the encryption/decryption
	 */
	private byte[] rc4(byte[] buf) {
		int xorIndex;
		byte tmp;

		if (buf == null) {
			return null;
		}

		byte[] result = new byte[buf.length];

		for (int i = 0; i < buf.length; i++) {

			x = (x + 1) & 0xff;
			y = ((state[x] & 0xff) + y) & 0xff;

			tmp = state[x];
			state[x] = state[y];
			state[y] = tmp;

			xorIndex = ((state[x] & 0xff) + (state[y] & 0xff)) & 0xff;
			result[i] = (byte) (buf[i] ^ state[xorIndex]);
		}
		return result;
	}
}