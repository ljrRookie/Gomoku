package com.ljr.gomoku;

import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ljr.gomoku.view.WuziqiPanel;

import java.security.KeyStore;

public class MainActivity extends AppCompatActivity {
    private WuziqiPanel mWuziqi;
    private LinearLayout mRestart;
    private LinearLayout mGoback;
    public static TextView mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initListener() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("游戏结束")
                .setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWuziqi.restartGame();
                    }
                }).setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setCancelable(false);
        mWuziqi.setListener(new WuziqiPanel.OnGameStatusChangeListener() {
            @Override
            public void onGameOver(int gameWinResult) {
                switch (gameWinResult) {
                    case WuziqiPanel.WHITE_WIN:
                        builder.setMessage("白棋胜利！");
                        break;
                    case WuziqiPanel.BLACK_WIN:
                        builder.setMessage("黑棋胜利!");
                        break;
                    case WuziqiPanel.NO_WIN:
                        builder.setMessage("和棋!");
                        break;
                }
                builder.show();
            }
        });
        mRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWuziqi.restartGame();
            }
        });
        mGoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWuziqi.goBack();
            }
        });
    }

    private void initView() {
        mWuziqi = (WuziqiPanel) findViewById(R.id.wuziqi);
        mRestart = (LinearLayout) findViewById(R.id.restart);
        mGoback = (LinearLayout) findViewById(R.id.goback);
        mUser = (TextView) findViewById(R.id.user);


    }


}
