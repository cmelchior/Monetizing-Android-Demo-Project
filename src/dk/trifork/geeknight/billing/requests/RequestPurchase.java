package dk.trifork.geeknight.billing.requests;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

import com.adwhirl.Logger;
import com.android.vending.billing.IMarketBillingService;

import dk.trifork.geeknight.billing.Consts;
import dk.trifork.geeknight.billing.ResponseHandler;
import dk.trifork.geeknight.billing.Consts.ResponseCode;

/**
 * Wrapper class that requests a purchase.
 */
public class RequestPurchase extends BillingRequest {
	
    public final String mProductId;
    public final String mDeveloperPayload;

    public RequestPurchase(String itemId) {
        this(itemId, null);
    }

    public RequestPurchase(String itemId, String developerPayload) {
        // This object is never created as a side effect of starting this
        // service so we pass -1 as the startId to indicate that we should
        // not stop this service after executing this request.
        super(-1);
        mProductId = itemId;
        mDeveloperPayload = developerPayload;
    }

    @Override
    protected long run(IMarketBillingService service) throws RemoteException {
        Bundle request = makeRequestBundle("REQUEST_PURCHASE");
        request.putString(Consts.BILLING_REQUEST_ITEM_ID, mProductId);

        // Note that the developer payload is optional.
        if (mDeveloperPayload != null) {
            request.putString(Consts.BILLING_REQUEST_DEVELOPER_PAYLOAD, mDeveloperPayload);
        }
        Bundle response = service.sendBillingRequest(request);
        PendingIntent pendingIntent = response.getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT);
        if (pendingIntent ==  null) {
            Logger.e(LOG_TAG, "Error with requestPurchase");
            return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
        }

        Intent intent = new Intent();
        ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
        return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }

    @Override
    public void responseCodeReceived(ResponseCode responseCode) {
        ResponseHandler.responseCodeReceived(mService, this, responseCode);
    }
}