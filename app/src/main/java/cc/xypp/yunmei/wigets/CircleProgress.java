package cc.xypp.yunmei.wigets;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CircleProgress extends View {

    private static final String TAG = "CircleProgress";

    private Paint _paint;
    private RectF _rectF;
    private Rect _rect;
    private int _max = 100;
    private double targetV = 0,_current = 0;
    //圆弧（也可以说是圆环）的宽度
    private float _arcWidth = 30;
    //控件的宽度
    private float _width;
    private String _tip="";

    public CircleProgress(Context context) {
        this(context, null);
    }

    public CircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        System.out.println("DRAW_RR");
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _rectF = new RectF();
        _rect = new Rect();
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                    double tmp = (targetV - _current) / 8;
                    //System.out.println("test_" + tmp + "|" + _current);
                    if (tmp < 1.8 && tmp > 0) tmp = 1.8;
                    if (tmp > -1.8 && tmp < 0) tmp = -1.8;
                    if (tmp > targetV - _current) tmp = targetV - _current;
                    if (tmp > 0.00001 || tmp < -0.00001) {
                        _current += tmp;
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                    }
                }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    public void SetCurrent(int _current) {
        this.targetV = _current;
    }

    public void SetMax(int _max) {
        this._max = _max;
    }
    public void setTip(String tip) {
        this._tip = tip;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //getMeasuredWidth获取的是view的原始大小，也就是xml中配置或者代码中设置的大小
        //getWidth获取的是view最终显示的大小，这个大小不一定等于原始大小
        _width = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制圆形
        //设置为空心圆，如果不理解绘制弧线是什么意思就把这里的属性改为“填充”，跑一下瞬间就明白了
        _paint.setStyle(Paint.Style.STROKE);
        //设置圆弧的宽度（圆环的宽度）
        _paint.setStrokeWidth(_arcWidth);
        _paint.setColor(Color.GRAY);
        //大圆的半径
        float bigCircleRadius = _width / 2;
        //小圆的半径
        float smallCircleRadius = bigCircleRadius - _arcWidth;
        //绘制小圆
        canvas.drawCircle(bigCircleRadius, bigCircleRadius, smallCircleRadius, _paint);
        _paint.setColor(Color.parseColor("#54a3f7"));
        _rectF.set(_arcWidth, _arcWidth, _width - _arcWidth, _width - _arcWidth);
        //绘制圆弧
        canvas.drawArc(_rectF, 90, (float) (_current * 360 / _max), false, _paint);
        //计算百分比
        //String txt = _current * 100 / _max + "%";
        _paint.setStrokeWidth(0);
        _paint.setTextSize(40);
        _paint.getTextBounds(_tip, 0, _tip.length(), _rect);
        _paint.setColor(Color.parseColor("#54a3f7"));
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //绘制百分比
        canvas.drawText(_tip, bigCircleRadius - _rect.width() / 2, bigCircleRadius + _rect.height() / 2, _paint);
    }
}
