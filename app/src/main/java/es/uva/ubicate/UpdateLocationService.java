package es.uva.ubicate;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateLocationService extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_LOCATION = "es.uva.ubicate.action.UPDATE_LOCATION";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "es.uva.ubicate.extra.PARAM1";

    private static final String TAG = "UpdateLocationService";

    public UpdateLocationService() {
        super("UpdateLocationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d( TAG, "onCreated" );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d( TAG, "onDestroyed" );
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateLocationService.class);
        intent.setAction(ACTION_UPDATE_LOCATION);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d( TAG, "onHandleIntent..." );
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_LOCATION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionUpdateLocation(param1);
            }else  {
                Log.e( TAG, "Action not yet implemented!" );
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateLocation(String param1) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }
}