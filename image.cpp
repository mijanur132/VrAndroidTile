#include"image.h"
#include "../../../../../OpenCV-android-sdk/sdk/native/jni/include/opencv2/core.hpp"
#include<fstream>
#include<string.h>
#include <math.h>
#include <android/log.h>
#include "path.h"
#include <C:\opencv\build\include\opencv2\opencv.hpp>
#include <C:\opencv\build\include\opencv2\core\core.hpp>
#include <C:\opencv\build\include\opencv2\highgui\highgui.hpp>


using namespace cv;
using namespace std;


string filename;
int Is_MInv_calculated;
M33 M_Inv;
float corePredictionMargin = 1;

void Tile_operation_per_frame(Mat & frame, vector<vector<vector <cv::Mat>>> & frameQvecTiles, vector <int>& reqTiles, int pan, int tilt, int chunkN, int fi)
{
	Path path1;
	Mat ret1;
	vector<float> tstamps;
	int frameLen = 3840;
	int frameWidth = 2048;
	float margin=0.6;
	float hfov = 90.0f;

	int compressionFactor = 5;
	int w = frameLen * hfov*0.6 / 360;
	int h = frameWidth * hfov*0.6 / 360;
    Mat convPixels( h*corePredictionMargin,w*corePredictionMargin, CV_8UC3, Scalar::all(0));
    ERI eri(frameLen, frameWidth);
	eri.ERI2Conv4tiles(convPixels, frameQvecTiles, reqTiles, chunkN, fi, pan, tilt);
	frame=convPixels.clone(); //xxOpt: convpixels declare once at ERI

  // __android_log_print(ANDROID_LOG_VERBOSE,"MyApp", "func: TilesOperationPerFrame-> :%d: %d:", chunkN, reqTiles.size());
}

void getTilesNumber2reqFov(float fovMul, int tileBitMap[], int pan, int tilt)
{
    int tileColN=6;
    int tileRowN=4;
    int frameCol=3840;
    int frameRow=2048;

    float FoV2Mul=fovMul;
    float hfov=90*FoV2Mul;
    float w = frameCol * hfov / 360;
    float h = frameRow * hfov / 360;
   // int h = frameRow * 90 / 360;
    float corePredictionMargin=1;
    ERI eri(frameCol, frameRow);
    PPC camera(hfov * corePredictionMargin, w * corePredictionMargin, h * corePredictionMargin);
    camera.Pan(pan);
    camera.Tilt(tilt);
    int pixelI, pixelJ = 0;
    for (int v = 0; v < camera.h; v++)
    {
        for (int u = 0; u < camera.w; u++)
        {
            eri.EachPixelConv2ERI(camera, u, v, pixelI, pixelJ);
            int Xtile = floor(pixelJ * tileColN / eri.w); //m*n col and row
            int Ytile = floor(pixelI * tileRowN / eri.h);
            int vectorindex = (Ytile)*tileColN + Xtile;
            tileBitMap[vectorindex] = 1;
        }

    }

}