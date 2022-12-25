package com.example.my_buttonclick;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fabReset;
    private TextView tvSecond, tvMillis;
    private TextView tV;
    private Button startUp;
    private Button numDown;
    private boolean isPlaying; // (isPlaying 변수 값) 반전시키기
    private boolean isRunning; // (isRunning 변수 값) 반전시키기
    private long startTime; // 시작 버튼 누른 시점 (혹은 재시작 누른 시점)
    private long elapsedTime; // 최신측정시간 (시작 버튼을 누른 이후 현재까지 경과시간)
    private long totalElapsedTime; // 전체측정시간 (pause 누를 때 elapsedTime 누적)
    private long updateTime; // 화면에 표시할 시간
    private int millis; // 화면에 표시할 밀리초
    private int sec; // 화면에 표시할 초
    private int count = 30; // 초깃값(Number of Enemies) : 30명
    private int endTime = 10; // 타이머 종료 시간 : 10초

    // final Handler 선언 및 생성
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // findViewById(리소스 Id)를 이용하여 바인딩하기
        fabReset = findViewById(R.id.fabReset);
        startUp = findViewById(R.id.buttonUp);
        numDown = findViewById(R.id.buttonDown);
        tvSecond = findViewById(R.id.tvSecond);
        tV = findViewById(R.id.textView);
        tvMillis = findViewById(R.id.tvMillis);

        // Reset 이벤트 처리
        fabReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        // Game Start 이벤트 처리
        startUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    MyThread myThread = new MyThread(); // (startUp 버튼) 스레드 생성
                myThread.start(); // 스레드 시작

                // 스레드 : startUp 버튼 이벤트 처리
                isRunning = !isRunning;
                // 타이머 : startUp 버튼 이벤트 처리
                isPlaying = !isPlaying;
                if (isPlaying) {
                    start();
                } else {
                    pause();
                }
            }
        });

        // Enemy Down 이벤트 처리
        numDown.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (count == 0) { // (카운트가 0일 때)
                    handler.removeCallbacks(timeUpdater); // timeUpdater 핸들러를 중지함.
                    // 토스트 메세지 1 => 사용자 승리!
                    final Toast maintainMessage1 = Toast.makeText(getBaseContext(),
                            "Congratulations. You Win!", Toast.LENGTH_SHORT);
                    maintainMessage1.show();
                    // 토스트 메세지가 Activity 강제 종료 이후에 매우 짧게 나타나므로
                    // 카운트다운타이머를 이용하여 토스트 메세지 시간을 연장함.
                    new CountDownTimer(10000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            maintainMessage1.show();
                        }

                        @Override
                        public void onFinish() {
                            maintainMessage1.show();
                        }
                    }.start();

                    // 스레드를 1초 동안 지연시킨 후
                    try {
                        MyThread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Activity 강제 종료
                    finish();

                } else {
                    tV.setText("" + --count);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void reset() {
        // reset 버튼 클릭 시 : Pause => Restart
        startUp.setText("Restart");
        // reset 버튼 클릭 시 : 초깃값(Number of Enemies)으로 설정
        tV.setText(String.valueOf(count = 30));
        totalElapsedTime = 0L;
        millis = 0;
        sec = 0;
        isPlaying = false;
        isRunning = false;
        handler.removeCallbacks(timeUpdater);
        handler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                tvSecond.setText("0");
                tvMillis.setText(".00");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void start() {
        // start 버튼 클릭 시 : Game Start => Pause
        startUp.setText("Pause");
        // 부팅 이후 경과시간 ms
        startTime = SystemClock.uptimeMillis();
        // UI 업데이트 시작
        handler.post(timeUpdater);
    }

    Runnable timeUpdater = new Runnable() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void run() {
            // 게임 시작 또는 재시작 후 시간이 얼마나 경과했는지 측정
            elapsedTime = SystemClock.uptimeMillis() - startTime;
            // 마지막 누적측정시간에 최신측정시간을 더해서 화면에 표시할 시간을 계산
            updateTime = totalElapsedTime + elapsedTime;
            millis = (int) (updateTime % 1000) / 10;  // 0.01초까지만 표시
            sec = (int) (updateTime / 1000);
            tvSecond.setText(String.valueOf(sec));
            tvMillis.setText(String.format(".%02d", millis));
            // 시간 재측정 및 UI 업데이트 요청. 무한루프개념.
            handler.post(this); // 여전히 다른 스레드도 동시 실행되므로 문제 없음.

            if (sec == endTime) { // (타이머 종료 시간이 ?초일 때)
                handler.removeCallbacks(timeUpdater); // timerUpdater 핸들러를 중지함.
                tvMillis.setText(".00");
                // 토스트 메세지 2 => 적군의 승리!
                final Toast maintainMessage2 = Toast.makeText(getBaseContext(),
                        "Time Over, You Lose", Toast.LENGTH_SHORT);
                maintainMessage2.show();
                // 토스트 메세지가 Activity 강제 종료 이후에 매우 짧게 나타나므로
                // 카운트다운타이머를 이용하여 토스트 메세지 시간을 연장함.
                new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        maintainMessage2.show();
                    }

                    @Override
                    public void onFinish() {
                        maintainMessage2.show();
                    }
                }.start();
                // 타이머는 스레드 지연 없이 바로 Activity 강제 종료
                finish();
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void pause() {
        // pause 버튼 클릭 시 : Pause => Restart
        startUp.setText("Restart");
        // 마지막 누적측정시간 = (전)누적측정시간 + 최신측정시간
        totalElapsedTime += elapsedTime;
        // UI 업데이트 취소
        handler.removeCallbacks(timeUpdater);
    }

    class MyThread extends Thread { // 클래스 MyThread 생성
        @Override
        public void run() {
            if (isRunning && (sec != endTime)) {
                for (int i = 0; i < 70; i++) {
                    if (!isRunning) {
                        break;
                    }
                    try {
                        Thread.sleep(300); // 스레드 0.3초 지연 발생
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Handler 원형 클래스 객체 사용
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tV.setText(String.valueOf(count++));
                        }
                    });
                    // 로그켓을 이용한 스레드 카운트 테스트
                    Log.d("[  Thread Test  ]", "count = " + count);
                }
            }
        }
    }
}
