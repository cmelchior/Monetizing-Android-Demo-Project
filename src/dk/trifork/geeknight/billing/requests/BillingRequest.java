package dk.trifork.geeknight.billing.requests;

import android.os.Bundle;
import android.os.RemoteException;

import com.adwhirl.Logger;
import com.android.vending.billing.IMarketBillingService;

import dk.trifork.geeknight.billing.BillingService;
import dk.trifork.geeknight.billing.Consts;
import dk.trifork.geeknight.billing.Consts.ResponseCode;

/**
 * The base class for all requests that use the MarketBillingService.
 * Each derived class overrides the run() method to call the appropriate
 * service interface.  If we are already connected to the MarketBillingService,
 * then we call the run() method directly. Otherwise, we bind
 * to the service and save the request on a queue to be run later when
 * the service is connected.
 */
public abstract class BillingRequest {
	
	protected static final String LOG_TAG = "BillingRequest";
	
    private final int mStartId;
    protected long mRequestId;
    protected BillingService mService;
    
    public BillingRequest(int startId) {
        mStartId = startId;
    }

    public int getStartId() {
        return mStartId;
    }

    /**
     * Run the request, starting the connection if necessary.
     * @return true if the request was executed or queued; false if there
     * was an error starting the connection
     */
    public boolean runRequest(BillingService billingService, IMarketBillingService service) {
    	if (billingService == null) return false;
    	
    	mService = billingService;
    
    	if (runIfConnected(service)) {
            return true;
        }

    	try {
	        if (mService.bindToMarketBillingService()) {
	            // Add a pending request to run when the service is connected.
	            mService.addRequest(this);
	            return true;
	        }
    	} catch(NullPointerException e) {
    		// Some weird error when switching language while on the upgrade screen.
    		return false;
    	}
        
        return false;
    }

    /**
     * Try running the request directly if the service is already connected.
     * @return true if the request ran successfully; false if the service
     * is not connected or there was an error when trying to use it
     */
    public boolean runIfConnected(IMarketBillingService marketBillingService) {
    	Logger.d(LOG_TAG, getClass().getSimpleName());
    	if (mService != null && marketBillingService != null) {
            try {
                mRequestId = run(marketBillingService);
                Logger.d(LOG_TAG, "request id: " + mRequestId);
                if (mRequestId >= 0) {
                    mService.putSentRequest(mRequestId, this);
                }
                return true;
            } catch (RemoteException e) {
                onRemoteException(e);
            }
        }
        return false;
    }

    /**
     * Called when a remote exception occurs while trying to execute the
     * {@link #run()} method.  The derived class can override this to
     * execute exception-handling code.
     * @param e the exception
     */
    protected void onRemoteException(RemoteException e) {
        Logger.w(LOG_TAG, "remote billing service crashed");
        mService = null;
    }
    
    /**
     * The derived class must implement this method.
     * @throws RemoteException
     */
    abstract protected long run(IMarketBillingService service) throws RemoteException;

    /**
     * This is called when Android Market sends a response code for this
     * request.
     * @param responseCode the response code
     */
    public void responseCodeReceived(ResponseCode responseCode) {
    	// Override if needed
    }

    protected Bundle makeRequestBundle(String method) {
        Bundle request = new Bundle();
        request.putString(Consts.BILLING_REQUEST_METHOD, method);
        request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
        request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, mService.getPackageName());
        return request;
    }
    
    protected void logResponseCode(String method, Bundle response) {
        ResponseCode responseCode = ResponseCode.valueOf(response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE));
        Logger.e(LOG_TAG, method + " received " + responseCode.toString());
    }
}
