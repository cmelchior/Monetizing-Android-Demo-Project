package dk.trifork.geeknight.billing.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IMarketBillingService;

import dk.trifork.geeknight.billing.Consts;
import dk.trifork.geeknight.billing.Security;

/**
 * Wrapper class that sends a GET_PURCHASE_INFORMATION message to the server.
 */
public class GetPurchaseInformation extends BillingRequest {
    long mNonce;
    final String[] mNotifyIds;

    public GetPurchaseInformation(int startId, String[] notifyIds) {
        super(startId);
        mNotifyIds = notifyIds;
    }

    @Override
    protected long run(IMarketBillingService service) throws RemoteException {
        mNonce = Security.generateNonce();

        Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
        request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
        request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
        Bundle response = service.sendBillingRequest(request);
        logResponseCode("getPurchaseInformation", response);
        return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }

    @Override
    protected void onRemoteException(RemoteException e) {
        super.onRemoteException(e);
        Security.removeNonce(mNonce);
    }
}
