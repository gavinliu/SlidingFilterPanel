package cn.gavinliu.SlidingFilterPanel;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class SlidingFilterPanel extends LinearLayout {

	private static String TAG = "VideoFilterPanel";
	private static final int MIN_FLING_VELOCITY = 400;

	private ViewDragHelper mDragHelper;
	private View mContent;
	private View mHandle;
	private View mEdge;

	private int mTop;
	private int mDragRange;
	private float mDragOffset;
	private float mInitialMotionX;
	private float mInitialMotionY;
	private boolean mIsHandleViewUnder;

	public SlidingFilterPanel(Context context) {
		super(context);
		init(context);
	}

	public SlidingFilterPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SlidingFilterPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		final float density = context.getResources().getDisplayMetrics().density;
		mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
		mDragHelper.setMinVelocity(MIN_FLING_VELOCITY * density);
	}

	@Override
	protected void onFinishInflate() {
		mContent = findViewById(R.id.content);
		mHandle = mContent.findViewById(R.id.handle);
		mEdge = mContent.findViewById(R.id.edge);
		mEdge.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hide();
			}
		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "onMeasure");
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0), resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout");
		mDragRange = getHeight();
		mContent.layout(0, mTop, r, mTop + mContent.getMeasuredHeight());
	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return mIsHandleViewUnder;
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			mTop = top;
			mDragOffset = (float) Math.abs(top) / mDragRange;

			requestLayout();
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			final int newTop = Math.min(top, 0);
			return newTop;
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			int top = getPaddingTop();
			if (yvel < 0 || (yvel == 0 && mDragOffset > 0.4f)) {
				top = -mDragRange;
			} else {
				top = 0;
			}
			mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
			invalidate();
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return mDragRange;
		}
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	
	public void show() {
		mDragHelper.smoothSlideViewTo(mContent, mContent.getLeft(), 0);
		ViewCompat.postInvalidateOnAnimation(this);
	}

	public void hide() {
		mDragHelper.smoothSlideViewTo(mContent, mContent.getLeft(), -mDragRange);
		ViewCompat.postInvalidateOnAnimation(this);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		if (action != MotionEvent.ACTION_DOWN) {
			mDragHelper.cancel();
			return super.onInterceptTouchEvent(ev);
		}

		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mDragHelper.cancel();
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		boolean interceptTap = false;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mInitialMotionX = x;
			mInitialMotionY = y;
			interceptTap = mDragHelper.isViewUnder(mHandle, (int) x, (int) y);
			break;

		case MotionEvent.ACTION_MOVE:
			final float adx = Math.abs(x - mInitialMotionX);
			final float ady = Math.abs(y - mInitialMotionY);
			final int slop = mDragHelper.getTouchSlop();
			if (ady > slop && adx > ady) {
				mDragHelper.cancel();
				return false;
			}
			break;
		}
		mIsHandleViewUnder = mDragHelper.isViewUnder(mHandle, (int) x, (int) y);

		return mDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDragHelper.processTouchEvent(event);
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		mIsHandleViewUnder = mDragHelper.isViewUnder(mHandle, (int) x, (int) y);

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			mInitialMotionX = x;
			mInitialMotionY = y;
			break;
		}
		case MotionEvent.ACTION_UP: {
			final float dx = x - mInitialMotionX;
			final float dy = y - mInitialMotionY;
			final float slop = mDragHelper.getTouchSlop();

			if (dx * dx + dy * dy < slop * slop && mIsHandleViewUnder) {
				if (mDragOffset == 0) {
					hide();
				} else {
					show();
				}
			}
			break;
		}
		}
		return mIsHandleViewUnder || isViewHit(mHandle, (int) x, (int) y) || isViewHit(mContent, (int) x, (int) y);
	}

	private boolean isViewHit(View view, int x, int y) {
		int[] viewLocation = new int[2];
		view.getLocationOnScreen(viewLocation);
		int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		int screenX = parentLocation[0] + x;
		int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() && screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
	}
}
