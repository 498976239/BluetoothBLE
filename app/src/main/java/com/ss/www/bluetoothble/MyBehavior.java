package com.ss.www.bluetoothble;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SS on 17-7-10.
 */
public class MyBehavior extends CoordinatorLayout.Behavior {
    public MyBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    //关心滚动事件
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return true;
    }
    //发生滚动时我们要做的事情
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        //我们是垂直方向的滚动，我们只要监听dy
        if(dy < 0){//往下拉
            ViewCompat.animate(child).translationX(0).alpha(1).setDuration(1000).start();
            //child.animate().translationX(0).start();


        }else {
            ViewCompat.animate(child).translationX(-1200).alpha(0).setDuration(1000).start();
            //child.animate().translationX(-1200).start();

        }
    }
}
