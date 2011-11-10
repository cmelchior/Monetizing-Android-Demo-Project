package dk.trifork.geeknight.bigredbutton;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements android.view.View.OnClickListener, AdWhirlInterface {
    
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
	public void adWhirlGeneric() {
		// Do nothing
	}
}