package com.ljr.gomoku.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



import com.ljr.gomoku.R;

import java.util.ArrayList;
import java.util.List;

import static com.ljr.gomoku.MainActivity.mUser;

/**
 * Created by LinJiaRong on 2017/6/9.
 * TODO：
 */

public class WuziqiPanel extends View {

    private Bitmap mWhitePiece;     //白棋的图片
    private Bitmap mBlackPiece;     //黑棋的图片
    private int MAX_LINE;//棋盘行列数
    private int MAX_COUNT_IN_LINE;    //多少颗棋子相邻时赢棋

    private Paint mPaint = new Paint();
    private int mPanelLineColor;//棋盘线的颜色
    private int mPanelWidth;//棋盘宽度
    private float mLineHeight;//棋盘单行间距
    //棋子占行距的比例
    private final float RATIO_PIECE_OF_LINE_HEIGHT = 3 * 1.0f / 4;

    //是否将要下白棋
    public boolean mIsWhite = true;
    //已下的白棋的列表
    private ArrayList<Point> mWhitePieceArray = new ArrayList<>();
    //已下的黑棋的列表
    private ArrayList<Point> mBlackPieceArray = new ArrayList<>();
    private final int INIT_WIN = -1;            //游戏开始时的状态
    public static final int WHITE_WIN = 0;      //白棋赢
    public static final int BLACK_WIN = 1;      //黑棋赢
    public static final int NO_WIN = 2;         //和棋
    private int mGameWinResult = INIT_WIN;      //初始化游戏结果

    //游戏是否结束
    private boolean mIsGameOver;
    //游戏结束监听
    private OnGameStatusChangeListener listener;

    public void setListener(OnGameStatusChangeListener listener) {
        this.listener = listener;
    }

    public WuziqiPanel(Context context) {
        this(context, null);
    }

    public WuziqiPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WuziqiPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取Xml中自定义的属性值并对相应的属性赋值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WuziqiPanel);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attrName = typedArray.getIndex(i);
            switch (attrName) {
                //棋盘背景
                case R.styleable.WuziqiPanel_panel_background:
                    BitmapDrawable panelBackgroundBitmap = (BitmapDrawable) typedArray.getDrawable(attrName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        setBackground(panelBackgroundBitmap);
                    }
                    break;
                //棋盘线的颜色
                case R.styleable.WuziqiPanel_panel_line_color:
                    mPanelLineColor = typedArray.getColor(attrName, 0x88000000);
                    break;
                //白棋图片
                case R.styleable.WuziqiPanel_white_img:
                    BitmapDrawable whitePieceBitmap = (BitmapDrawable) typedArray.getDrawable(attrName);
                    mWhitePiece = whitePieceBitmap.getBitmap();
                    break;
                //黑棋颜色
                case R.styleable.WuziqiPanel_black_img:
                    BitmapDrawable blackPieceBitmap = (BitmapDrawable) typedArray.getDrawable(attrName);
                    mBlackPiece = blackPieceBitmap.getBitmap();
                    break;
                //棋盘线的总数
                case R.styleable.WuziqiPanel_max_count_line:
                    MAX_LINE = typedArray.getInteger(attrName, 10);
                    break;
                //棋盘线的颜色
                case R.styleable.WuziqiPanel_max_min_count_piece:
                    MAX_COUNT_IN_LINE = typedArray.getInteger(attrName, 5);
                    break;
            }
        }
        init();
    }

    //初始化
    private void init() {
        mPaint.setColor(mPanelLineColor);
        //抗锯齿
        mPaint.setAntiAlias(true);
        //防抖动
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        if (mWhitePiece == null) {
            mWhitePiece = BitmapFactory.decodeResource(getResources(), R.mipmap.stone_w2);
        }
        if (mBlackPiece == null) {
            mBlackPiece = BitmapFactory.decodeResource(getResources(), R.mipmap.stone_b1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heigthMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heigthMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;
        int pieceWidth = (int) (mLineHeight * RATIO_PIECE_OF_LINE_HEIGHT);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制棋盘
        drawBoard(canvas);
        //绘制棋子
        drawPiece(canvas);
        //检查游戏是否结束
        checkGameOver();
    }
//重新开始游戏
    public void restartGame(){
        mWhitePieceArray.clear();
        mBlackPieceArray.clear();
        mIsGameOver = false;
        mGameWinResult = INIT_WIN;
        mUser.setText("白棋先下");
        invalidate();
    }
    //悔棋
    public void goBack(){
        if(mIsWhite){
            mUser.setText("黑棋");
            mUser.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            mBlackPieceArray.remove(mBlackPieceArray.size()-1);
        }else{
            mUser.setText("白棋");
            mUser.setTextColor(getResources().getColor(R.color.white));
            mWhitePieceArray.remove(mWhitePieceArray.size()-1);
        }
        mIsWhite = !mIsWhite;
        invalidate();
    }
    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhitePieceArray);
        boolean blackWin = checkFiveInLine(mBlackPieceArray);
        boolean noWin = checkNotWin(whiteWin, blackWin);
//获取游戏结果
        if (whiteWin) {
            mGameWinResult = WHITE_WIN;
        } else if (blackWin) {
            mGameWinResult = BLACK_WIN;
        } else if (noWin) {
            mGameWinResult = NO_WIN;
        }
        if (whiteWin || blackWin || noWin) {
            mIsGameOver = true;
            //回调游戏状态接口
            if (listener != null) {
                listener.onGameOver(mGameWinResult);
            }
        }
    }

    //判断是否和棋
    private boolean checkNotWin(boolean whiteWin, boolean blackWin) {
        if (whiteWin || blackWin) {
            return false;
        }
        //如果总格子数=白棋数+黑棋数，则平局
        int maxPieces = MAX_LINE * MAX_LINE;
        if (mWhitePieceArray.size() + mBlackPieceArray.size() == maxPieces) {
            return true;
        }
        return false;
    }

    //检查是否五子连珠
    private boolean checkFiveInLine(List<Point> points) {
        for (Point point : points) {
            int x = point.x;
            int y = point.y;
            boolean checkHorizontal = checkHorizontalFiveInLine(x, y, points);
            boolean checkVertical = checkVerticalFiveInLine(x, y, points);
            boolean checkLeftDiagonal = checkLeftDiagonalFiveInLine(x, y, points);
            boolean checkRightDiagonal = checkRightDiagonalFiveInLine(x, y, points);
            if (checkHorizontal || checkVertical || checkLeftDiagonal || checkRightDiagonal) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRightDiagonalFiveInLine(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }

        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkLeftDiagonalFiveInLine(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkVerticalFiveInLine(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private boolean checkHorizontalFiveInLine(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true;
        }
        return false;
    }

    private void drawPiece(Canvas canvas) {
        for (int i = 0, n = mWhitePieceArray.size(); i < n; i++) {
            Point whitePoint = mWhitePieceArray.get(i);
            canvas.drawBitmap(mWhitePiece, (whitePoint.x + (1 - RATIO_PIECE_OF_LINE_HEIGHT) / 2) * mLineHeight
                    , (whitePoint.y + (1 - RATIO_PIECE_OF_LINE_HEIGHT) / 2) * mLineHeight, null);
        }
        for (int i = 0, n = mBlackPieceArray.size(); i < n; i++) {
            Point blackPoint = mBlackPieceArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x + (1 - RATIO_PIECE_OF_LINE_HEIGHT) / 2) * mLineHeight
                    , (blackPoint.y + (1 - RATIO_PIECE_OF_LINE_HEIGHT) / 2) * mLineHeight, null);
        }
    }

    private void drawBoard(Canvas canvas) {
        int panelWidth = mPanelWidth;
        float lineHeight = mLineHeight;
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (lineHeight / 2);
            int endX = (int) (panelWidth - lineHeight / 2);

            int Y = (int) ((0.5 + i) * lineHeight);
            canvas.drawLine(startX, Y, endX, Y, mPaint);//画横线
            canvas.drawLine(Y, startX, Y, endX, mPaint);//画竖线
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getValidPoint(x, y);
            //判断是否已存在集合中
            if (mWhitePieceArray.contains(p) || mBlackPieceArray.contains(p)) {
                return false;
            }
            if (mIsWhite) {
                mWhitePieceArray.add(p);
                mUser.setText("黑棋");
                mUser.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

            } else {
                mBlackPieceArray.add(p);
                mUser.setText("白棋");
                mUser.setTextColor(getResources().getColor(R.color.white));
            }
            invalidate();
            mIsWhite = !mIsWhite;
            return true;
        }
        return true;
    }

    //根据触摸点获取最近的格子位置
    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    public interface OnGameStatusChangeListener {
        void onGameOver(int gameWinResult);
    }
    /**
     * 当View销毁时需要保存和恢复的数据
     */
    //保存数据
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instance",onSaveInstanceState());
        bundle.putBoolean("game_over",mIsGameOver);
        bundle.putParcelableArrayList("white_array",mWhitePieceArray);
        bundle.putParcelableArrayList("black_array",mBlackPieceArray);
        return bundle;
    }
    //读取数据

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean("game_over");
            mWhitePieceArray = bundle.getParcelableArrayList("white_array");
            mBlackPieceArray = bundle.getParcelableArrayList("black_array");
            super.onRestoreInstanceState(bundle.getParcelable("instance"));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}

