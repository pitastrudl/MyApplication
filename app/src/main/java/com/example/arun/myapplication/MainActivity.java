package com.example.arun.myapplication;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import com.example.arun.myapplication.KillNotificationService.KillNotificationService;


//menu za zbirat aplikacije
//namesto imen imas ikone od aplikacij za gumbe
//kaj se zgodi ce ni tega apkja instaleranga, check na zacetku

public class MainActivity extends AppCompatActivity {
    static String TAG = "Mainactivity";
    static int numtest = 0;
    static String currapp = "";
    static String nextapp = "";
    static String torque = "org.prowl.torquefree";
    static String kodi = "org.xbmc.kodi";
    static boolean notifCreated = false;
    static String arunapp = "com.example.arun.myapplication";

    final static int REQUEST_ENABLE_BT = 1;

    TextView textInfo;


    // checkbt
    private void checkBT() {
        //Log.i(TAG,"checking bluetooth status, checkBT");
        final TextView s = (TextView) findViewById(R.id.status_text);
        final BluetoothAdapter nBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!nBluetoothAdapter.isEnabled()) {
            s.setText("Please enable bluetooth");
            s.setBackgroundColor(Color.RED);
        } else {
            s.setText("Bluetooth enabled");
            s.setBackgroundColor(Color.GREEN);
        }
    }

    //timer
    int counter = 0;

    private final static int INTERVAL = 1000; //1 sec
    Handler mHandler = new Handler();
    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mHandlerTask, INTERVAL);

            checkBT();
            String openApp = getApp();

            if (!openApp.equals(arunapp) && notifCreated) {

                Log.v("RUNNER app not opened",openApp + " " + arunapp);
                if (counter > 3){
                    removeNotif();
                    Log.v("RUNNER","Notification removed within runner");
                    counter = 0;
                }else{
                    Log.v("RUNNER counter inc ", Integer.toString(counter));
                    counter++;
                }

            }

            if (notifCreated) {
                checkForNotif();
                Log.v("RUNNER checked notif ", Integer.toString(counter));
            }


        }
    };


    public String getApp() {
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        //Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        String currapps = componentInfo.getPackageName();
        //Toast.makeText(MainActivity.this, currapp, Toast.LENGTH_LONG).show();
        Log.v("Current app open", currapps);
        return currapps;
    }


    public void checkForNotif() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        if (notifications.length == 0) {
            Log.v("NOTIFICAITON", "CREATED");
            makeNotif();
        }
        for (StatusBarNotification notification : notifications) {
            Log.v("something", notification.toString());
            if (notification.getId() != 1 && notification.getPackageName() != arunapp) {


            }
        }

    }


    public void removeNotif(){
        notifCreated = false;
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

    }

    public void startRepeatingTask() {
        mHandlerTask.run();
    }

    public void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }


    //result from activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //ask for bluetooth
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "BlueTooth Turned On", Toast.LENGTH_LONG).show();
                checkBT();
            } else {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                checkBT();
            }
        }
        /*

        if (bluetoothAdapter.isEnabled()) {
            s.setText("enabled bluetooth");
            s.setBackgroundColor(Color.GREEN);
        }else{
            s.setText("disabled bluetooth");
            s.setBackgroundColor(Color.RED);
        }
        */
    }




    public void makeNotif() {
        //notif test
        String packagename = nextapp;
        if (nextapp == torque) {
            nextapp = kodi;
        } else {
            nextapp = torque;
        }


        //prepare notification
        //dodal za v7  tisto pred new

        // se zbrise notificaton
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();


        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder)
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notif)
                        .setContentTitle("App switcher")
                        .setContentText("Click me to switch to: " + packagename)
                        .setOngoing(true);

        Intent intent = getPackageManager().getLaunchIntentForPackage(packagename);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }


    //oncreate
    protected void onCreate(Bundle savedInstanceState) {

        startService(new Intent(this, KillNotificationService.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  //nalouda vse na zacetku
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        final TextView t = (TextView) findViewById(R.id.status_text);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);


        //take first package and set bg to the button
        try {
            Drawable d = getPackageManager().getApplicationIcon(torque);
            button.setBackground(d);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(MainActivity.this, "Torque not installed, please install", Toast.LENGTH_LONG).show();
            return;
        }

        //take second package, set bg to button
        try {
            Drawable d = getPackageManager().getApplicationIcon(kodi);
            button2.setBackground(d);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(MainActivity.this, "Kodi not installed, please install from playstore", Toast.LENGTH_LONG).show();
            return;
        }


        //for cancelling test is in the bfirst button action


        //sets according to bluetooth status
        if (!mBluetoothAdapter.isEnabled()) {
            t.setText("disabled bluetooth");
            t.setBackgroundColor(Color.RED);

        } else {
            t.setText("enabled bluetooth");
            t.setBackgroundColor(Color.GREEN);
        }


        //first button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nextapp = kodi;
                notifCreated = true;
                makeNotif();
                //test disable notify
                //NotificationManager notifManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                //notifManager.cancelAll();

                Log.i(TAG, "button clicked");
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Log.i(TAG, "no bluetooth support");
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        t.setText("disabled bluetooth");
                        t.setBackgroundColor(Color.RED);
                        //if not enabled mrequest
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        t.setText("enabled bluetooth");
                        t.setBackgroundColor(Color.GREEN);
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(torque);
                        if (launchIntent != null) {
                            Toast.makeText(MainActivity.this, "Launching app", Toast.LENGTH_LONG).show();
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }

                    }
                }

            }
        });

        //second button click
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextapp = torque;
                makeNotif();
                notifCreated = true;
                Log.i(TAG, "button clicked");
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Log.i(TAG, "no bluetooth support");
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        t.setText("disabled bluetooth");
                        t.setBackgroundColor(Color.RED);
                        //button.setBackgroundColor(Color.RED);
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        t.setText("enabled bluetooth");
                        t.setBackgroundColor(Color.GREEN);
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(kodi);
                        if (launchIntent != null) {
                            Toast.makeText(MainActivity.this, "Launching app", Toast.LENGTH_LONG).show();
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }

                    }
                }

            }
        });

        startRepeatingTask();
    }



}
