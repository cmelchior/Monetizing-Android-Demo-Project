package dk.trifork.geeknight.billing.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IMarketBillingService;

import dk.trifork.geeknight.billing.Consts;
import dk.trifork.geeknight.billing.ResponseHandler;
import dk.trifork.geeknight.billing.Security;
import dk.trifork.geeknight.billing.Consts.ResponseCode;

/**
 * Wrapper class that sends a RESTORE_TRANSACTIONS message to the server.
 */
public class RestoreTransactions extends BillingRequest {
    long mNonce;

    public RestoreTransactions() {
        // This object is never created as a side effect of starting this
        // service so we pass -1 as the startId to indicate that we should
        // not stop this service after executing this request.
        super(-1);
    }

    @Override
    protected long run(IMarketBillingService service) throws RemoteException {
        mNonce = Security.generateNonce();

        Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
        request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
        Bundle response = service.sendBillingRequest(request);
        logResponseCode("restoreTransactions", response);
        return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
    }

    @Override
    protected void onRemoteException(RemoteException e) {
        super.onRemoteException(e);
        Security.removeNonce(mNonce);
    }

    @Override
    public void responseCodeReceived(ResponseCode responseCode) {
        ResponseHandler.responseCodeReceived(mService, this, responseCode);
    }
}
