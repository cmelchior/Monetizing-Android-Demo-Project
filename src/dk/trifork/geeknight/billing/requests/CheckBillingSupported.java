package dk.trifork.geeknight.billing.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.adwhirl.Logger;
import com.android.vending.billing.IMarketBillingService;

import dk.trifork.geeknight.billing.Consts;
import dk.trifork.geeknight.billing.ResponseHandler;
import dk.trifork.geeknight.billing.Consts.ResponseCode;

/**
 * Wrapper class that checks if in-app billing is supported.
 */
public class CheckBillingSupported extends BillingRequest {

	public CheckBillingSupported() {
        // This object is never created as a side effect of starting this
        // service so we pass -1 as the startId to indicate that we should
        // not stop this service after executing this request.
        super(-1);
    }

    @Override
    protected long run(IMarketBillingService service) throws RemoteException {
        Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
        Bundle response = service.sendBillingRequest(request);
        int responseCode = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
        Logger.i(LOG_TAG, "CheckBillingSupported response code: " + ResponseCode.valueOf(responseCode));
        boolean billingSupported = (responseCode == ResponseCode.RESULT_OK.ordinal());
        ResponseHandler.checkBillingSupportedResponse(billingSupported);
        return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
    }
}
