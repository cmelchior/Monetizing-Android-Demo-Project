package dk.trifork.geeknight.bigredbutton;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;

import dk.trifork.geeknight.billing.Consts.PurchaseState;
import dk.trifork.geeknight.billing.Consts.ResponseCode;
import dk.trifork.geeknight.billing.InAppBillingActivity;
import dk.trifork.geeknight.billing.requests.RequestPurchase;
import dk.trifork.geeknight.billing.requests.RestoreTransactions;

public class MainActivity extends InAppBillingActivity implements android.view.View.OnClickListener, AdWhirlInterface {

	private static final int OPTION_RESTORE = 0;
	private static final int OPTION_PURCHASE = 1;
	private static final int OPTION_TEST_PURCHASED = 2;
	private static final int OPTION_TEST_CANCELED = 3;
	private static final int OPTION_TEST_REFUNDED = 4;
	private static final int OPTION_TEST_ITEM_UNAVAILABLE = 5;
	
	private TextView buttonText;
	private ImageButton button;
	private String[] textList;
	private int currentButtonNumber;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Set view references
        buttonText = (TextView) findViewById(R.id.button_text);
        buttonText.setText("");
        button = (ImageButton) findViewById(R.id.red_button);
        
        // Setup button texts
        textList = getResources().getStringArray(R.array.button_texts); 
        currentButtonNumber = -1;
        
        // Setup event handlers
        button.setOnClickListener(this);
        
        // Replace button with a green if upgrade has been bought
        if(isPurchased("upgrade")) {
        	button.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_button));
        }
        	
        // Initialize ads
        initializeAdWhirl();
    }

    /**
     * Handle button clicks. Loop button texts inifinitly.
     */
	@Override
	public void onClick(View v) {
		currentButtonNumber = ++currentButtonNumber % textList.length;
		buttonText.setText(textList[currentButtonNumber]);
	}
	
	/**
	 * Initializes the AdWhirl component and start displaying ads
	 */
	private void initializeAdWhirl() {
		FrameLayout container = (FrameLayout) findViewById(R.id.ads);
		
		// Setup AdWhirl
		AdWhirlManager.setConfigExpireTimeout(1*60*60*1000); // Timeout - 1h
		AdWhirlTargeting.setTestMode(true);
		AdWhirlTargeting.setKeywords("red button fun game");
		
		// Setup 
		AdWhirlLayout adsLayout = new AdWhirlLayout(this, "ef8147ec68264bce86cd371739f3f8fb");
		int diWidth = 320;
		int diHeight = 52;
		float density = getResources().getDisplayMetrics().density;

		adsLayout.setAdWhirlInterface(this);
		adsLayout.setMaxWidth((int)(diWidth * density));
		adsLayout.setMaxHeight((int)(diHeight * density));
		container.addView(adsLayout);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, OPTION_RESTORE, 0, "Restore");
		menu.add(0, OPTION_PURCHASE, 0, "Upgrade");
		menu.add(0, OPTION_TEST_PURCHASED, 0, "android.test.purchased");
		menu.add(0, OPTION_TEST_CANCELED, 0, "android.test.canceled");
		menu.add(0, OPTION_TEST_REFUNDED, 0, "android.test.refunded");
		menu.add(0, OPTION_TEST_ITEM_UNAVAILABLE, 0, "android.test.item_unavailable");

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case OPTION_RESTORE: restorePurchases();
		case OPTION_PURCHASE: requestPurchase("upgrade");
		case OPTION_TEST_PURCHASED: requestPurchase("android.test.purchased"); break;
		case OPTION_TEST_CANCELED: requestPurchase("android.test.canceled"); break;
		case OPTION_TEST_REFUNDED: requestPurchase("android.test.refunded"); break;
		case OPTION_TEST_ITEM_UNAVAILABLE: requestPurchase("android.test.item_unavailable"); break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	

	@Override
	public void adWhirlGeneric() {
		// Do nothing
	}

	@Override
	protected void onPurchaseStateChange(PurchaseState purchaseState, String itemId, 
			int quantity, long purchaseTime, String developerPayload) {

		if (itemId.equals("android.test.purchased")) {
			if (purchaseState == PurchaseState.PURCHASED) {
				PersistentState.setAppUpgraded(true);
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_button));

			} else {
				PersistentState.setAppUpgraded(false);
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_button));
			}
		}
		
		// Test state changes (should be removed in a real application
		if (itemId.equals("android.test.refunded") && purchaseState == PurchaseState.REFUNDED) {
			PersistentState.setAppUpgraded(false);
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_button));
		}
	}

	@Override
	protected void onBillingSupported(boolean supported) {
		// Do nothing
	}

	@Override
	protected void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
		if (responseCode != ResponseCode.RESULT_OK) {
			// Request to purchase failed
			// Chance to show a message to the user
		}
	}

	@Override
	protected void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
		if (responseCode == ResponseCode.RESULT_OK) {
			PersistentState.setPurchasesInitialized(true);
		}
	}
}