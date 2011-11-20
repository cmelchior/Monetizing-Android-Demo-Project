package dk.trifork.geeknight.bigredbutton;
import android.app.Application;
/**
 * Global context which will always be created before any activities.
 */
public class GlobalApplicationContext extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		PersistentState.initialize(this);
	}
}