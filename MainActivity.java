package com.example.coreAndroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.os.Environment.getExternalStorageState;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.StrictMath.abs;


public class MainActivity extends AppCompatActivity {
    int chunk2display=1;
    volatile int yes2DL=0;
    volatile int yes2PL=0;
    volatile int totalPlChunk=0;
    long   chunk2loadFile=1L;
    volatile int pan=0;
    volatile int tilt=0;
    volatile int lastChunkReqPan=0;
    volatile int lastChunkReqTilt=0;
    volatile int totalPan=0;
    volatile int totalTilt=0;
    volatile long startTotal=0;
    volatile  int totalReqTiles=-1;
    volatile int totalDlTiles=0;
    volatile float downX=0;
    volatile float downY=0;

    private static final String TAG = "OCVSample::Activity";
    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load ndk built module, as specified in moduleName in build.gradle               // after opencv initialization
                    System.loadLibrary("native-lib");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

    }




    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, _baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }


    @Override
    public void onPause() {
        super.onPause();

    }

    private class MyTask extends AsyncTask<Long, Void, Void> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected Void doInBackground(Long... param) {
            Long addr= param[0];
            long x=param[1];
            long p=param[2];
            int chunk2load=(int)x;
            while(true)
            {
                if(yes2DL==1)
                {
                    System.out.println("doInBackground: dl ordered for chunk................................................................"+chunk2load);
                    downloadFileHttp(chunk2load, totalPan, totalTilt );
                    System.out.println("doInBackground: dl finished for chunk:..................................................................."+chunk2load);
                    yes2DL=0;
                    yes2PL=1;
                    chunk2load=chunk2load+1;
                }
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCoREparameters();
                    System.out.println("CoRE param updated..........................................>>>>");
                    startTotal = System.currentTimeMillis();
                    yes2DL=1;
                    dlThread();

                    playThread();



                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    public void dlThread()
    {

                Mat m = new Mat();
                long cameraPan = totalPan;
                long cameraTilt = totalTilt;
                new MyTask().execute(m.getNativeObjAddr(), chunk2loadFile, cameraPan, cameraTilt);//calling load video from device using videocapture in background

    }

    public void playThread()
    {

        System.out.println("function: playThread..");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            Long start = System.currentTimeMillis();
            int ia=0;
            Mat m1=new Mat();
            Long FirstStart = System.currentTimeMillis();
            int dlChunkPan1xx=0;
            int dlChunkTilt1xx=0;
            int timeCond=20;
            public void run()
                    {
                        int lastPan=0;
                       // System.out.println("dl req:..................................................................."+totalDlTiles+ totalReqTiles);
//                        while(totalDlTiles!=totalReqTiles)
//                        {
//                            //System.out.println("dl req:..................................................................."+totalDlTiles+ totalReqTiles);
//                        }
                        ImageView iv = (ImageView) findViewById(R.id.imageView);
                       // iv.invalidate();
                        Bitmap bm;
                        iv.setOnTouchListener(new View.OnTouchListener(){
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                int add=5;
                                boolean mIsSwiping = false;
                                switch(event.getActionMasked()) {
                                    case MotionEvent.ACTION_DOWN: {
                                       downX = event.getX();
                                       downY=event.getY();
                                       break;
                                    }
                                    case MotionEvent.ACTION_UP:
                                        float deltaX = event.getX() - downX;
                                        float deltaY = event.getY() - downY;
                                        Toast.makeText(MainActivity.this, "touch.................."+deltaX+" "+deltaY, Toast.LENGTH_SHORT).show();
                                        if (abs(deltaX) > 10)
                                        {
                                            int addX=(int)deltaX/40;
                                            totalPan=(totalPan+addX);
                                        }

                                        if (abs(deltaY) > 10)
                                        {
                                            int addY=(int)deltaY/100;
                                            totalTilt=(totalTilt+addY);
                                            if (totalTilt>60)
                                            {totalTilt=60;}
                                            if (totalTilt<-60)
                                            {totalTilt=-60;}
                                        }
                                        else {
                                            }
                                            return true;
                                        }

                                return true;
                            }
                        });
                        Long current = System.currentTimeMillis();
                        long playTime=current-start;
                        long i=0;
                        if (chunk2display==59)
                        {
                            Long endTotal = System.currentTimeMillis();
                            Long totalPlay=endTotal-startTotal;
                            System.out.println("Total chunk: dl req and total Time:..................................................................."+totalDlTiles+";" +totalReqTiles+";" +totalPlay);
                            System.exit(0);
                        }
                        if (chunk2display==1 && ia==0)
                        {
                            timeCond=5000;
                        }
                        else{timeCond=2;}

                        while(playTime<timeCond)
                        {   current = System.currentTimeMillis();
                            playTime=current-start;

                        }
                        start=current;
                        long frameTime=current-FirstStart;

                        TileOperationPerFrame(m1.getNativeObjAddr(), ia, chunk2display, totalPan, totalTilt); // ia increaseas and one after another frame comses out
                        bm = Bitmap.createBitmap(m1.cols(), m1.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(m1, bm);
                        iv.setImageURI(null);
                        iv.setImageBitmap(bm);
                        handler.postDelayed(this, 1);
                        if (ia==29)
                        {
                            chunk2display=chunk2display+1;
                            totalPlChunk=totalPlChunk+1;
                            while(yes2PL==0)
                            {
                            }
                            System.out.println("ended..............................................................................................................."+(chunk2display-1));
                            ia=-1;
                        }
                        if (ia==5)
                        {
                            yes2DL=1;
                            yes2PL=0;
                        }
                        ia++;
                    }
                }, 2);

    }


    public void downloadFileHttp(int chunkN, int pan, int tilt)
    {
        String fPath="";
        try
        {
            int [] tilesArr;
            tilesArr=new int[24];
            if (tilt>60)
            {
                tilt=60;
            }
            if (tilt<-60)
            {
                tilt=-60;
            }
            getTilesNumber2req(tilesArr, pan, tilt, chunkN);
           // String sourceBaseAddr="http://10.0.2.2:80/3vid2crf3trace/Tiles/mobisys/30_diving_1min.avi";
            String sourceBaseAddr="http://192.168.43.179:80/3vid2crf3trace/Tiles/mobisys/30_roller_1min.avi";

            for (int i=0; i<24; i++)
            {
                if (tilesArr[i]==1)
                {       totalReqTiles=totalReqTiles+1;

                       // String sourceBaseAddr="http://192.168.43.179:80/3vid2crf3trace/android/tilt0/";
                        String name=sourceBaseAddr+"_"+chunkN+"_"+i+".avi"+".mp4";
                        URL url = new URL(name);
                        System.out.println("requested file name................................>>>"+ name);
                        URLConnection ucon = url.openConnection();
                        ucon.setReadTimeout(50000);
                        ucon.setConnectTimeout(100000);
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 500);
                        File file = new File("/storage/emulated/0/Download/30_roller_1min.avi_" + chunkN+"_"+i+".avi.mp4");
                        fPath=file.getPath();

                        //fPath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+ "30_diving_1min.avi_1_15.avi";
                        //fPath="/storage/emulated/0/Download/diving_1_10_40.mp4";

                        if (!file.exists())
                        {

                            file.createNewFile();
                            FileOutputStream outStream = new FileOutputStream(file);
                            byte[] buff = new byte[500 * 1024];

                            int len;
                            while ((len = inStream.read(buff)) != -1) {
                                outStream.write(buff, 0, len);
                            }

                            outStream.flush();
                            outStream.close();
                            inStream.close();
                        }
                        System.out.println("dl finished.total DL:..................................................................."+fPath+ i);
                        loadVideoFromDevice(fPath, chunkN, i);
                    totalDlTiles=totalDlTiles+1;


                }
            }

            totalDlTiles=totalDlTiles+1;

        }




        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Cant save file.......>>>"+fPath);
            System.exit(1);
        }


    }

    public native int  getTilesNumber2req(int tilesArr[], int pan, int tilt, int chunkN);
    public native void initCoREparameters();
    public native int loadVideoFromDevice(String videoPath, int chunkN, int tileN);
    public native void TileOperationPerFrame(long addr, int fi, int chunkN, int cameraPan, int cameraTilt);
}
