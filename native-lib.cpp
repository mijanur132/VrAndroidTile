#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>
#include <ostream>
#include <string_view>
#include <locale>
#include <android/log.h>
#include"ERI.h"
#include "image.h"
#include "path.h"
#include "../../../../../../../../opencv/build/include/opencv2/videoio.hpp"
#include <vector>

using namespace std;
using namespace cv;

typedef unsigned char byte;
vector<vector <Mat>> loadedFrameVec;
int framloaded;
vector<vector<vector <Mat>>>frameQvecTiles;


extern "C"
{
JNIEXPORT jint JNICALL Java_com_example_coreAndroid_MainActivity_getTilesNumber2req(JNIEnv *env, jobject instance, jintArray arr, jint pan) {


    jint *c_array;
    jint i=0;
    c_array = env->GetIntArrayElements( arr, NULL);
    if (c_array == NULL) {
        return -1; /* exception occurred */
    }
    getTilesNumber2reqFov(c_array,pan);

    //do work inside the array;

    env->ReleaseIntArrayElements(arr, c_array, 0);

    return 0;

}
}

extern "C"
{
JNIEXPORT jint JNICALL Java_com_example_coreAndroid_MainActivity_loadVideoFromDevice(JNIEnv *env,
                                                                             jobject instance,
                                                                             jstring videoPath,
                                                                             jint chunkN, jint tileN) {
    __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "func: loadVideoFromDevice");
    jboolean iscopy;
    const char *vpath = (env)->GetStringUTFChars(videoPath, &iscopy);
    __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", ":%s", vpath);
    VideoCapture cap1("/storage/emulated/0/Download/30_diving_1min.avi_1_15.avi.mp4");
   // VideoCapture cap1(vpath);
    if (!cap1.isOpened()) {
        return 0;
    }



    for (int j = 0; j < 30; j++)
    {
        Mat frame;
        cap1 >> frame;

        if (!frame.empty())
        {
            frameQvecTiles[tileN][chunkN][j] = frame.clone();
        }
        else
        {
            cout << "Cannot open, the video file has no frame: " << j << endl;
            break;
        }
    }
//    for (int fi = 0; fi < 30; fi++)
//    {
//        Mat frame;
//        cap1 >> frame;
//        __android_log_print(ANDROID_LOG_VERBOSE,"MyApp","%d, =%d", fi,chunkN);
//        if (frame.empty()) {
//            __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "emptyframe.................");
//            frame = Mat::zeros(100 + fi, 400 + fi, CV_8UC3);//dummy frame
//        } else {
//        }
//        int chunkno = (int) chunkN;
//        //__android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "fi=%d, CN=%d>> ", fi,chunkno);
//        if (fi==0)
//       // {loadedFrameVec[chunkno][tileN][0]=frame;}
//        else{loadedFrameVec[chunkno][tileN].push_back(frame);}
//
//        frame.release();
//    }
    __android_log_print(ANDROID_LOG_VERBOSE,"MyApp"," loaded..........................=%d,=%d", chunkN, tileN);
    return 1;
}
}

extern "C"
{
JNIEXPORT void JNICALL Java_com_example_coreAndroid_MainActivity_CoREoperationPerFrame(JNIEnv *env, jobject instance, jlong addr, jint fi, jint chunkN, jint cameraPan, jint baseAngle) {

    Mat* pMatGr=(Mat*)addr;

    while(fi>=loadedFrameVec[chunkN].size() && loadedFrameVec[chunkN].size()<80)
   {
     //  __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "looping fi=%d, size=%d, chunkN=%d>> ", fi,loadedFrameVec[chunkN].size(), chunkN);
   }
    __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "..................................................................................looping fi=%d, size=%d, chunkN=%d, pan=%d>> ", fi,loadedFrameVec[chunkN].size(), chunkN, cameraPan);
    *pMatGr=loadedFrameVec[chunkN][fi].clone();
    CoRE_operation_per_frame(*pMatGr, cameraPan, baseAngle); //xxxOpt: pass fi as an input parameter instead of image vec[fi], use pMatGr as output parameter
    Mat m;
    loadedFrameVec[chunkN][fi]=m;
    return;
}
}

extern "C"
{
JNIEXPORT void JNICALL Java_com_example_coreAndroid_MainActivity_initCoREparameters(JNIEnv *env, jobject instance) {

    for (int i = 0; i < 24; i++)
    {
        vector<vector <Mat>> temp1;
        for (int chunk = 0; chunk <= 120; chunk++)
        {
            vector <Mat> temp;
            for (int i = 0; i < 30; i++)
            {
                Mat m;
                temp.push_back(m);
                m.release();
            }
            temp1.push_back(temp);
        }
        frameQvecTiles.push_back(temp1);
    }
    ERI eri(3840,2048);
    Path path1;
    path1.updateReriCs(0);
    eri.atanvalue();
    eri.xz2LonMap();
    eri.xz2LatMap();
    path1.nonUniformListInit();
    path1.mapx();
    return;
}
}









