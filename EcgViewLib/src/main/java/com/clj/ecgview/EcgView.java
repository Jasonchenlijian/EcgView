package com.clj.ecgview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class EcgView extends View {

    private Paint mPaint;
    private List<Integer> mDataList = new ArrayList<>();
    private Matrix mMatrix;

    private int scaleType;
    private int xGridNum;
    private int yGridNum;
    private int minGridNum;
    private boolean littleGrid;
    private int backgroundColor;
    private int gridColor;
    private int littleGridColor;
    private int lineColor;
    private int totalSize;
    private int startColumn;
    private int intervalColumn;

    public EcgView(Context context) {
        this(context, null);

    }

    public EcgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EcgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EcgView, defStyleAttr, 0);
        scaleType = a.getInt(R.styleable.EcgView_scaleType, 0);
        xGridNum = a.getInt(R.styleable.EcgView_xGridNum, 26);
        yGridNum = a.getInt(R.styleable.EcgView_yGridNum, 38);
        minGridNum = a.getInt(R.styleable.EcgView_minGridNum, 5);
        littleGrid = a.getBoolean(R.styleable.EcgView_littleGrid, true);
        backgroundColor = a.getInt(R.styleable.EcgView_backgroundColor, Color.WHITE);
        gridColor = a.getInt(R.styleable.EcgView_gridColor, Color.RED);
        littleGridColor = a.getInt(R.styleable.EcgView_littleGridColor, Color.rgb(255, 180, 180));
        lineColor = a.getInt(R.styleable.EcgView_lineColor, Color.BLACK);
        totalSize = a.getInt(R.styleable.EcgView_totalSize, 4800);
        startColumn = a.getInt(R.styleable.EcgView_startColumn, Color.BLACK);
        intervalColumn = a.getInt(R.styleable.EcgView_intervalColumn, 4800);
        a.recycle();
    }

    private void init() {
        mDataList = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mMatrix = new Matrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (xGridNum == 0 || yGridNum == 0)
            return;

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();

        // 去除padding之后，图片可用的宽高范围
        int imageWidth = getWidth() - paddingLeft - paddingRight;
        int imageHeight = getHeight() - paddingTop - paddingBottom;

        // 锁定宽高比之后，图片最终显示的宽和高
        int actualWidth;
        int actualHeight;

        float ratio = imageWidth / imageHeight;
        float ratio_benchmark = xGridNum / yGridNum;
        if (ratio <= ratio_benchmark) {
            int widthRemain = imageWidth % xGridNum;
            actualWidth = imageWidth - widthRemain;
            actualHeight = imageWidth / xGridNum * yGridNum;
        } else {
            int heightRemain = imageHeight % yGridNum;
            actualHeight = imageHeight - heightRemain;
            actualWidth = imageHeight / yGridNum * xGridNum;
        }

        // 因为像素的比例和整数原因，大部分情况下图形不可能铺满X轴。
        // 所以，将图形平移至X轴中间，使图形的X中心与View的X轴中心重合，Y轴依然顶着最上沿，该方法参考自ImageView的ScaleType，CENTER_INSIDE的变种
        if (scaleType == ScaleType.SCALE_CENTER.nativeInt) {
            float scale = Math.min((float) imageWidth / (float) actualWidth, (float) imageHeight / (float) actualHeight);
            float dx = Math.round((imageWidth - actualWidth * scale) * 0.5f);
            float dy = 0;

            mMatrix.setScale(scale, scale);
            mMatrix.postTranslate(dx, dy);
            // 该方法需要在画图之前调用
            canvas.concat(mMatrix);
        }

        // 每一个大网格和每一个小网格所占的像素个数
        int largeGridPxNum = actualWidth / xGridNum;
        int littleGridPxNum = largeGridPxNum / minGridNum;

        // 画布是白色的
        canvas.drawColor(backgroundColor);

        if (littleGrid) {
            //绘制小网格（浅红色）
            mPaint.setColor(littleGridColor);
            // 每隔littleGridPxNum个像素画一条横线
            for (int i = 0; i < actualHeight; i += littleGridPxNum) {
                canvas.drawLine(paddingLeft + 0, paddingTop + i,
                        paddingLeft + actualWidth, paddingTop + i, mPaint);
            }
            // 每隔littleGridPxNum个像素画一条竖线
            for (int i = 0; i < actualWidth; i += littleGridPxNum) {
                canvas.drawLine(paddingLeft + i, paddingTop + 0,
                        paddingLeft + i, paddingTop + actualHeight, mPaint);
            }
        }

        //绘制大网格（红色）
        mPaint.setColor(gridColor);
        // 每隔mLargeGridPxNum个像素画一条竖线
        for (int i = 0; i <= actualWidth; i += largeGridPxNum) {
            canvas.drawLine(paddingLeft + i, paddingTop + 0,
                    paddingLeft + i, paddingTop + actualHeight, mPaint);
        }
        // 每隔mLargeGridPxNum个像素画一条横线
        for (int i = 0; i <= actualHeight; i += largeGridPxNum) {
            canvas.drawLine(paddingLeft + 0, paddingTop + i,
                    paddingLeft + actualWidth, paddingTop + i, mPaint);
        }

        // 开始绘制数据曲线（黑色）
        if (mDataList == null || mDataList.size() < 1)
            return;

        mPaint.setColor(lineColor);

        // 数据源总长度
        int length = mDataList.size();

        // 第一列的起始数据源位置
        int firstLineStartPosition = 0;
        // 第二列的起始数据源位置
        int secondLineStartPosition = totalSize / 4;
        // 第三列的起始数据源位置
        int thirdLineStartPosition = 2 * totalSize / 4;
        // 第四列的起始数据源位置
        int fourthLineStartPosition = 3 * totalSize / 4;
        // 把一列按照数据源均分
        float unitPointPxNum = actualHeight / (totalSize / 4.0f);

        // 绘制第一列
        for (int i = firstLineStartPosition; i <= secondLineStartPosition && i < length - 1; i++) {
            // 计算第该点和横坐标和纵坐标（从下往上描点）
            float pointy = actualHeight - unitPointPxNum * i + paddingTop;   // 计算纵坐标
            float datamv = mDataList.get(i);                                 // 将测量值转化为十进制，实际的MV数再除以42.5(这属于心电图的一个标准)
            float pointx = startColumn * largeGridPxNum - datamv * littleGridPxNum / 4.25f + paddingLeft;  // 计算横坐标

            //然后计算该点相邻的后面那个点的坐标
            float pointy_next = actualHeight - unitPointPxNum * (i + 1) + paddingTop;
            float datamv_next = mDataList.get(i + 1);
            float pointx_next = startColumn * largeGridPxNum - datamv_next * littleGridPxNum / 4.25f + paddingLeft;

            // 连接这两个点
            canvas.drawLine(pointx, pointy, pointx_next, pointy_next, mPaint);
        }

        // 绘制第二列
        for (int i = secondLineStartPosition; i <= thirdLineStartPosition && i < length - 1; i++) {
            //先计算第一个点和横坐标和纵坐标
            float pointy = actualHeight - unitPointPxNum * (i - secondLineStartPosition) + paddingTop;
            float datamv = mDataList.get(i);
            float pointx = (startColumn + intervalColumn) * largeGridPxNum - datamv * littleGridPxNum / 4.25f + paddingLeft;

            //然后计算第二个点的横坐标和纵坐标
            float pointy_next = actualHeight - unitPointPxNum * (i - secondLineStartPosition + 1) + paddingTop;
            float datamv_next = mDataList.get(i + 1);
            float pointx_next = (startColumn + intervalColumn) * largeGridPxNum - datamv_next * littleGridPxNum / 4.25f + paddingLeft;

            //连接这两个点
            canvas.drawLine(pointx, pointy, pointx_next, pointy_next, mPaint);
        }

        // 绘制第三列
        for (int i = thirdLineStartPosition; i <= fourthLineStartPosition && i < length - 1; i++) {
            //先计算第一个点和横坐标和纵坐标
            float pointy = actualHeight - unitPointPxNum * (i - thirdLineStartPosition) + paddingTop;
            float datamv = mDataList.get(i);
            float pointx = (startColumn + intervalColumn * 2) * largeGridPxNum - datamv * littleGridPxNum / 4.25f + paddingLeft;

            //然后计算第二个点的横坐标和纵坐标
            float pointy_next = actualHeight - unitPointPxNum * (i - thirdLineStartPosition + 1) + paddingTop;
            float datamv_next = mDataList.get(i + 1);
            float pointx_next = (startColumn + intervalColumn * 2) * largeGridPxNum - datamv_next * littleGridPxNum / 4.25f + paddingLeft;

            //连接这两个点
            canvas.drawLine(pointx, pointy, pointx_next, pointy_next, mPaint);
        }

        // 绘制第四列
        for (int i = fourthLineStartPosition; i < length - 1; i++) {
            //先计算第一个点和横坐标和纵坐标
            float pointy = actualHeight - unitPointPxNum * (i - fourthLineStartPosition) + paddingTop;
            float datamv = mDataList.get(i);
            float pointx = (startColumn + intervalColumn * 3) * largeGridPxNum - datamv * littleGridPxNum / 4.25f + paddingLeft;

            //然后计算第二个点的横坐标和纵坐标
            float pointy_next = actualHeight - unitPointPxNum * (i - fourthLineStartPosition + 1) + paddingTop;
            float datamv_next = mDataList.get(i + 1);
            float pointx_next = (startColumn + intervalColumn * 3) * largeGridPxNum - datamv_next * littleGridPxNum / 4.25f + paddingLeft;

            //连接这两个点
            canvas.drawLine(pointx, pointy, pointx_next, pointy_next, mPaint);
        }

    }


    public List<Integer> getDataList() {
        return mDataList;
    }

    public void setDataList(List<Integer> mDataList) {
        this.mDataList = mDataList;
        invalidate();
    }

    public enum ScaleType {

        NORMAL(0),

        SCALE_CENTER(1);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

}
