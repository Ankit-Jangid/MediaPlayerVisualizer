package com.thinkcrazy.mediaplayervisualizer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Random;

import static com.thinkcrazy.mediaplayervisualizer.Constants.AUDIO_PERMISSION_CONSTANT;
import static com.thinkcrazy.mediaplayervisualizer.Constants.STORAGE_PERMISSION_CONSTANT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = MainActivity.class.getSimpleName();
    private AppCompatActivity mActivity = this;
    private Button mPlayPauseBtn;


    // audio visulizer...
    private boolean playing = false;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String dataSource;
    private int[] colorArray;
    private View[] rowsArray1, rowsArray2, rowsArray3;
    private GradientDrawable[] backgroundsArray1;
    private GradientDrawable gdDefault;
    private Handler handler = new Handler();
    private Runnable animationThread;
    private AudioCapture mAudioCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();
        askRequiredPermissions();
        setupVisualizer();

    }

    private void setUI() {
        mPlayPauseBtn = findViewById(R.id.btn_play_pause);
        mPlayPauseBtn.setOnClickListener(this);
    }

    private void setupVisualizer() {

        colorArray = getResources().getIntArray(R.array.equalizer_color_array);
        LinearLayout layout_container1 = findViewById(R.id.layout_container1);
        LinearLayout layout_container2 = findViewById(R.id.layout_container2);
        LinearLayout layout_container3 = findViewById(R.id.layout_container3);

        rowsArray1 = new View[colorArray.length];
        rowsArray2 = new View[colorArray.length];
        rowsArray3 = new View[colorArray.length];

        gdDefault = new GradientDrawable();
        gdDefault.setColor(Color.GRAY);
        gdDefault.setCornerRadius(10);

        backgroundsArray1 = new GradientDrawable[colorArray.length];

        // drawing empty bars
        for (int i = 0; i < colorArray.length; i++) {
            // for (int i = 0; i <= 10; i++) {

            backgroundsArray1[i] = new GradientDrawable();
            backgroundsArray1[i].setColor(colorArray[i]);
            backgroundsArray1[i].setCornerRadius(15);

            // normally add the items
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            GradientDrawable gd = new GradientDrawable();
            assert inflater != null;
            View view1 = inflater.inflate(R.layout.row_item, null);
            View view2 = inflater.inflate(R.layout.row_item, null);
            View view3 = inflater.inflate(R.layout.row_item, null);
            //gd.setColor(colorArray[i]);
            gd.setColor(Color.GRAY);
            gd.setCornerRadius(15);
            view1.findViewById(R.id.image_).setBackground(gd);
            view2.findViewById(R.id.image_).setBackground(gd);
            view3.findViewById(R.id.image_).setBackground(gd);

            layout_container1.addView(view1);
            layout_container2.addView(view2);
            layout_container3.addView(view3);

            rowsArray1[i] = view1.findViewById(R.id.image_);
            rowsArray2[i] = view2.findViewById(R.id.image_);
            rowsArray3[i] = view3.findViewById(R.id.image_);


            // static data source
//           // File audioFile = new File(Environment.getExternalStorageDirectory() + "/nokia_lumia_800.mp3");
//            dataSource = audioFile.getAbsolutePath();
//            mediaPlayer = new MediaPlayer();


            mediaPlayer = MediaPlayer.create(this, R.raw.stereo_love_edward_maya);
            mediaPlayer.setLooping(false);
            dataSource = getResources().getResourceName(R.raw.stereo_love_edward_maya);


        }


    }


    /**
     * --------------- Check Permissions before proceeding further and run'em -------
     */

    private void askRequiredPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (!checkAudioPermission()) {
                requestAudioPermission();
            }

            if (!checkStoragePermission()) {
                requestStoragePermission();
            }

        }

    }

    private boolean checkAudioPermission() {

        int result0 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result0 == PackageManager.PERMISSION_GRANTED;

    }

    private boolean checkStoragePermission() {

        int result0 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return result0 == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        }, AUDIO_PERMISSION_CONSTANT);

    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, STORAGE_PERMISSION_CONSTANT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case STORAGE_PERMISSION_CONSTANT:

                if (grantResults.length > 0) {
                    boolean STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (STORAGE) {
                        // do stuff here
                        Log.e(TAG, "STORAGE_ permission granted");
//                            {
//              Toast.makeText(mActivity,"STORAGE_ permission granted",Toast.LENGTH_SHORT).show();
//                            }
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage(getString(R.string.storage_permission_needed_msg))
                                .setPositiveButton(getString(R.string.btn_ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                final Intent i = new Intent();
                                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                                i.setData(Uri.parse("package:" + mActivity.getPackageName()));
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                startActivity(i);
                                            }
                                        }).setNegativeButton(getString(R.string.btn_cancel), null).show();
                    }
                }

                break;
            case AUDIO_PERMISSION_CONSTANT:
                if (grantResults.length > 0) {
                    boolean AUDIO = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (AUDIO) {
                        Log.e(TAG, "Audio  permission granted");
                    } else {
                        if (!AUDIO) {
                            new AlertDialog.Builder(this)
                                    .setMessage(getString(R.string.audio_permission_needed))
                                    .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            final Intent i = new Intent();
                                            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            i.addCategory(Intent.CATEGORY_DEFAULT);
                                            i.setData(Uri.parse("package:" + mActivity.getPackageName()));
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                            startActivity(i);
                                        }
                                    }).setNegativeButton(getString(R.string.btn_cancel), null).show();

                        }
                    }
                }

                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            // play/pause or stop the controls..with lifecycle of media player
            case R.id.btn_play_pause:
                 if (playing && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    stopPlaying();
                    playing = false;
                    return;
                }

                 onPlay(true);
                // resumePlaying();
                if (playing) {
                    animationThread = new Runnable() {
                        @Override
                        public void run() {
                            getAudioMetaInfo();
                            // randomStaticData();
                            handler.postDelayed(this, 15); // specify the ms to control speed of bars
                        }

                    };
                    handler.post(animationThread);
                }
                break;
        }

    }


    //------------ mediaPlayer controls----------------------

    private void onPlay(boolean start) {

        if (start) {
            startPlaying(dataSource);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(String dataSource1) {
        playing = true;
        mediaPlayer = MediaPlayer.create(this, R.raw.stereo_love_edward_maya);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        mPlayPauseBtn.setText(getString(R.string.stop));


        // mediaPlayer = new MediaPlayer();
        // Log.e(TAG, "startPlaying->dataSource ::" + dataSource);
//         try {
//            mediaPlayer.reset();
//            mediaPlayer.setDataSource(dataSource);
//            // mediaPlayer.prepareAsync();
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//
//            Log.e(TAG, "mediaPlayer is playing");
//
//        } catch (IOException e) {
//            Log.e(TAG, "mediaPlayer ..failed");
//        }
    }

    private void stopPlaying() {

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mPlayPauseBtn.setText(getString(R.string.play));

        }
        //mediaAnimation(false);
    }

    private void pausePlaying() {

        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mPlayPauseBtn.setText(getString(R.string.play));
        }
        // mediaAnimation(false);
    }

    private void resumePlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            playing = true;
            mPlayPauseBtn.setText(getString(R.string.stop));


        }
        //  mediaAnimation(true);
    }


    private void randomStaticData() {

        Random random = new Random();

        int minValue = random.nextInt(128);

        animate_MAX(minValue, true);
        animate_AVG(minValue, true);
        animate_MIN(minValue, true);

    }


    /**
     * -------- Audio visualization Ops...........
     */

    private void getAudioMetaInfo() {

        /**---- if you make sleep thread in order to optimize then
         * you may have the lags between the samples, resulting in variance of animation in
         *  bars visuals */


        // AudioCapture mAudioCapture = new AudioCapture(AudioCapture.TYPE_PCM, 1024);
        mAudioCapture = new AudioCapture(AudioCapture.TYPE_PCM, 80);
        mAudioCapture.start();
//        try {
////                    Thread.sleep(2 * 1000);
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        int[] mVizData;
        mVizData = mAudioCapture.getFormattedData(1, 1);
        // mAudioCapture.release();
        int minValue = 0;
        int maxValue = 0;
        for (int value : mVizData) {
            if (value < minValue) {
                minValue = value;
            } else if (value > maxValue) {
                maxValue = value;
            }

            animate_AVG(maxValue - minValue, true);
            animate_MIN(minValue, true);
            animate_MAX(maxValue, true);


        }
    }

    private void animate_AVG(int value, boolean status) {
        int color;
         if (status) {
            color = (value / colorArray.length);
            if (color < 15) {

                // filling color
                for (int i = 0; i < color; i++) {
                    rowsArray1[i].setBackground(gdDefault);
                    rowsArray1[i].setBackground(backgroundsArray1[i]);
                }

                // emptying...
                for (int j = color + 1; j < colorArray.length && j < 15; j++) {
                    rowsArray1[j].setBackground(gdDefault);

                }

            }

        }

    }

    private void animate_MIN(int value, boolean status) {
        int color;
         if (status) {

             color = Math.abs(value / colorArray.length); // as

            if (color < 15) {

                // filling color
                for (int i = 0; i < color; i++) {
                    rowsArray2[i].setBackground(gdDefault);
                    rowsArray2[i].setBackground(backgroundsArray1[i]);
                }
                //emptying...
                for (int j = color + 1; j < colorArray.length && j < 15; j++) {
                    rowsArray2[j].setBackground(gdDefault);

                }

            }

        }

    }

    private void animate_MAX(int value, boolean status) {
        int color;
         if (status) {
            color = value / colorArray.length; // as
            if (color < 15) {

                // filling color
                for (int i = 0; i < color; i++) {
                    rowsArray3[i].setBackground(gdDefault);
                    rowsArray3[i].setBackground(backgroundsArray1[i]);
                }
                // emptying...
                for (int j = color + 1; j < colorArray.length && j < 15; j++) {
                    rowsArray3[j].setBackground(gdDefault);

                }

            }

        }
    }


    @Override
    public void onStop() {
        super.onStop();
        stopPlaying();
        releaseResources();
        System.gc();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPlaying();
        releaseResources();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseResources();
        finish();
    }

    // releasing all occupied resources and stopping background threads ...
    private void releaseResources() {

        if (mAudioCapture != null) {
            mAudioCapture.release();
            mAudioCapture = null;
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        handler.removeCallbacks(animationThread);
    }


}
