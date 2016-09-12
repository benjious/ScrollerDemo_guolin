package com.example.administrator.scrollerdemo_guolin;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Administrator on 2016/9/9.
 */
public class ScrollerLayout extends ViewGroup {

    //用于完成滚动操作的实例
    private Scroller mScroller;

    //判定为拖动的最小移动像素数
    private int mTouchSlop;

    //手机按下时的屏幕坐标
    private float mXDown;

    //手机当时所处的屏幕坐标
    private float mXMove;

    //上次触发ACTION_MOVE事件时的屏幕坐标
    private float mXLastMove;


    //界面可滚动的左右边界
    private int leftBorder;
    private int rightBorder;

    public ScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //第一步，创建Scroller的实例
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        //获取TouchSlop值,这个判断值是有系统来和设定的,用于判断用户是否拖动
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    /**
     * 这个方法测量每个子控件尺寸大小
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            //为ScrollerLayout中的每一个子控件在水平方向上进行布局
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 每一子控件在水平方向上进行布局
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                childView.layout(i * childView.getMeasuredWidth(), 0, (i + 1) * childView.getMeasuredWidth(), childView.getMeasuredHeight());
            }
            //初始化左右边界值
            leftBorder = getChildAt(0).getLeft();
            rightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }

    //这个方法是事件分发的判断的方法,判断是否拦截事件给子控件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //获取刚触摸下去的坐标
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                //当手指拖动值大于TouchSlop值时,就认为应该进行滚动,拦截子控件的事件
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    //即是说这个方法是计算距离的,下面的方法是让他平滑效果的
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                /**
                 * 这里三个参数:
                 * scrolledX: 滚动的距离
                 * getScaleX: 前面已经滚动的距离
                 * getWidth:  这是显示街面上的宽度
                 */
                mXMove = event.getRawX();
                int scrolledX = (int) (mXLastMove - mXMove);
                //当用户拖动超出边界,就回到边界的位置
                if (getScrollX() + scrolledX < leftBorder) {
                    //当这个坐标未到达左边界
                    scrollTo(leftBorder, 0);
                    return true;
                } else if (getScrollX() + getWidth() + scrolledX > rightBorder) {
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy(scrolledX, 0);
                mXLastMove = mXMove;
                break;

            case MotionEvent.ACTION_UP:
                //当手纸抬起时,根据当前的滚动值来判定应该滚动到哪个子控件的界面
                int targetIndex=(getScrollX()+getWidth()/2)/getWidth();
                int dx= (targetIndex*getWidth()-getScrollX());
                //第二步,调用startScroll()方法来初始化滚动数据并刷新界面
                mScroller.startScroll(getScrollX(), 0, dx, 0);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        //第三步,重写computeScroll()方法,并在其内部完成平滑滚动的逻辑
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            invalidate();
            Log.d("TAG","------------------");
        }
    }
}
