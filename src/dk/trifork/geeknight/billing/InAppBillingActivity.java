package dk.trifork.geeknight.billing;
/**
 * Abstract class encapsulating in-app billing.
 * Based on the Dungeons In-app Billing example from Google.
 * 
 * @see http://developer.android.com/guide/market/billing/billing_integrate.html#billing-download
 * 
 * @author Christian Melchior
 */
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.adwhirl.Logger;

import dk.trifork.geeknight.bigredbutton.R;
import dk.trifork.geeknight.billing.Consts.PurchaseState;
import dk.trifork.geeknight.billing.Consts.ResponseCode;
import dk.trifork.geeknight.billing.requests.RequestPurchase;
import dk.trifork.geeknight.billing.requests.RestoreTransactions;

public abstract class InAppBillingActivity extends Activity implements OnClickListener {

	private final static String LOG_TAG = "InAppBilling";
	
    /**
     * Used for storing the log text.
     */
    private InAppPurchaseObserver mPurchaseObserver;
    private Handler mHandler;

    private BillingService mBillingService;
    private PurchaseDatabase mPurchaseDatabase;
    
    private static final int DIALOG_CANNOT_CONNECT_ID = 1;
    private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 2;

    /**
     * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
     * messages to this application so that we can update the UI.
     */
    private class InAppPurchaseObserver extends PurchaseObserver {
        public InAppPurchaseObserver(Handler handler) {
            super(InAppBillingActivity.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported) {
        	if (!supported) {
        		showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
        	}
        	Logger.d(LOG_TAG, "onBillingSupported: " + supported);
        	InAppBillingActivity.this.onBillingSupported(supported);
        }

        /**
         * This is called when the state of a purchase is updated
         */
        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
                int quantity, long purchaseTime, String developerPayload) {
        	Logger.d(LOG_TAG, "onPurchaseStateChange: " + itemId + ", " + purchaseState);
        	InAppBillingActivity.this.onPurchaseStateChange(purchaseState, itemId, quantity, purchaseTime, developerPayload);
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request,
                ResponseCode responseCode) {
        	Logger.d(LOG_TAG, "onPurchaseStateChange: " + request.mProductId + ", " + responseCode);
        	InAppBillingActivity.this.onRequestPurchaseResponse(request, responseCode);
        }

        @Override	
        public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
        	Logger.d(LOG_TAG, "onRestoreTransactionsResponse: " + responseCode);
        	InAppBillingActivity.this.onRestoreTransactionsResponse(request, responseCode);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHandler = new Handler();
        mPurchaseObserver = new InAppPurchaseObserver(mHandler);
        mBillingService = new BillingService();
        mBillingService.setContext(this);

        mPurchaseDatabase = new PurchaseDatabase(this);
        
        // Check if billing is supported.
        ResponseHandler.register(mPurchaseObserver);
        if (!mBillingService.checkBillingSupported()) {
            showDialog(DIALOG_CANNOT_CONNECT_ID);
        }
    }

    protected abstract void onBillingSupported(boolean supported);
    protected abstract void onPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload);
    protected abstract void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode);
    protected abstract void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode);    

    /**
     * Called when this activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        ResponseHandler.register(mPurchaseObserver);
    }

    /**
     * Called when this activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        ResponseHandler.unregister(mPurchaseObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPurchaseDatabase.close();
        mBillingService.unbind();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_CANNOT_CONNECT_ID:
            return createDialog(R.string.cannot_connect_title, R.string.cannot_connect_message);
        case DIALOG_BILLING_NOT_SUPPORTED_ID:
            return createDialog(R.string.billing_not_supported_title, R.string.billing_not_supported_message);
        default:
            return null;
        }
    }

    private Dialog createDialog(int titleId, int messageId) {
        String helpUrl = replaceLanguageAndRegion(getString(R.string.help_url));
        final Uri helpUri = Uri.parse(helpUrl);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleId)
            .setIcon(android.R.drawable.stat_sys_warning)
            .setMessage(messageId)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.learn_more, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, helpUri);
                    startActivity(intent);
                }
            });
        return builder.create();
    }

    /**
     * Replaces the language and/or country of the device into the given string.
     * The pattern "%lang%" will be replaced by the device's language code and
     * the pattern "%region%" will be replaced with the device's country code.
     *
     * @param str the string to replace the language/country within
     * @return a string containing the local language and region codes
     */
    private String replaceLanguageAndRegion(String str) {
        // Substitute language and or region if present in string
        if (str.contains("%lang%") || str.contains("%region%")) {
            Locale locale = Locale.getDefault();
            str = str.replace("%lang%", locale.getLanguage().toLowerCase());
            str = str.replace("%region%", locale.getCountry().toLowerCase());
        }
        return str;
    }

    /**
     * If the database has not been initialized, we send a
     * RESTORE_TRANSACTIONS request to Android Market to get the list of purchased items
     * for this user. This happens if the application has just been installed
     * or the user wiped data. We do not want to do this on every startup, rather, we want to do
     * only when the database needs to be initialized.
     */
    protected void restorePurchases() {
    	mBillingService.restoreTransactions();
        Toast.makeText(this, R.string.restoring_transactions, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 
     * Execute a asynchronously purchase requests.
     */
    protected void requestPurchase(String productId, String developerPayload) {
    	if (!mBillingService.requestPurchase(productId, developerPayload)) {
    		showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
    	}
    }
    
    protected void requestPurchase(String productId) {
    	requestPurchase(productId, null);
    }
    
    /**
     * Check if a given item is purchased
     */
    protected boolean isPurchased(String productId) {
    	final Cursor c = mPurchaseDatabase.queryTransactions(productId, PurchaseState.PURCHASED);
    	int count = 0;
    	if (c != null) {
    		count = c.getCount();
    		c.close();
    	}
    	return count > 0;
    }
}