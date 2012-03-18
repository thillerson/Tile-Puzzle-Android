package com.tackmobile;

import roboguice.util.Ln;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

public class GameboardView extends View {
	
	protected ShapeDrawable squaw;

	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		squaw = new ShapeDrawable(new RectShape());
		squaw.getPaint().setColor(Color.RED);
		squaw.setBounds(100, 100, 200, 200);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Ln.d("Drarwring");
		squaw.draw(canvas);
	}

}
