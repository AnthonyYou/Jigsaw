package com.frontier.model;

/**
 * Created by frontier on 10/3/15.
 */
public interface ITouchEvent {
    public void onTouchDown(int x, int y);

    public void onTouchMove(int x, int y);

    public void onTouchUp(int x, int y);

    public void loseFocus();
}
