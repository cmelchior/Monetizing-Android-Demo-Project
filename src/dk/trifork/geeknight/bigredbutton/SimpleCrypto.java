package dk.trifork.geeknight.bigredbutton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Usage:
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>

 * @author ferenc.hechler
 * @credit http://www.androidsnippets.com/encryptdecrypt-strings
 * 
 */
public class SimpleCrypto {

	private final static String HEX = "0123456789ABCDEF";
	
	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}
	
	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
	    kgen.init(128, sr); // 192 and 256 bits may not be available
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    return raw;
	}

	
	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}
	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}
	
	public static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
	
	/**
	 * Returns a unique seed based on the device. We want to avoid persisting
	 * this seed, so use the following priority list:
	 * 
	 * 1. Use Settings.Secure.ANDROID_ID (for Android 2.2+)
	 * 2. Use TelephonyManager.getDeviceId() (if != null)
	 * 3. Create a UUID and save in SharedPreferences as deviceId.
	 * 
	 * @author Christian Melchior
	 */
	public static String getDeviceSeed(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		
		String deviceId;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
			// Use System ID (Only reliable for Android 2.2+)
			deviceId = Settings.Secure.ANDROID_ID;
	
		} else if (tm.getDeviceId() != null) {
			// Use SIM card ID 
			deviceId = tm.getDeviceId();
			
		} else {
			// Last resort: create a unique ID for this installation
			SharedPreferences sp = ctx.getSharedPreferences("PREF_UNIQUE_ID", Context.MODE_PRIVATE);
			String uniqueId = sp.getString("PREF_UNIQUE_ID", null);
			if (uniqueId == null) {
				uniqueId = UUID.randomUUID().toString();
				Editor editor = sp.edit();
				editor.putString("PREF_UNIQUE_ID", uniqueId);
				editor.commit();
			}
			
			deviceId = uniqueId;
		}
		
		return createHashedDeviceId(deviceId);
	}
	
	/**
	 * Creates a SHA-1 hash of any deviceId. 
	 * If SHA-1 isn't available, just return the deviceId.
	 * 
	 * @author Christian Melchior
	 */
	private static String createHashedDeviceId(String deviceId) {
		if (deviceId == null) {
			deviceId = "";
		}
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] result = digest.digest(deviceId.getBytes());
			return toHex(result);
			
		} catch (NoSuchAlgorithmException e) {
			return deviceId;
		}
	}
}