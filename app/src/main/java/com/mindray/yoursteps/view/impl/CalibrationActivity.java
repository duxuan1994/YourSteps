package com.mindray.yoursteps.view.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.service.StepCount2;
import com.mindray.yoursteps.utils.CountDownTimer;
import com.mindray.yoursteps.utils.VibrateUtil;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

//    float[] orignValues = new float[3];

    //检测波峰或者波谷,true为当前只检测波峰，false为当前只检测波谷
    boolean isPeakOrValley = true;

    //检测5组波峰与波谷的差，保存波谷检测时间与波峰检测时间的差，以及波峰值减波谷值的差
    final int diffNum = 5;

    static double tValue = 0.2 * 9.8;

    double[][] diffValue = new double[2][diffNum];

    double[] result = new double[2];

    //此次波峰值
    double valueOfPeak = 0;

    //此次波谷值
    double valueOfValley = 0;

    //CSVM的均值

    static int points1 = 0;

    static int points2 = 0;

    static int[] pointsOfPeak = new int[2];     //存波峰位置

    //状态判断，初始为静止状态
//    public static int  stationvalue = 0;

//    public void setStation(int stationvalue) {
//        this.stationvalue = stationvalue;
//    }

//    public static int getStationvalue() {
//        return StepCount2.stationvalue;
////    }

    // float fStand;
    // float dStand;

    //判断连着上升2次，并且下降2次则为峰值所在，数组长度暂时设置为4，后面可调整为3或者5
    final int judgeNum = 4;
    double[] isPeakOfWave = new double[judgeNum];
//
//    //检测到5个波峰波谷值就进行状态检测
//    int isStation = 0;
//
//    //进行静止判断的时间
//    long isStandTime = 0;

    // 0-慢走；1-快走；2-慢跑；3-快跑
    private static int calibrationTag = 0;
    private StringBuilder sb0 = new StringBuilder();
    private StringBuilder sb1 = new StringBuilder();
    private StringBuilder sb2 = new StringBuilder();
    private StringBuilder sb3 = new StringBuilder();

    private int i1 = 1;
    private int i2 = 1;
    private int i3 = 1;
    private int i4 = 1;
    private double[] store1 = new double[2000];
    private double[] store2 = new double[2000];
    private double[] store3 = new double[2000];
    private double[] store4 = new double[2000];
    private List listMean1 = new ArrayList();
    private List listMean2 = new ArrayList();
    private List listMean3 = new ArrayList();
    private List listMean4 = new ArrayList();
    private List listVar1 = new ArrayList();
    private List listVar2 = new ArrayList();
    private List listVar3 = new ArrayList();
    private List listVar4 = new ArrayList();
    private List listSCVM1 = new ArrayList();
    private List listSCVM2 = new ArrayList();
    private List listSCVM3 = new ArrayList();
    private List listSCVM4 = new ArrayList();
    private List listPoints1 = new ArrayList();
    private List listPoints2 = new ArrayList();
    private List listPoints3 = new ArrayList();
    private List listPoints4 = new ArrayList();

    public static String[] set = new String[80];

    private int steps = 0;
    private double diffV = 0;

    private TextView txtCalibration;
    private TextView txtCountDownTime;

    private TimeCount calibrationTime = new TimeCount(241000, 60000);
    private OneMinuteCount minuteCount = new OneMinuteCount(60000, 1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        initParam();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        calibrationTime.start();
    }

    private void initParam() {
        txtCalibration = (TextView) findViewById(R.id.textView_calibration);
        txtCountDownTime = (TextView) findViewById(R.id.textView_calibrationCount);

        txtCalibration.setText(getResources().getString(R.string.calibration_preparation));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        calibrationTag = 0;
        calibrationTime.cancel();
        this.finish();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        switch (calibrationTag) {
            case 1:
                if (i1 > 300 && i1 < 2301) {
                    store1[i1 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
                }
                i1++;
                sb0.append(x + " " + y + " " + z + " ");
                break;
            case 2:
                if (i2 > 300 && i2 < 2301) {
                    store2[i2 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
                }
                i2++;
                sb1.append(x + " " + y + " " + z + " ");
                break;
            case 3:
                if (i3 > 300 && i3 < 2301) {
                    store3[i3 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
                }
                i3++;
                sb2.append(x + " " + y + " " + z + " ");
                break;
            case 4:
                if (i4 > 300 && i4 < 2301) {
                    store4[i4 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
                }
                i4++;
                sb3.append(x + " " + y + " " + z + " ");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {

        String str0 = sb0.toString();
        save(str0, "walkSlowly");

        String str1 = sb1.toString();
        save(str1, "walkQuickly");

        String str2 = sb2.toString();
        save(str2, "runSlowly");

        String str3 = sb3.toString();
        save(str3, "runQuickly");

        super.onDestroy();


    }

    public void setValue(String[] set) {
        for (int i = 0; i < 20; i++) {
            set[i] = listMean1.get(i) + " " + listVar1.get(i) + " " + listSCVM1.get(i) + " " + listPoints1.get(i) + "　" + 1;
        }
        for (int i = 20; i < 40; i++) {
            set[i] = listMean2.get(i - 20) + " " + listVar2.get(i - 20) + " " + listSCVM2.get(i - 20) + " " + listPoints2.get(i - 20) + "　" + 2;
        }
        for (int i = 40; i < 60; i++) {
            set[i] = listMean3.get(i - 40) + " " + listVar3.get(i - 40) + " " + listSCVM3.get(i - 40) + " " + listPoints3.get(i - 40) + "　" + 3;
        }
        for (int i = 60; i < 80; i++) {
            set[i] = listMean4.get(i - 60) + " " + listVar4.get(i - 60) + " " + listSCVM4.get(i - 60) + " " + listPoints4.get(i - 60) + "　" + 4;

        }
    }

    public void save(String input, String fileName) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 拆分数组：将2000拆分为20*100
    private double[][] separateArray(double[] doubles) {
        double[][] d2 = new double[20][100];
        for (int j = 0; j < 20; j++) {
            for (int k = 0; k < 100; k++) {
                d2[j][k] = doubles[k + j * 100];
            }
        }
        return d2;
    }

    // 计算均值的函数
    private double calculateMean(double[] doubles) {
        double sum = 0;
        for (double d : doubles) {
            sum += d;
        }
        return sum / doubles.length;
    }

    // 计算方差的函数
    private double calculateVariance(double[] doubles) {
        double sum = 0;
        double mean = calculateMean(doubles);
        for (double d : doubles) {
            sum += Math.pow(d - mean, 2);
        }
        return sum / doubles.length;
    }

    private double[] calculateCSVMAndPoints(double[] doubles) {
        diffV = 0;
        points1 = 0;
        points2 = 0;
        steps = 0;


        for (int i = 0; i < doubles.length; i++) {
            DetectorNewStep(doubles[i], i);
        }
        if (steps > 0) {
            result[0] = diffV / steps;
        }

        if (steps > 1) {
            result[1] = (points2 - points1) / (steps - 1); //平均两步之间的点数！
        }

        return result;
    }

    public void DetectorNewStep(double values, int k) {

        if (DetectorPeak(values, isPeakOfWave, k)) {
            steps++;    //检测到1步
            if (steps == 1) {
                points1 = k;
            } else {
                points2 = k;
            }
        }

        for (int i = 0; i < judgeNum - 1; i++) {
            isPeakOfWave[i] = isPeakOfWave[i + 1];
        }
        isPeakOfWave[judgeNum - 1] = values;   //判断波峰波谷的矩阵更新
    }

    public boolean DetectorPeak(double newValue, double[] oldValue, int p1) {
        //lastStatus = isDirectionUp;
        if (isPeakOrValley && newValue > oldValue[judgeNum - 1] && oldValue[judgeNum - 1] >= oldValue[judgeNum - 2]
                && oldValue[judgeNum - 2] <= oldValue[judgeNum - 3] && oldValue[judgeNum - 3] < oldValue[judgeNum - 4]) {
            //timeOfValley = System.currentTimeMillis();
            isPeakOrValley = !isPeakOrValley; //检测到波谷，下一步检测波峰！
            valueOfValley = oldValue[2];
//            pointsOfValley = p1;
//            System.out.println("This is test_3");
        }

        if (!isPeakOrValley && oldValue[judgeNum - 2] > 11.7 && newValue < oldValue[judgeNum - 1] && oldValue[judgeNum - 1] <= oldValue[judgeNum - 2]
                && oldValue[judgeNum - 2] >= oldValue[judgeNum - 3] && oldValue[judgeNum - 3] > oldValue[judgeNum - 4]) {
            if (pointsOfPeak[0] == 0) {
                pointsOfPeak[0] = p1;
            } else {
                pointsOfPeak[1] = p1;
            }
            //isPeakOrValley = !isPeakOrValley;  //检测波峰
            valueOfPeak = oldValue[2];  // 数组中2为峰值
            System.out.println("This is test_4");
//            if(station<3){
//                tValue = 0.1 * 9.8;
//            }
//            else{
//                tValue = 0.2 * 9.8;
//            }                                  //动态阈值
            //判断是否是干扰
            if (valueOfPeak - valueOfValley > (tValue)     //这里可以进行更改判断的阈值0.15手持可以，但是对于放在兜里有点大？
                    && pointsOfPeak[1] - pointsOfPeak[0] > 10 && pointsOfPeak[1] - pointsOfPeak[0] < 88) {
//                System.out.println("_STEP "+(timeOfPeak - timeOfValley));
                isPeakOrValley = !isPeakOrValley; //有效的一步，下一个状态检测波谷
                //确认为一个有效的步态
                for (int i = 0; i < diffNum - 1; i++) {
                    diffValue[1][i] = diffValue[1][i + 1];
                    diffValue[0][i] = diffValue[0][i + 1];
                    //System.out.println("test_11"+" "+ diffValue[0][i]);
                }
                System.out.println("This is test_9");
                diffValue[1][diffNum - 1] = valueOfPeak - valueOfValley;

                diffV += valueOfPeak - valueOfValley;

                diffValue[0][diffNum - 1] = pointsOfPeak[1] - pointsOfPeak[0];  //判断状态用到的5组的时间和幅度差值
                //是否进行状态判断
//                isStation += 1;    //不进行状态判断！！ 可以标注掉
                //5次进行一次状态检测
                System.out.println("This is test_8");
//                if (isStation % 5 == 0 && CURRENT_STEPS >= 5) {
//                    station(diffValue);
//
//                    System.out.println("This is test_10");
//                }

                pointsOfPeak[0] = pointsOfPeak[1]; //留出pointsOfPeak[1]接收新的值
                System.out.print("This is test_7");
//                timeOfPeak = System.currentTimeMillis();
                return true;  //返回新的一步

            } else {
                isPeakOrValley = !isPeakOrValley;// 判断是否是干扰，如果是干扰，满足else，放弃之前的波谷值。
                System.out.println("This is test_5");
            }
        }

        System.out.println("This is test_2" + " " + (oldValue[0] - oldValue[1]) + isPeakOrValley);

        return false;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class TimeCount extends CountDownTimer {
        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            switch (calibrationTag) {
                case 0:
                    walkSlowly();
                    minuteCount.start();
                    break;
                case 1:
                    walkQuickly();
                    VibrateUtil.Vibrate(CalibrationActivity.this, 500);
                    minuteCount.start();
                    break;
                case 2:
                    runSlowly();
                    VibrateUtil.Vibrate(CalibrationActivity.this, 500);
                    minuteCount.start();
                    break;
                case 3:
                    runQuickly();
                    VibrateUtil.Vibrate(CalibrationActivity.this, 500);
                    minuteCount.start();
                    break;
                default:
                    break;

            }
        }

        @Override
        public void onFinish() {

            VibrateUtil.Vibrate(CalibrationActivity.this, 500);

            double[][] store11 = separateArray(store1);
            double[][] store22 = separateArray(store2);
            double[][] store33 = separateArray(store3);
            double[][] store44 = separateArray(store4);

            for (int i = 0; i < 20; i++) {

                double[] temp1 = new double[100];
                double[] temp2 = new double[100];
                double[] temp3 = new double[100];
                double[] temp4 = new double[100];

                for (int j = 0; j < 100; j++) {
                    temp1[j] = store11[i][j];
                    temp2[j] = store22[i][j];
                    temp3[j] = store33[i][j];
                    temp4[j] = store44[i][j];
                }

                listMean1.add(calculateMean(temp1));
                listMean2.add(calculateMean(temp2));
                listMean3.add(calculateMean(temp3));
                listMean4.add(calculateMean(temp4));

                listVar1.add(calculateVariance(temp1));
                listVar2.add(calculateVariance(temp2));
                listVar3.add(calculateVariance(temp3));
                listVar4.add(calculateVariance(temp4));

                listSCVM1.add(calculateCSVMAndPoints(temp1)[0]);
                listSCVM2.add(calculateCSVMAndPoints(temp2)[0]);
                listSCVM3.add(calculateCSVMAndPoints(temp3)[0]);
                listSCVM4.add(calculateCSVMAndPoints(temp4)[0]);

                listPoints1.add(calculateCSVMAndPoints(temp1)[1]);
                listPoints2.add(calculateCSVMAndPoints(temp2)[1]);
                listPoints3.add(calculateCSVMAndPoints(temp3)[1]);
                listPoints4.add(calculateCSVMAndPoints(temp4)[1]);

            }
            setValue(set);

            System.out.println("_STEP" + set);

            Toast.makeText(CalibrationActivity.this,
                    getResources().getString(R.string.calibration_finished), Toast.LENGTH_LONG).show();
            calibrationTag = 0;
            calibrationTime.cancel();
            CalibrationActivity.this.finish();
        }
    }

    private class OneMinuteCount extends CountDownTimer {
        OneMinuteCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            txtCountDownTime.setText(String.valueOf(millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            minuteCount.cancel();
        }
    }

    private void walkSlowly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_walkSlowly));
        calibrationTag++;
    }

    private void walkQuickly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_walkQuickly));
        Toast.makeText(CalibrationActivity.this,
                getResources().getString(R.string.calibration_interval1), Toast.LENGTH_SHORT).show();
        calibrationTag++;
    }

    private void runSlowly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_runSlowly));
        Toast.makeText(CalibrationActivity.this,
                getResources().getString(R.string.calibration_interval2), Toast.LENGTH_SHORT).show();
        calibrationTag++;
    }

    private void runQuickly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_runQuickly));
        Toast.makeText(CalibrationActivity.this,
                getResources().getString(R.string.calibration_interval3), Toast.LENGTH_SHORT).show();
        calibrationTag++;
    }
}
