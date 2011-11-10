package dk.trifork.geeknight.bigredbutton;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements android.view.View.OnClickListener {
    
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
        button = (ImageButton) findViewById(R.id.red_button);
        
        // Setup button texts
        textList = getResources().getStringArray(R.array.button_texts); 
        currentButtonNumber = -1;
        
        // Setup event handlers
        button.setOnClickListener(this);
    }

    /**
     * Handle button clicks. Loop button texts inifinitly.
     */
	@Override
	public void onClick(View v) {
		currentButtonNumber = ++currentButtonNumber % textList.length;
		buttonText.setText(textList[currentButtonNumber]);
	}
}