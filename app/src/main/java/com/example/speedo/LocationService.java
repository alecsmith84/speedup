package com.example.speedo;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final long interval = 100*2;
    private static final long fastestInterval = 100*1;
    LocationRequest myLocation;
    GoogleApiClient myApi;
    Location myCurrent, lStart, lEnd;
    static double distance = 0;
    double speed;

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        myApi = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        myApi.connect();
        return binder;
    }

    protected void createLocationRequest() {
        myLocation = new LocationRequest();
        myLocation.setInterval(interval);
        myLocation.setFastestInterval(fastestInterval);
        myLocation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        return super.onStartCommand(intent,flags,startID);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(myApi,myLocation,this);
        } catch (SecurityException e) {
        }
    }

    protected  void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                myApi, this);
        distance = 0;
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        myCurrent = location;
        if(lStart == null){
            lStart = myCurrent;
            lEnd = myCurrent;
        } else
            lEnd = myCurrent;

        updateUI();
        speed = (location.getSpeed() * 18/5)/1.609 ;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){

    }
    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    private void updateUI() {
        if (MainActivity.p == 0) {
            distance = distance + (lStart.distanceTo(lEnd)/1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
           // MainActivity.time.setText("Total Time: " + diff + " minutes");
            MainActivity.time.setText(getString(R.string.time, diff));
            if (speed > 0.0)
                //MainActivity.speed.setText("Current Speed: " + new DecimalFormat("#.##").format(speed) + " mph");

                MainActivity.speed.setText(new DecimalFormat("#").format(speed));
            else
                MainActivity.speed.setText("0");

            //MainActivity.dist.setText("no");

            lStart = lEnd;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (myApi.isConnected())
            myApi.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}
