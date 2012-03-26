package com.tackmobile;

import java.util.ArrayList;
import java.util.HashSet;

import roboguice.util.Ln;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class GameboardView extends RelativeLayout implements OnTouchListener {
	
	protected Size tileSize;
	protected Rect gameboardRect;
	protected HashSet<GameTile> tiles;
	protected GameTile emptyTile, movedTile;
	private boolean boardCreated;
	private boolean lastMoveWasDrag;
	private PointF lastDragPoint;
	private TileServer tileServer;
	
	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		Drawable globe = getResources().getDrawable(R.drawable.globe);
		Bitmap original = ((BitmapDrawable)globe).getBitmap();
		tileServer = new TileServer(original, 4, 4, 68);
		
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
		tile.setImageBitmap(tileServer.serveRandomSlice());
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
				} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
					lastMoveWasDrag = true;
					if (lastDragPoint != null) {
						moveDraggedTilesByMotionEventDelta(event);
						lastDragPoint = new PointF(event.getRawX(), event.getRawY());
					} else {
						lastDragPoint = new PointF(event.getRawX(), event.getRawY());
					}
				} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					if (lastMoveWasDrag) {
						
					} else {
						animateTilesToEmptySpaceStartingWith(movedTile);
					}
					lastMoveWasDrag = false;
					lastDragPoint = null;
					movedTile = null;
				}
				return true;
			}
		} catch (ClassCastException e) {
			return false;
		}
	}

	protected void moveDraggedTilesByMotionEventDelta(MotionEvent event) {
		float dxEvent = lastDragPoint.x - event.getRawX();
		float dyEvent = lastDragPoint.y - event.getRawY();
		float dxTile = movedTile.getX() - dxEvent;
		float dyTile = movedTile.getY() - dyEvent;
		movedTile.setX(dxTile);
		movedTile.setY(dyTile);
	}

	private void animateTilesToEmptySpaceStartingWith(GameTile tile) {
		ArrayList<GameTileMotionDescriptor> motionDescriptors = getTilesBetweenEmptyTileAndTile(tile);
		emptyTile.setX(tile.getX());
		emptyTile.setY(tile.getY());
		emptyTile.coordinate = tile.coordinate;
		ObjectAnimator animator;
		for (final GameTileMotionDescriptor motionDescriptor : motionDescriptors) {
			Ln.d("Starting animation: %s", motionDescriptor);
			animator = ObjectAnimator.ofObject(
						motionDescriptor.tile,
						motionDescriptor.property,
						new FloatEvaluator(),
						motionDescriptor.from,
						motionDescriptor.to);
			animator.setDuration(16);
			animator.addListener(new AnimatorListener() {
				
				public void onAnimationStart(Animator animation) { }
				public void onAnimationCancel(Animator animation) { }
				public void onAnimationRepeat(Animator animation) { }
				
				public void onAnimationEnd(Animator animation) {
					motionDescriptor.tile.coordinate = motionDescriptor.finalCoordinate;
					motionDescriptor.tile.setX(motionDescriptor.finalRect.left);
					motionDescriptor.tile.setY(motionDescriptor.finalRect.top);
					Ln.d("Animation complete: %s", motionDescriptor);
				}
			});
			animator.start();
		}
	}

	private ArrayList<GameTileMotionDescriptor> getTilesBetweenEmptyTileAndTile(GameTile tile) {
		ArrayList<GameTileMotionDescriptor> descriptors = new ArrayList<GameTileMotionDescriptor>();
		Coordinate coordinate, finalCoordinate;
		GameTile foundTile;
		GameTileMotionDescriptor motionDescriptor;
		Rect rect;
		if (tile.isToRightOf(emptyTile)) {
			Ln.d("To right of empty tile");
			for (int i = tile.coordinate.column; i > emptyTile.coordinate.column; i--) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(tile.coordinate.row, i-1);
				rect = rectForCoordinate(finalCoordinate);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"x",
							foundTile.getX(),
							rect.left
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = rect;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isToLeftOf(emptyTile)) {
			Ln.d("To left of empty tile");
			for (int i = tile.coordinate.column; i < emptyTile.coordinate.column; i++) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(tile.coordinate.row, i+1);
				rect = rectForCoordinate(finalCoordinate);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"x",
							foundTile.getX(),
							rect.left
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = rect;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isAbove(emptyTile)) {
			Ln.d("Above empty tile");
			for (int i = tile.coordinate.row; i < emptyTile.coordinate.row; i++) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(i+1, tile.coordinate.column); 
				rect = rectForCoordinate(finalCoordinate);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"y",
							foundTile.getY(),
							rect.top
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = rect;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isBelow(emptyTile)) {
			Ln.d("Below empty tile");
			for (int i = tile.coordinate.row; i > emptyTile.coordinate.row; i--) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(i-1, tile.coordinate.column);
				rect = rectForCoordinate(finalCoordinate);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"y",
							foundTile.getY(),
							rect.top
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = rect;
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
		
		public Rect finalRect;
		public String property;
		public GameTile tile;
		public float from, to;
		public Coordinate finalCoordinate;
		
		public GameTileMotionDescriptor(GameTile tile, String property, float from, float to) {
			super();
			this.tile = tile;
			this.from = from;
			this.to = to;
			this.property = property;
		}

		@Override
		public String toString() {
			return "GameTileMotionDescriptor [property=" + property + ", tile="
					+ tile + ", from=" + from + ", to=" + to + "]";
		}
		
	}

}
