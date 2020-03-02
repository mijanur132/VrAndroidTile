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
vector<vector <int>> reqTiles;
int mxChunkN=60;

extern "C"
{
JNIEXPORT jint JNICALL Java_com_example_coreAndroid_MainActivity_getTilesNumber2req(JNIEnv *env, jobject instance, jintArray arr, jint pan, jint chunkN) {


    jint *c_array;
    jint i=0;
    c_array = env->GetIntArrayElements( arr, NULL);
    if (c_array == NULL) {
        return -1; /* exception occurred */
    }
    getTilesNumber2reqFov(c_array,pan);
    for (int i=0; i<24; i++)
    {
        if (c_array[i]==1)
        {
            reqTiles[chunkN].push_back(i);
        }
    }

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
   // VideoCapture cap1("/storage/emulated/0/Download/30_diving_1min.avi_1_15.avi.mp4");
    VideoCapture cap1(vpath);
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
            __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "func: size len:%d",frameQvecTiles[tileN][chunkN][j].cols );
            frame.release();
        }
        else
        {
            __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "func: Cant loadVideoFromDevice");
            break;
        }
    }

    __android_log_print(ANDROID_LOG_VERBOSE,"MyApp"," loaded..........................=%d,=%d", chunkN, tileN);
    return 1;
}
}

extern "C"
{
JNIEXPORT void JNICALL Java_com_example_coreAndroid_MainActivity_TileOperationPerFrame(JNIEnv *env, jobject instance, jlong addr, jint fi, jint chunkN, jint cameraPan) {

    Mat* pMatGr=(Mat*)addr;
    *pMatGr=Mat::zeros(512*0.6,960*0.6, CV_8UC3); //margin

  //  __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "..................................................................................looping fi=%d, chunkN=%d, reqTiles=%d>> ", fi, chunkN, reqTiles[chunkN].size());
//
//   if(frameQvecTiles[8][1][1].cols>=10)
//   {
//       *pMatGr=frameQvecTiles[8][1][1];
//       __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "func: size len:%d: %d", frameQvecTiles[8][1][1].cols );
//   }
//   else
//   {
//       __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "func: Cant load in tileoperation","tiles col: %d",frameQvecTiles[8][1][1].cols);
//
//   }
    Tile_operation_per_frame(*pMatGr,frameQvecTiles, reqTiles[chunkN], cameraPan, chunkN, fi); //xxxOpt: pass fi as an input parameter instead of image vec[fi], use pMatGr as output parameter

}
}

extern "C"
{
JNIEXPORT void JNICALL Java_com_example_coreAndroid_MainActivity_initCoREparameters(JNIEnv *env, jobject instance) {
    __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", ".................................................................................intialization... ");
    for (int i = 0; i < 24; i++)
    {
        vector<vector <Mat>> temp1;
        for (int chunk = 0; chunk <= 120; chunk++)
        {
            vector <Mat> temp;
            for (int i = 0; i < 30; i++)
            {
                Mat m;
                temp.push_back(m.clone());
            }
            temp1.push_back(temp);
        }
        frameQvecTiles.push_back(temp1);
    }

    for (int i = 0; i <= mxChunkN; i++)
    {

        vector<int> temp;
        int x = 0;
        temp.push_back(x);
        reqTiles.push_back(temp);
    }

    ERI eri(3840,2048);
    Path path1;
    path1.updateReriCs(0);
    eri.atanvalue();
    eri.xz2LonMap();
    eri.xz2LatMap();
    path1.nonUniformListInit();
   // path1.mapx();
    return;
}
}









