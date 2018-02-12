package com.zh.dlibtest;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import com.zh.camera.BaseView;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity implements CvCameraViewListener2{

	private Button init,collect,reco,track;
	//private ImageView img;
	Bitmap srcBitmap;
	private Handler mHandler;
	String ret =" ";
	
	/*********************************************************/
	final String TAG = "Rectangle";
	private BaseView mOpenCvCameraView;
	private Mat frameMat ;
	private Mat inMat;
	public float angle = 0f;
	public float orient = 0f;
	MyOrientationDetector orientationDetector = null;
	
	int PICNUM =50;
	int piccnt=1;
	int TrackMode = 0;
	public String rootPath = Environment.getExternalStorageDirectory().getPath()+"/facereco";
	Boolean initflag = false;
	Boolean collectflag = false;
	Boolean recoFlag = false;
	Boolean trackFlag = false;
	Boolean synchronizeFlag = false;
	
	@Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            
        } else {
        	System.loadLibrary("DlibTest");  
        	mOpenCvCameraView.enableView();
            orientationDetector.enable();
            inMat= new Mat();
        }
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	 
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        init = (Button) findViewById(R.id.btinit);
        collect = (Button) findViewById(R.id.btcoll);
		reco = (Button) findViewById(R.id.btreco);		
		track = (Button) findViewById(R.id.bttrack);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.p2);
		srcBitmap = bitmap;
		//img.setImageBitmap(srcBitmap);
		mOpenCvCameraView = (BaseView) findViewById(R.id.CameraView);
	    mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        orientationDetector = new MyOrientationDetector(this);
		
		reco.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (initflag) {
					recoFlag = true;
					recoFace();
				}			
			}
		});
		
		collect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (initflag) {
					collectflag = true;
					collectFace();
				}			
			}
		});
		
		track.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (initflag) {
					trackFlag = true;
				}			
			}
		});
		
		init.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initFace();
			}
		});
    }
    //��ʼ��ģ��
    private void initFace() {
		new Thread(new Runnable() {		
			public void run() {
				
				//rootPath = "/storage/sdcard1/facereco";
				//rootPath = "/storage/emulated/0/facereco";
				//rootPath = rootPath+"/facereco";
				if (!initflag) {
					long current =System.currentTimeMillis();
					int res=DlibTest.initModel(rootPath);
					long performance = System.currentTimeMillis() - current;
					ret = "��ʼ��ģ��,��ʱ:"+ String.valueOf(performance) + "ms."+"����ֵ:"+res;
					mHandler.post(updateui);
					initflag = true;
				}											
			}
		}).start();
	}
    
    //�ռ�����
    private void collectFace() {
		new Thread(new Runnable() {
			public void run() {
				
				//rootPath = "/storage/sdcard1/facereco";
				//rootPath = "/storage/emulated/0/facereco";
				//rootPath = rootPath+"/facereco";
				while (collectflag) {
					if (inMat != null) {
						if (piccnt<11) {
							//Log.e(TAG, "Cols:"+inMat.cols()+"-------------------");
							long current =System.currentTimeMillis();
							
							int collret=0;
							if(synchronizeFlag){
								collret = DlibTest.collect(inMat.getNativeObjAddr(),rootPath,piccnt);
								synchronizeFlag =false;								
							}
							
							long performance = System.currentTimeMillis() - current;
							
							if(collret == 1){
								piccnt++;
								ret = "�ռ���"+String.valueOf(piccnt)+"��,��ʱ:"+ String.valueOf(performance) + "ms.";
								mHandler.post(updateui);
							}
						}else {
							collectflag = false;
						}	
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}															
			}
		}).start();
	}
    
    //ʶ��
    private void recoFace() {
		new Thread(new Runnable() {
			public void run() {			
				
				//rootPath = "/storage/sdcard1/facereco";//���ݴ��Ŀ¼
				//rootPath = "/storage/emulated/0/facereco";
				//rootPath = rootPath+"/facereco";
				while (recoFlag) {
					if (inMat != null) {
						long current =System.currentTimeMillis();
						int recoret = 0;
						if(synchronizeFlag){
							recoret = DlibTest.faceReco(inMat.getNativeObjAddr(),rootPath);
							synchronizeFlag = false;
						}
						long performance = System.currentTimeMillis() - current;
						if (recoret == 1) {
							ret = "ʶ����ȷ,��ʱ:"+ String.valueOf(performance) + "ms.";
							trackFlag = false;
						}else if(recoret<0){
							ret = "û�м�⵽����,��ʱ:"+ String.valueOf(performance) + "ms.";
						}else {
							ret = "ʶ�����,��ʱ:"+ String.valueOf(performance) + "ms.";
							trackFlag = false;
						}				
						mHandler.post(updateui);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}											
			}
		}).start();
	}
      
    //��������
    private Mat trackFace(Mat intput) {
    	long current =System.currentTimeMillis();
    	Mat outMat =new Mat();
    	String trackret = DlibTest.track(intput.getNativeObjAddr(),outMat.getNativeObjAddr(), TrackMode);
    	TrackMode =1;
    	long performance = System.currentTimeMillis() - current;
    	ret = trackret+"���ٺ�ʱ��"+ String.valueOf(performance) + "ms.";
    	Log.e("Ret:", ret+".");
    	mHandler.post(updateui);
    	return outMat;						
	}
     
    //���½��� 
    Runnable updateui = new Runnable() {
		@Override
		public void run() {
			MainActivity.this.setTitle(ret); 
			ret="";
		}
	};
	
	@Override
	public void onPause() {
		orientationDetector.disable();
		Log.d(TAG, "i'm onPause");
		super.onPause();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}
	
	public void onCameraViewStarted(int width, int height) {
		Log.e(TAG, "onCameraViewStarted");
		frameMat = new Mat(height, width, CvType.CV_8UC4);   // Ϊͼ��֡��������ռ䣬8λ4ͨ��ͼ�񣬰���͸��alphaͨ��
	}

	public void onCameraViewStopped() {
		Log.e(TAG, "onCameraViewStopped");
		frameMat.release();   // ����֡����
	}

	//��ȡ����ͷͼƬ֡
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		frameMat = inputFrame.rgba();   // ��ȡ����ͷǰ��ͼ��
	  	        
	    if (orient == 0) {// ����
			org.opencv.core.Core.flip(frameMat.t(), frameMat, 0);
		} else if (orient == 90) {
			org.opencv.core.Core.flip(frameMat, frameMat, -1);
		} else if (orient == 180) {
			org.opencv.core.Core.flip(frameMat.t(), frameMat, 1);
		} else if (orient == 270) {
			
		}
	    	
		if (frameMat != null) {
			if (collectflag || recoFlag) {
				//frameMat.copyTo(inMat);
				if (!synchronizeFlag) {
					//inMat = frameMat;
					frameMat.copyTo(inMat);
					synchronizeFlag =true;
				}		
			}		
			if (trackFlag) {//��������
				Mat out = trackFace(frameMat);
				if (out != null) {
					frameMat = out;
				}
			}			
		}
		
		mOpenCvCameraView.setRotate(angle);
		return frameMat;
	    
	}
			
	class MyOrientationDetector extends OrientationEventListener {
		public MyOrientationDetector(Context context) {
			super(context);
		}
		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
				return; // �ֻ�ƽ��ʱ����ⲻ����Ч�ĽǶ�
			}
			// ֻ����Ƿ����ĸ��Ƕȵĸı�
			if (orientation > 350 || orientation < 10) { // 0��
				orientation = 0;
				angle = 0f;
				orient = 0f;
			} else if (orientation > 80 && orientation < 100) { // 90��
				orientation = 90;
				angle = 270f;
				// angle = 90f;
				orient = 90f;
			} else if (orientation > 170 && orientation < 190) { // 180��
				orientation = 180;
				angle = 180f;
				// angle = 270f;
				orient = 180f;
			} else if (orientation > 260 && orientation < 280) { // 270��
				orientation = 270;
				angle = 90f;
				// angle = 90f;
				orient = 270f;
			} else {
				return;
			}
			Log.d("CameraService", orientation + "," + angle + "," + orient);
		}
	}	

}
