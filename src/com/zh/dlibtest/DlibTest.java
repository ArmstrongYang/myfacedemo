package com.zh.dlibtest;

public class DlibTest {

	//��ʼ��ģ��
	native static int initModel(String path);
	
	//�����ռ�
	native static int collect(long input,String path,int num);
	
	//����ʶ��	
	native static int faceReco(long input,String path);
	
	//��������	
	native static String track(long input,long output,int mode);

}
