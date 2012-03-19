package com.tackmobile;

import java.util.HashSet;

import roboguice.util.Ln;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class GameboardView extends RelativeLayout {
	
	protected Size tileSize;
	protected Rect gameboardRect;
	protected HashSet<GameTile> tiles;
	protected GameTile emptyTile;

	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		createTiles();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		determineGameboardSizes();
		placeTiles();
	}

	protected void placeTiles() {
		for (GameTile tile : tiles) {
			placeTile(tile);
		}
	}
	
	protected void placeTile(GameTile tile) {
		Rect tileRect = rectForCoordinate(tile.coordinate);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(tileSize.width, tileSize.height);
		params.topMargin = tileRect.top;
		params.leftMargin = tileRect.left;
		addView(tile, params);
	}

	protected void createTiles() {
		tiles = new HashSet<GameTile>();
		for (int rowI=0; rowI<4; rowI++) {
			for (int colI=0; colI<4; colI++) {
				GameTile tile = createTileAtCoordinate( new Coordinate(rowI, colI) );
				if (rowI == 3 && colI == 3) {
					emptyTile = tile;
					tile.setEmpty(true);
				}
			}
		}
	}

	protected GameTile createTileAtCoordinate(Coordinate coordinate) {
		GameTile tile = new GameTile(getContext(), coordinate);
		tiles.add(tile);
		return tile;
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
