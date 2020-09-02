package jp.gr.java_conf.datingapp.utility;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

public class ViewBehavior extends CoordinatorLayout.Behavior<HeaderView> {

    private Context mContext;

    private int mStartMarginLeft;
    private int mEndMargintLeft;
    private int mMarginRight;
    private int mStartMarginBottom;
    private boolean isHide;

    public ViewBehavior(Context context, AttributeSet attrs) {
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, HeaderView child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, HeaderView child, View dependency) {

//        int maxScroll = ((AppBarLayout) dependency).getTotalScrollRange();
//        float percentage = Math.abs(dependency.getY()) / (float) maxScroll;
//
//        float childPosition = dependency.getHeight()
//                + dependency.getY()
//                - child.getHeight()
//                - (getToolbarHeight() - child.getHeight()) * percentage / 2;
//
//
//        childPosition = childPosition - mStartMarginBottom * (1f - percentage);
//
//        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
//        lp.leftMargin = (int) (percentage * mEndMargintLeft) + mStartMarginLeft;
//        lp.rightMargin = mMarginRight;
//        child.setLayoutParams(lp);
//
//        child.setY(childPosition);
//
//
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            if (isHide && percentage < 1) {
//                child.setVisibility(View.VISIBLE);
//                isHide = false;
//            } else if (!isHide && percentage == 1) {
//                child.setVisibility(View.GONE);
//                isHide = true;
//            }
//        }
        return true;
    }


    public int getToolbarHeight() {
        int result = 0;
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
        }
        return result;
    }

}