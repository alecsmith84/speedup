package com.example.speedo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.autofill.TextValueSanitizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

   LocationService myService;
   static boolean status;
   LocationManager locationManager;
   static TextView dist, time, speed;
   Button start, pause, stop;
   static long startTime, endTime;
   ImageView image;
   static ProgressDialog locate;
   static int p = 0;

   private ServiceConnection sc = new ServiceConnection() {
       @Override
       public void onServiceConnected(ComponentName name, IBinder service) {
           LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
           myService = binder.getService();
           status = true;
       }

       @Override
       public void onServiceDisconnected(ComponentName name) {
           status = false;
       }

   };

   void bindService() {
       if (status == true)
           return;
       Intent i = new Intent(getApplicationContext(), LocationService.class);
       bindService(i,sc,BIND_AUTO_CREATE);
       status = true;
       startTime = System.currentTimeMillis();
   }

   void unbindService() {
       if (status == false)
           return;
       Intent i = new Intent(getApplicationContext(), LocationService.class);
       unbindService(sc);
       status = false;
   }

   @Override
    protected void onResume() {
       super.onResume();
   }

   @Override
    protected void onStart() {
       super.onStart();
   }

   @Override
   protected void onDestroy() {
       super.onDestroy();
       if(status == true)
           unbindService();
   }


   @Override
    public void onBackPressed() {
       if (status == false)
           super.onBackPressed();
       else
           moveTaskToBack(true);
   }

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

       //dist = (TextView) findViewById(R.id.distancetxt);
       time = (TextView) findViewById(R.id.timetxt);
       speed = (TextView) findViewById(R.id.speedtxt);

       start = (Button) findViewById(R.id.start);
       pause = (Button) findViewById(R.id.pause);
       stop = (Button) findViewById(R.id.stop);

       image = (ImageView) findViewById(R.id.image);

       start.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               checkGps();
               locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

               if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                   return;
               }

               if (status == false)
                   bindService();
               locate = new ProgressDialog(MainActivity.this);
               locate.setIndeterminate(true);
               locate.setCancelable(false);
               locate.setMessage("Getting Location...");
               locate.show();
               start.setVisibility(View.GONE);
               pause.setVisibility(View.VISIBLE);
               pause.setText("Pause");
               stop.setVisibility(View.VISIBLE);
           }
       });

       pause.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (pause.getText().toString().equalsIgnoreCase("pause")) {
                   pause.setText("Resume");
                   p = 1;
               }else if (pause.getText().toString().equalsIgnoreCase("Resume")) {
                   checkGps();
                   locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                   if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                       return;
                   }
                   pause.setText("Pause");
                   p = 0;
               }

           }
       });

       stop.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(status == true)
                   unbindService();
               start.setVisibility(View.VISIBLE);
               pause.setText("Pause");
               pause.setVisibility(View.GONE);
               stop.setVisibility(View.GONE);
               p=0;
           }
       });
   }

   void checkGps() {
       locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

       if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
           GPSAlert();
       }
   }

   private void GPSAlert() {
       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
       alertDialogBuilder.setMessage("Enable GPS to use application")
               .setCancelable(false)
               .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       Intent callGPSSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                       startActivity(callGPSSettings);
                   }
               });
       alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
               dialog.cancel();
           }
       });

       AlertDialog alert = alertDialogBuilder.create();
       alert.show();
   }
   
}
