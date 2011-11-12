package dk.trifork.geeknight.billing.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IMarketBillingService;

import dk.trifork.geeknight.billing.Consts;

/**
 * Wrapper class that confirms a list of notifications to the server.
 */
public class ConfirmNotifications extends BillingRequest {
    final String[] mNotifyIds;

    public ConfirmNotifications(int startId, String[] notifyIds) {
        super(startId);
        mNotifyIds = notifyIds;
    }

    @Override
    protected long run(IMarketBillingService service) throws RemoteException {
        Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
        request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
        Bundle response = service.sendBillingRequest(request);
        logResponseCode("confirmNotifications", response);
        return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }
}
