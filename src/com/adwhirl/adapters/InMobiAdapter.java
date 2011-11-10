package com.adwhirl.adapters;

import android.app.Activity;
import android.widget.LinearLayout;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlLayout.ViewAdRunnable;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlTargeting.Gender;
import com.adwhirl.Logger;
import com.adwhirl.obj.Extra;
import com.adwhirl.obj.Ration;
import com.adwhirl.util.AdWhirlUtil;
import com.inmobi.androidsdk.IMAdListener;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdRequest.ErrorCode;
import com.inmobi.androidsdk.IMAdRequest.GenderType;
import com.inmobi.androidsdk.IMAdView;

/**
 * An adapter for the InMobi Android SDK.
 * Note: The InMobi site Id is looked up using ration.key
 * 
 * Converted to InMobi SDK300 by Christian Melchior
 */

public final class InMobiAdapter extends AdWhirlAdapter implements IMAdListener {
	private Extra extra = null;
	public int adSize = IMAdView.INMOBI_AD_UNIT_320X50;

	public InMobiAdapter(AdWhirlLayout adWhirlLayout, Ration ration) {
		super(adWhirlLayout, ration);
		extra = adWhirlLayout.extra;
	}

	@Override
	public void handle() {
		Logger.i(AdWhirlUtil.ADWHIRL, "inMobiAdapter tries to handle ad request");
		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}

		Activity activity = adWhirlLayout.activityReference.get();
		if (activity == null) {
			return;
		}

		IMAdView adView = new IMAdView(activity, adSize, ration.key); 
		adView.setRefreshInterval(IMAdView.REFRESH_INTERVAL_OFF);
		adView.setIMAdListener(this);
		
		final float scale = activity.getResources().getDisplayMetrics().density;
		int width = (int) (320 * scale + 0.5f);
		int height = (int) (50 * scale + 0.5f);
		adView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
		
		// Initialize add request
		IMAdRequest request = new IMAdRequest();
		request.setAge(AdWhirlTargeting.getAge());
		request.setAreaCode(null);
		request.setCurrentLocation(null);
		request.setDateOfBirth(null);
		request.setEducation(null);
		request.setEthnicity(null);
		request.setGender(getInMobiGenderFromAdWhirl());
		request.setIncome(0);
		request.setInterests(null);
		request.setLocationInquiryAllowed((extra.locationOn == 1) ? true : false);
		request.setKeywords(AdWhirlTargeting.getKeywords());
		request.setPostalCode(AdWhirlTargeting.getPostalCode());
		request.setSearchString(null);
		request.setTestMode(AdWhirlTargeting.getTestMode());

		adView.setIMAdRequest(request);
		adView.loadNewAd();
	}

	/**
	 * Convert AdWhirl gender type to InMobi gender typer
	 */
	private GenderType getInMobiGenderFromAdWhirl() {
		Gender gender = AdWhirlTargeting.getGender();
		if (Gender.MALE == gender) {
			return GenderType.MALE;
		}
		if (Gender.FEMALE == gender) {
			return GenderType.FEMALE;
		}
		return GenderType.NONE;
	}

	@Override
	public void onAdRequestCompleted(IMAdView adView) {
		Logger.d(AdWhirlUtil.ADWHIRL, "InMobi success");

		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}

		adWhirlLayout.adWhirlManager.resetRollover();
		adWhirlLayout.handler.post(new ViewAdRunnable(adWhirlLayout, adView));
		adWhirlLayout.rotateThreadedDelayed();
	}

	@Override
	public void onAdRequestFailed(IMAdView adView, ErrorCode errorCode) {
		Logger.d(AdWhirlUtil.ADWHIRL, "InMobi failure: " + errorCode);
		
		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}
		adWhirlLayout.rollover();
	}

	@Override
	public void onDismissAdScreen(IMAdView arg0) {
		// Do nothing
	}

	@Override
	public void onShowAdScreen(IMAdView arg0) {
		// Do nothing
	}

}
