package com.example.bartosz.whereismyfriend.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.example.bartosz.whereismyfriend.UserManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Bartosz on 15.09.2017.
 */

public class UpdateUserLocationService extends Service {

    private Context _context;
    private UserManager _userManager;
    public static final int notify = 5000;  //interval between two services(Here Service run every 5 seconds)
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    @Override
    public void onCreate() {
        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _userManager = new UserManager();
                    _context = getApplicationContext();
                    _userManager.UpdateCurrentUserLocation(_context);
                }
            });
        }
    }
}