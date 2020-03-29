package com.example.coreAndroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

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
    volatile int mxChunk=119;
    volatile int select=1;
    volatile int touched=0;
    volatile int maxshift=40;
    volatile int shiftcount=0;
    volatile int addX=0;
    volatile int addY=0;
    volatile  int totalFrame=0;
    volatile int record=0; //0 means trace plaly
    volatile float fovMul=1;
    //volatile String ip="http://192.168.43.179:80";
    volatile String ip="http://10.0.0.4:80";


    volatile Vector<Integer> vectorPan = new Vector<>();
    volatile Vector<Integer> vectorTilt = new Vector<>();

    volatile ArrayList<String> panTilt = new ArrayList<String>();

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
                    downloadFileHttp2QL360(chunk2load, totalPan, totalTilt );
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
                String pathx="/storage/emulated/0/MichiganPan.txt";
                String pathy="/storage/emulated/0/MichiganTilt.txt";

                if(select==3){
                         pathx="/storage/emulated/0/WisconsinPan.txt";
                         pathy="/storage/emulated/0/WisconsinTilt.txt";
                }
                if (select==2)
                {
                    pathx="/storage/emulated/0/MinnesotaPan.txt";
                    pathy="/storage/emulated/0/MinnesotaTilt.txt";
                }

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCoREparameters();

                    File file = new File(pathx);

                    try{
                    Scanner scanner = new Scanner(file);
                    int [] tall = new int [100];
                    int i = 0;
                    while(scanner.hasNextInt())
                    {
                        vectorPan.add(scanner.nextInt());
                    }
                    }  catch(IOException ioe){
                            ioe.printStackTrace();
                             }
                     file = new File(pathy);
                    try{
                        Scanner scanner = new Scanner(file);
                        int [] tall = new int [100];
                        int i = 0;
                        while(scanner.hasNextInt())
                        {
                            vectorTilt.add(scanner.nextInt());
                        }
                    }  catch(IOException ioe){
                        ioe.printStackTrace();
                    }

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

                        ImageView iv = (ImageView) findViewById(R.id.imageView);
                       // iv.invalidate();
                        Bitmap bm;
                        iv.setOnTouchListener(new View.OnTouchListener(){
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                int add=5;
                                touched=1;
                                shiftcount=0;
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
                                       // Toast.makeText(MainActivity.this, "touch.................."+deltaX+" "+deltaY, Toast.LENGTH_SHORT).show();
                                        if (abs(deltaX) > 40)
                                        {
                                            addX=(int)deltaX/(20*maxshift);

                                        }

                                       if (abs(deltaY) > 20)
                                        {
                                            addY=(int)deltaY/(maxshift*20);


                                        }
                                        else {
                                            }
                                            return true;
                                        }

                                return true;
                            }
                        });
                        if(record!=0)
                        {
                           if (touched==1 && shiftcount<10) {
                                totalPan = (totalPan + addX);
                                if (totalPan >= 360) {
                                    totalPan = totalPan - 360;
                                }
                                if (totalPan <= -360) {
                                    totalPan = totalPan + 360;
                                }

                                totalTilt=(totalTilt+addY);
                                if (totalTilt>60)
                                {totalTilt=60;}
                                if (totalTilt<-60)
                                {totalTilt=-60;}

                                shiftcount=shiftcount+1;
                            }
                        }
                        Long current = System.currentTimeMillis();
                        long playTime=current-start;
                        long i=0;
                        if (chunk2display==mxChunk)
                        {
                            Long endTotal = System.currentTimeMillis();
                            Long totalPlay=endTotal-startTotal;
                            System.out.println("Total chunk: dl req and total Time:..................................................................."+totalDlTiles+";" +totalReqTiles+";" +totalPlay);

                           String url = "https://forms.gle/KpmbsmJqYjh5pwe67";   //michigan
                            if (select==2) {
                                url="https://forms.gle/Mkh5SkKHD1nwBHtk8";  //minne
                            }
                            if(select==3) {
                                url = "https://forms.gle/vwFJq7NThP5k4QbN6";  //winsconsin
                            }
                            Intent ix = new Intent(Intent.ACTION_VIEW);
                            ix.setData(Uri.parse(url));
                            startActivity(ix);
                            Iterator it= panTilt.iterator();

                            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                                try {
                                        FileOutputStream fOut=
                                                new FileOutputStream(
                                                        new File(Environment.getExternalStoragePublicDirectory(
                                                                Environment.DIRECTORY_DOWNLOADS), "Michigan.txt"), TRUE
                                                );

                                    if(select==2){
                                        fOut =  new FileOutputStream(
                                                    new File(Environment.getExternalStoragePublicDirectory(
                                                            Environment.DIRECTORY_DOWNLOADS), "Minnesota.txt"),TRUE
                                            );}
                                    if(select==3) {
                                        fOut =  new FileOutputStream(
                                                        new File(Environment.getExternalStoragePublicDirectory(
                                                                Environment.DIRECTORY_DOWNLOADS), "Wisconsin.txt"), TRUE
                                                );
                                    }


                                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                    myOutWriter.append("new user:"+"\n");
                                    while(it.hasNext())
                                        myOutWriter.append(it.next().toString()+"\n");

                                    myOutWriter.close();
                                    fOut.close();
                                    Log.v("MyApp","File has been written");
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                    Log.v("MyApp","File didn't write");
                                }
                            }

                            System.exit(0);
                        }
                        if (chunk2display==1 && ia==0)
                        {
                            timeCond=5000;
                        }
                        else{timeCond=80;}

                        while(totalDlTiles<24*(chunk2display-0)+0)
                        {   current = System.currentTimeMillis();
                            playTime=current-start;
                        }
                        System.out.println("It came here: totalDlTiles............................................................"+totalDlTiles);
                        start=current;
                        long frameTime=current-FirstStart;
                        panTilt.add(totalPan+"_"+totalTilt);
                        if(record==0){
                            totalPan=vectorPan.get(totalFrame);
                            totalTilt=vectorTilt.get(totalFrame);
                            totalFrame=totalFrame+1;
                        }
                      //  System.out.println("current frame pan and tilt..................................................................."+chunk2display+";"+ia+";"+totalPan+";" +totalTilt);
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
            if (chunkN>59)
            {chunkN=chunkN+0;}  //temp
            getTilesNumber2req(tilesArr, fovMul, pan, tilt, chunkN);
            String sourceBaseAddr=ip+"/3vid2crf3trace/android/divingT/30_diving_1min.avi";

            if(select==2) {
                sourceBaseAddr=ip+"/3vid2crf3trace/android/rhinoT/30_rhino_1min.avi";
                mxChunk=89;

            }

            if(select==3) {
                sourceBaseAddr = ip+"/3vid2crf3trace/android/rollerT/30_roller_1min.avi";
            }

            for (int i=0; i<24; i++)
            {
                if (tilesArr[i]==1)
                {       totalReqTiles=totalReqTiles+1;
                        String name=sourceBaseAddr+"_"+chunkN+"_"+i+".avi";
                        URL url = new URL(name);
                        System.out.println("requested file name................................>>>"+ name);
                        URLConnection ucon = url.openConnection();
                        ucon.setReadTimeout(50000);
                        ucon.setConnectTimeout(50000);
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 500);
                        File file = new File("/storage/emulated/0/divingT/30_diving_1min.avi_" + chunkN+"_"+i+".avi");

                        if(select==2) {
                             file = new File("/storage/emulated/0/rhinoT/30_rhino_1min.avi_" + chunkN+"_"+i+".avi");
                        }
                        if(select==3) {
                            file = new File("/storage/emulated/0/rollerT/30_roller_1min.avi_" + chunkN + "_" + i + ".avi");
                        }

                      // fPath=file.getPath();
                        fPath="/storage/emulated/0/divingT/30_diving_1min.avi_" + chunkN + "_" + i + ".avi";
                        if(select==2)
                        {
                            fPath="/storage/emulated/0/rhinoT/30_rhino_1min.avi_" + chunkN + "_" + i + ".avi";
                        }
                        if(select==3)
                        {
                            fPath="/storage/emulated/0/rollerT/30_roller_1min.avi_" + chunkN + "_" + i + ".avi";
                        }
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

                        loadVideoFromDevice(fPath, chunkN, i);
                    System.out.println("Loading finished.total DL:..................................................................."+fPath+ i);
                    totalDlTiles=totalDlTiles+1;
                }

            }

            //totalDlTiles=totalDlTiles+1;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Cant save file.......>>>"+fPath);
            System.exit(1);
        }

    }
    public void downloadFileHttp2QL360(int chunkN, int pan, int tilt)
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

            getTilesNumber2req(tilesArr, fovMul, pan, tilt, chunkN);
            String sourceBaseAddr="";

            for (int i=0; i<24; i++)
            {
                // totalReqTiles=totalReqTiles+1;
                if (tilesArr[i] == 1)
                {
                    totalReqTiles = totalReqTiles + 1;
                    sourceBaseAddr = ip + "/3vid2crf3trace/android/divingT/30_diving_1min.avi";

                    if (select == 2) {
                        sourceBaseAddr = ip + "/3vid2crf3trace/android/rhinoT/30_rhino_1min.avi";
                        mxChunk = 89;
                    }
                    if (select == 3) {
                        sourceBaseAddr = ip + "/3vid2crf3trace/android/rollerT/30_roller_1min.avi";
                    }

                    String name = sourceBaseAddr + "_" + chunkN + "_" + i + ".avi";
                    URL url = new URL(name);
                    System.out.println("requested file name................................>>>" + name);
                    URLConnection ucon = url.openConnection();
                    ucon.setReadTimeout(50000);
                    ucon.setConnectTimeout(50000);
                    InputStream is = ucon.getInputStream();
                    BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 500);
                    File file = new File("/storage/emulated/0/divingT/30_diving_1min.avi_" + chunkN + "_" + i + ".avi");

                    if (select == 2) {
                        file = new File("/storage/emulated/0/rhinoT/30_rhino_1min.avi_" + chunkN + "_" + i + ".avi");
                    }
                    if (select == 3) {
                        file = new File("/storage/emulated/0/rollerT/30_roller_1min.avi_" + chunkN + "_" + i + ".avi");
                    }


                    fPath = "/storage/emulated/0/divingT/30_diving_1min.avi_" + chunkN + "_" + i + ".avi";
                    if (select == 2) {
                        fPath = "/storage/emulated/0/rhinoT/30_rhino_1min.avi_" + chunkN + "_" + i + ".avi";
                    }
                    if (select == 3) {
                        fPath = "/storage/emulated/0/rollerT/30_roller_1min.avi_" + chunkN + "_" + i + ".avi";
                    }
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

                    loadVideoFromDevice(fPath, chunkN, i);
                    System.out.println("Loading finished.total DL:..................................................................." + fPath + i);
                    totalDlTiles=totalDlTiles+1;
                }

            }
            for (int i=0; i<24; i++)
            {
                // totalReqTiles=totalReqTiles+1;
                if (tilesArr[i] == 0)
                {
                    totalReqTiles=totalReqTiles+1;
                    sourceBaseAddr=ip+"/3vid2crf3trace/android/divingT/45_30_diving_1min.avi";
                    if(select==2) {
                        sourceBaseAddr=ip+"/3vid2crf3trace/android/rhinoT/45_30_rhino_1min.avi";
                        mxChunk=89; }
                    if(select==3) {
                        sourceBaseAddr = ip+"/3vid2crf3trace/android/rollerT/45_30_roller_1min.avi";
                    }
                    String name=sourceBaseAddr+"_"+chunkN+"_"+i+".avi.avi";
                    URL url = new URL(name);
                    System.out.println("requested low qual file name................................>>>"+ name);
                    URLConnection ucon = url.openConnection();
                    ucon.setReadTimeout(50000);
                    ucon.setConnectTimeout(50000);
                    InputStream is = ucon.getInputStream();
                    BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 500);
                    File file = new File("/storage/emulated/0/divingT/45_30_diving_1min.avi_" + chunkN+"_"+i+".avi");
                    if(select==2) {
                        file = new File("/storage/emulated/0/rhinoT/45_30_rhino_1min.avi_" + chunkN+"_"+i+".avi");
                    }
                    if(select==3) {
                        file = new File("/storage/emulated/0/rollerT/45_30_roller_1min.avi_" + chunkN + "_" + i + ".avi");
                    }
                    fPath="/storage/emulated/0/divingT/45_30_diving_1min.avi_" + chunkN + "_" + i + ".avi";
                    if(select==2)
                    {
                        fPath="/storage/emulated/0/rhinoT/45_30_rhino_1min.avi_" + chunkN + "_" + i + ".avi";
                    }
                    if(select==3)
                    {
                        fPath="/storage/emulated/0/rollerT/45_30_roller_1min.avi_" + chunkN + "_" + i + ".avi";
                    }

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
                    loadVideoFromDevice(fPath, chunkN, i);
                    System.out.println("Loading finished in low qual.total DL:..................................................................." + fPath + i);
                    totalDlTiles=totalDlTiles+1;

                }
               // totalDlTiles=totalDlTiles+1;
            }

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Cant save file..................>>>"+fPath);
            System.exit(1);
        }

    }

    public native int  getTilesNumber2req(int tilesArr[], float fovMul, int pan, int tilt, int chunkN);
    public native void initCoREparameters();
    public native int loadVideoFromDevice(String videoPath, int chunkN, int tileN);
    public native void TileOperationPerFrame(long addr, int fi, int chunkN, int cameraPan, int cameraTilt);
}
