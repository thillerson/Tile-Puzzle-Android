package com.tackmobile;

import java.util.ArrayList;
import java.util.HashSet;

import roboguice.util.Ln;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class GameboardView extends RelativeLayout implements OnTouchListener {
	
	protected Size tileSize;
	protected Rect gameboardRect;
	protected HashSet<GameTile> tiles;
	protected GameTile emptyTile, movedTile;
	private boolean boardCreated;
	private boolean lastMoveWasDrag;

	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		createTiles();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (!boardCreated) {
			determineGameboardSizes();
			placeTiles();
			boardCreated = true;
		}
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
		// And why the hell doesn't adding a tile to the view update the tile's top and left?
		// Perhaps we'll never know.
		tile.setTop(tileRect.top);
		tile.setLeft(tileRect.left);
		Ln.d("Added tile: %s", tile);
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

	public boolean onTouch(View v, MotionEvent event) {
		try {
			GameTile touchedTile = (GameTile)v;
			Ln.d("%s\n\tTile touched: %s", touchedTile, event);
			if (touchedTile.isEmpty() || !touchedTile.isInRowOrColumnOf(emptyTile)) {
				Ln.d("Empty or immovable tile; ignoring");
				return false;
			} else {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					movedTile = touchedTile;
				} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					if (lastMoveWasDrag) {
						
					} else {
						animateTilesToEmptySpaceStartingWith(movedTile);
					}
					lastMoveWasDrag = false;
					movedTile = null;
				}
				return true;
			}
		} catch (ClassCastException e) {
			return false;
		}
	}

	private void animateTilesToEmptySpaceStartingWith(GameTile tile) {
		ArrayList<GameTileMotionDescriptor> motionDescriptors = getTilesBetweenEmptyTileAndTile(tile);
		TranslateAnimation animation;
		emptyTile.setLayoutParams(tile.getLayoutParams());
		emptyTile.setLeft(tile.getLeft());
		emptyTile.setTop(tile.getTop());
		for (final GameTileMotionDescriptor motionDescriptor : motionDescriptors) {
			Ln.d("Starting animation: %s", motionDescriptor);
			animation = new TranslateAnimation(
					Animation.ABSOLUTE, motionDescriptor.fromX,
					Animation.ABSOLUTE, motionDescriptor.toX,
					Animation.ABSOLUTE, motionDescriptor.fromY,
					Animation.ABSOLUTE, motionDescriptor.toY);
			animation.setFillAfter(true);
			animation.setFillEnabled(true);
			animation.setAnimationListener(new AnimationListener() {
				
				public void onAnimationStart(Animation animation) {
					Ln.d("Starting animation: %s", motionDescriptor.tile);
				}
				
				public void onAnimationRepeat(Animation animation) { }
				
				public void onAnimationEnd(Animation animation) {
					Ln.d("Ending animation: %s", motionDescriptor.tile);
				}
			});
			tile.startAnimation(animation);
		}
	}

	private ArrayList<GameTileMotionDescriptor> getTilesBetweenEmptyTileAndTile(GameTile tile) {
		ArrayList<GameTileMotionDescriptor> descriptors = new ArrayList<GameTileMotionDescriptor>();
		Coordinate coordinate;
		GameTile foundTile;
		GameTileMotionDescriptor motionDescriptor;
		Rect rect;
		if (tile.isToRightOf(emptyTile)) {
			Ln.d("To right of empty tile");
			for (int i = tile.coordinate.column; i > emptyTile.coordinate.column; i--) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				rect = rectForCoordinate(new Coordinate(tile.coordinate.row, i-1));
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							foundTile.getLeft(),
							rect.left,
							foundTile.getTop(),
							rect.top
						);
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isToLeftOf(emptyTile)) {
			Ln.d("To left of empty tile");
			for (int i = tile.coordinate.column; i < emptyTile.coordinate.column; i++) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				rect = rectForCoordinate(new Coordinate(tile.coordinate.row, i+1));
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							foundTile.getLeft(),
							rect.left,
							foundTile.getTop(),
							rect.top
						);
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isAbove(emptyTile)) {
			Ln.d("Above empty tile");
			for (int i = tile.coordinate.row; i < emptyTile.coordinate.row; i++) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				rect = rectForCoordinate(new Coordinate(i+1, tile.coordinate.column));
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							foundTile.getLeft(),
							rect.left,
							foundTile.getTop(),
							rect.top
						);
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isBelow(emptyTile)) {
			Ln.d("Below empty tile");
			for (int i = tile.coordinate.row; i > emptyTile.coordinate.row; i--) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				rect = rectForCoordinate(new Coordinate(i-1, tile.coordinate.column));
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							foundTile.getLeft(),
							rect.left,
							foundTile.getTop(),
							rect.top
						);
				descriptors.add(motionDescriptor);
			}
		}
		return descriptors;
	}
	
	protected GameTile getTileAtCoordinate(Coordinate coordinate) {
		Ln.d("Finding tile at %s", coordinate);
		for (GameTile tile : tiles) {
			if (tile.coordinate.matches(coordinate)) {
				Ln.d("Found tile %s", tile);
				return tile;
			}
		}
		return null;
	}

	protected GameTile createTileAtCoordinate(Coordinate coordinate) {
		GameTile tile = new GameTile(getContext(), coordinate);
		tiles.add(tile);
		tile.setOnTouchListener(this);
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

		public boolean matches(Coordinate coordinate) {
			return coordinate.row == row && coordinate.column == column;
		}

		public boolean sharesAxisWith(Coordinate coordinate) {
			return (row == coordinate.row || column == coordinate.column);
		}

		public boolean isToRightOf(Coordinate coordinate) {
			return sharesAxisWith(coordinate) && (column > coordinate.column);
		}

		public boolean isToLeftOf(Coordinate coordinate) {
			return sharesAxisWith(coordinate) && (column < coordinate.column);
		}

		public boolean isAbove(Coordinate coordinate) {
			return sharesAxisWith(coordinate) && (row < coordinate.row);
		}

		public boolean isBelow(Coordinate coordinate) {
			return sharesAxisWith(coordinate) && (row > coordinate.row);
		}
		
		@Override
		public String toString() {
			return "Coordinate [row=" + row + ", column=" + column + "]";
		}

	}
	
	public class GameTileMotionDescriptor {
		
		public GameTile tile;
		public int fromX, toX, fromY, toY;
		
		public GameTileMotionDescriptor(GameTile tile, int fromX, int toX, int fromY, int toY) {
			super();
			this.tile = tile;
			this.fromX = fromX;
			this.toX = toX;
			this.fromY = fromY;
			this.toY = toY;
		}
		
		@Override
		public String toString() {
			return "GameTileMotionDescriptor [tile=" + tile + ", fromX="
					+ fromX + ", toX=" + toX + ", fromY=" + fromY + ", toY="
					+ toY + "]";
		}
		
	}

}
