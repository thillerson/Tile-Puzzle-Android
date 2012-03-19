package com.tackmobile;

import java.util.HashSet;

import roboguice.util.Ln;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class GameboardView extends View {
	
	protected Size tileSize;
	protected Rect gameboardRect;
	protected HashSet<GameTile> tiles;

	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		determineGameboardSizes();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawTiles(canvas);
	}
	
	protected void drawTiles(Canvas canvas) {
		for (GameTile tile : tiles) {
			placeTile(tile, canvas);
		}
	}
	
	protected void placeTile(GameTile tile, Canvas canvas) {
		tile.setBounds(rectForCoordinate(tile.coordinate));
		tile.draw(canvas);
	}

	protected void createTiles() {
		tiles = new HashSet<GameTile>();
		for (int rowI=0; rowI<4; rowI++) {
			for (int colI=0; colI<4; colI++) {
				createTileAtCoordinate( new Coordinate(rowI, colI) );
			}
		}
	}

	protected void createTileAtCoordinate(Coordinate coordinate) {
		tiles.add(new GameTile(coordinate));
	}

	protected void determineGameboardSizes() {
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		Ln.d("width %d, height %d", viewWidth, viewHeight);
		// ostensibly tiles can be sized based on view geometry. Hardcode for now.
		tileSize = new Size(68, 68);
		int gameboardWidth = tileSize.width * 4;
		int gameboardHeight = tileSize.height * 4;
		int gameboardTop = viewHeight/2 - gameboardHeight/2;
		int gameboardLeft = viewWidth/2 - gameboardWidth/2;
		gameboardRect = new Rect(gameboardLeft, gameboardTop, gameboardLeft + gameboardWidth, gameboardTop + gameboardHeight);
		createTiles();
	}

	protected Rect rectForCoordinate(Coordinate coordinate) {
		int top = (coordinate.row * tileSize.height) + gameboardRect.top;
		int left = (coordinate.column * tileSize.width) + gameboardRect.left;
		return new Rect(left, top, left + tileSize.width, top + tileSize.height);
	}
	
	public class Size {
		
		public int width;
		public int height;
		
		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
	}

	public class Coordinate {
		
		public int row;
		public int column;

		public Coordinate(int row, int column) {
			this.row = row;
			this.column = column;
		}

	}

}
