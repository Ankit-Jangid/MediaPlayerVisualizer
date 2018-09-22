package com.thinkcrazy.mediaplayervisualizer;

import android.media.audiofx.Visualizer;
import android.util.Log;

public class AudioCapture {
    private byte [] mRawVizData;
    private int [] mFormattedVizData;
    private byte [] mRawNullData = new byte[0];
    private int [] mFormattedNullData = new int[0];
    private Visualizer mVisualizer;
    private int mType;
    private static long MAX_IDLE_TIME_MS = 2000;
    private long mLastValidCaptureTimeMs;
    public static final int TYPE_PCM = 0;
    public static final int TYPE_FFT = 1;
    public AudioCapture(int type, int size) {
        mType = type;
        int[] range = new int[2];
        range = Visualizer.getCaptureSizeRange();
        if (size < range[0]) {
            size = range[0];
        }
        if (size > range[1]) {
            size = range[1];
        }
        mRawVizData = new byte[size];
        mFormattedVizData = new int[size];
        mVisualizer = null;
        try {
            mVisualizer = new Visualizer(0);
            if (mVisualizer != null) {
                if (mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(false);
                }
                mVisualizer.setCaptureSize(mRawVizData.length);
            }
        } catch (UnsupportedOperationException e) {
            Log.e("AudioCapture", "Visualizer cstor UnsupportedOperationException");
        } catch (IllegalStateException e) {
            Log.e("AudioCapture", "Visualizer cstor IllegalStateException");
        } catch (RuntimeException e) {
            Log.e("AudioCapture", "Visualizer cstor RuntimeException");
        }
    }
    public void start() {
        if (mVisualizer != null) {
            try {
                if (!mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(true);
                    mLastValidCaptureTimeMs = System.currentTimeMillis();
                }
            } catch (IllegalStateException e) {
                Log.e("AudioCapture", "start() IllegalStateException");
            }
        }
    }
    public void stop() {
        if (mVisualizer != null) {
            try {
                if (mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(false);
                }
            } catch (IllegalStateException e) {
                Log.e("AudioCapture", "stop() IllegalStateException");
            }
        }
    }
    public void release() {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }
    public byte[] getRawData() {
        if (captureData()) {
            return mRawVizData;
        } else {
            return mRawNullData;
        }
    }
    public int[] getFormattedData(int num, int den) {
        if (captureData()) {
            if (mType == TYPE_PCM) {
                for (int i = 0; i < mFormattedVizData.length; i++) {
                    // convert from unsigned 8 bit to signed 16 bit
                    int tmp = ((int)mRawVizData[i] & 0xFF) - 128;
                    // apply scaling factor
                    mFormattedVizData[i] = (tmp * num) / den;
                }
            } else {
                for (int i = 0; i < mFormattedVizData.length; i++) {
                    // apply scaling factor
                    mFormattedVizData[i] = ((int)mRawVizData[i] * num) / den;
                }
            }
            return mFormattedVizData;
        } else {
            return mFormattedNullData;
        }
    }
    private boolean captureData() {
        int status = Visualizer.ERROR;
        boolean result = true;
        try {
            if (mVisualizer != null) {
                if (mType == TYPE_PCM) {
                    status = mVisualizer.getWaveForm(mRawVizData);
                } else {
                    status = mVisualizer.getFft(mRawVizData);
                }
            }
        } catch (IllegalStateException e) {
            Log.e("AudioCapture", "captureData() IllegalStateException: "+this);
        } finally {
            if (status != Visualizer.SUCCESS) {
                Log.e("AudioCapture", "captureData() :  "+this+" error: "+ status);
                result = false;
            } else {
                // return idle state indication if silence lasts more than MAX_IDLE_TIME_MS
                byte nullValue = 0;
                int i;
                if (mType == TYPE_PCM) {
                    nullValue = (byte)0x80;
                }
                for (i = 0; i < mRawVizData.length; i++) {
                    if (mRawVizData[i] != nullValue) break;
                }
                if (i == mRawVizData.length) {
                    if ((System.currentTimeMillis() - mLastValidCaptureTimeMs) > MAX_IDLE_TIME_MS) {
                        result = false;
                    }
                } else {
                    mLastValidCaptureTimeMs = System.currentTimeMillis();
                }
            }
        }
        return result;
    }

}
