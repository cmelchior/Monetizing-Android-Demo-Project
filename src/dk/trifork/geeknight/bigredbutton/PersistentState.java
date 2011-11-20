package dk.trifork.geeknight.bigredbutton;
/**
 * Wrapper for manipulating SharedReferences.
 * Boolean preferences are removed when false to make it harder to decipher
 * the preference file.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AndroidRuntimeException;

public class PersistentState {

	private static final String PREFERENCE_FILE_NAME = "shared_prefs";
	private static SharedPreferences prefs;
	private static Context ctx;
	
	// Preference keys
	private static String PURCHASES_INITIALIZED; // True if market info has been fetched
	private static String APP_UPGRADE; // True if the app has been upgraded	
	
	public static void initialize(Context context) {
		if (context == null) throw new IllegalArgumentException("Context cannot be null");
		ctx = context;
		prefs = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		
		// Create encrypted keys
		PURCHASES_INITIALIZED = getEncryptedKey("purchases_initialized");
		APP_UPGRADE = getEncryptedKey("upgraded");
	}
	
	public static void setPurchasesInitialized(boolean isInitialized) {
		SharedPreferences.Editor editor = prefs.edit();
		if (isInitialized) {
			editor.putBoolean(PURCHASES_INITIALIZED, isInitialized);
		} else {
			editor.remove(PURCHASES_INITIALIZED);
		}
		editor.commit();
	}
	
	public static boolean isPurchasesInitialized() {
		return prefs.getBoolean(PURCHASES_INITIALIZED, false);
	}
	
	public static void setAppUpgraded(boolean isUpgraded) {
		SharedPreferences.Editor editor = prefs.edit();
		if (isUpgraded) {
			editor.putBoolean(APP_UPGRADE, isUpgraded);
		} else {
			editor.remove(APP_UPGRADE);
		}
 		
		editor.commit();
	}
	
	public static boolean isAppUpgraded() {
		return prefs.getBoolean(APP_UPGRADE, false);
	}
	
	/**
	 * Get the encrypted version of a preference key. The encryption is specific
	 * for the device
	 * 
	 * @param key
	 * @return Encrypted key
	 */
	private static String getEncryptedKey(String key) {
		try {
			return SimpleCrypto.encrypt(SimpleCrypto.getDeviceSeed(ctx), key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AndroidRuntimeException("Could not encrypt preference key");
		}
	}
}
