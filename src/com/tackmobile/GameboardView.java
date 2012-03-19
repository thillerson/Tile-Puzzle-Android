package com.tackmobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class GameboardView extends View {
	
	protected GameTile singleTile;

	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		singleTile = new GameTile();
		singleTile.getPaint().setColor(Color.RED);
		singleTile.setBounds(100, 100, 200, 200);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		singleTile.draw(canvas);
	}

}
