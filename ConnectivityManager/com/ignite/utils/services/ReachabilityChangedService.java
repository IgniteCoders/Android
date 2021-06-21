package com.ignite.utils.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aeioros.security.SessionManager;
import com.aeioros.simplegob.surveys.appcore.services.SyncAppInBackgroundService;
import com.ignite.utils.Connectivity;
import com.aeioros.utils.Constants;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;

/**
 * Created by mansour on 27/10/17.
 */

public class ReachabilityChangedService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (SessionManager.getSession().isLoggedIn() && Connectivity.isAvailable() && Connectivity.isConnected()) {
            Constants.Console.Log("ReachabilityChangedService", "isConnected");
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
            Job myJob = dispatcher.newJobBuilder()
                    .setService(SyncAppInBackgroundService.class) // the JobService that will be called
                    .setTag("my-unique-tag")        // uniquely identifies the job
                    .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                    .setLifetime(Lifetime.FOREVER)
                    .setReplaceCurrent(true)
                    .build();

            dispatcher.schedule(myJob);
        }
    }
}
