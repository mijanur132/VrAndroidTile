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
Mat convPixels( 512*0.5*corePredictionMargin,960*0.5*corePredictionMargin, CV_8UC3);

void CoRE_operation_per_frame(Mat & frame, int pan, int tilt, int baseAngle, int baseAngleTilt, int fi)
{
	Path path1;
	Mat ret1;
	vector<float> tstamps;
	int frameLen = 3840;
	int frameWidth = 2048;
	float hfov = 90.0f;
    float margin=0.5;

	int compressionFactor = 5;
	int w = frameLen * hfov*margin/ 360;
	int h = frameWidth * hfov*margin / 360;
	PPC camera2(hfov*corePredictionMargin, w*corePredictionMargin, h*corePredictionMargin);

	camera2.Pan(pan);
	camera2.Tilt(tilt);

	PPC refCam(hfov*corePredictionMargin, w*corePredictionMargin, h*corePredictionMargin);
	path1.CRERI2convOptimized(frame,  convPixels, camera2, baseAngle, baseAngleTilt, fi);
	frame=convPixels.clone(); //xxOpt: convpixels declare once at ERI

}

