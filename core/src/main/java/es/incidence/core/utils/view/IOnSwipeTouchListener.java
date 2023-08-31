package es.incidence.core.utils.view;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class IOnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    private float prevX;
    private float prevY;

    private boolean swipedLeft;
    private boolean swipedRight;
    private boolean swipedUp;
    private boolean swipedBottom;
    public boolean isSwiped()
    {
        boolean res = false;
        if (swipedLeft || swipedRight || swipedUp || swipedBottom)
        {
            res = true;
        }

        return res;
    }


    public IOnSwipeTouchListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                prevX = event.getX();
                prevY = event.getY();
                swipedLeft = false;
                swipedRight = false;
                swipedUp = false;
                swipedLeft = false;

                break;
            case MotionEvent.ACTION_MOVE:

                float newX = event.getX();
                float newY = event.getY();

                if (Math.abs(newX - prevX) > Math.abs(newY - prevY))
                {
                    //LEFT - RiGHT Direction

                    if(newX > prevX) {
                        //RIGHT

                        if (!isSwiped())
                        {
                            swipedRight = true;
                            onSwipeRight();
                        }

                    } else {
                        //LEFT

                        if (!isSwiped())
                        {
                            swipedLeft = true;
                            onSwipeLeft();
                        }
                    }
                }
                else {
                    // UP-DOWN Direction

                    if (newY > prevY) {
                        //DOWN

                        if (!isSwiped())
                        {
                            swipedBottom = true;
                            onSwipeBottom();
                        }

                    } else {
                        //UP

                        if (!isSwiped())
                        {
                            swipedUp = true;
                            onSwipeTop();
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                swipedLeft = false;
                swipedRight = false;
                swipedUp = false;
                swipedLeft = false;
                break;
        }

        //return true;
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             boolean result = false;
             /*
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }*/
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public void onClick() {
    }
}