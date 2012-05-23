package com.tackmobile;

import java.util.ArrayList;
import java.util.HashSet;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class GameboardView extends RelativeLayout implements OnTouchListener {
	
	protected Size tileSize;
	protected RectF gameboardRect;
	protected HashSet<GameTile> tiles;
	protected GameTile emptyTile, movedTile;
	private boolean boardCreated;
	private PointF lastDragPoint;
	private TileServer tileServer;
	protected ArrayList<GameTileMotionDescriptor> currentMotionDescriptors;
	
	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		Drawable img = getResources().getDrawable(R.drawable.android);
		Bitmap original = ((BitmapDrawable)img).getBitmap();
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
			if (touchedTile.isEmpty() || !touchedTile.isInRowOrColumnOf(emptyTile)) {
				return false;
			} else {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					movedTile = touchedTile;
					currentMotionDescriptors = getTilesBetweenEmptyTileAndTile(movedTile);
				} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
					if (lastDragPoint != null) {
						moveDraggedTilesByMotionEventDelta(event);
					}
					lastDragPoint = new PointF(event.getRawX(), event.getRawY());
				} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					// reload the motion descriptors in case of position change.
					currentMotionDescriptors = getTilesBetweenEmptyTileAndTile(movedTile);
					// if last move was a dragging move and the move was over half way to the empty tile
					if (lastDragPoint != null && lastDragMovedAtLeastHalfWay()) {
						animateCurrentMovedTilesToEmptySpace();
					// otherwise, if it wasn't a drag, do the move
					} else if (lastDragPoint == null) {
						animateCurrentMovedTilesToEmptySpace();
					// Animate tiles back to origin
					} else {
						animateMovedTilesBackToOrigin();
					}
					currentMotionDescriptors = null;
					lastDragPoint = null;
					movedTile = null;
				}
				return true;
			}
		} catch (ClassCastException e) {
			return false;
		}
	}

	protected boolean lastDragMovedAtLeastHalfWay() {
		if (currentMotionDescriptors != null && currentMotionDescriptors.size() > 0) {
			GameTileMotionDescriptor firstMotionDescriptor = currentMotionDescriptors.get(0);
			if (firstMotionDescriptor.axialDelta > tileSize.width/2) {
				return true;
			}
		}
		return false;
	}

	protected void moveDraggedTilesByMotionEventDelta(MotionEvent event) {
		boolean impossibleMove = true;
		float dxTile, dyTile;
		float dxEvent = event.getRawX() - lastDragPoint.x;
		float dyEvent = event.getRawY() - lastDragPoint.y;
		GameTile tile;
		for (GameTileMotionDescriptor gameTileMotionDescriptor : currentMotionDescriptors) {
			tile = gameTileMotionDescriptor.tile;
			dxTile = tile.getX() + dxEvent;
			dyTile = tile.getY() + dyEvent;
			
			RectF candidateRect = new RectF(dxTile, dyTile, dxTile + tile.getWidth(), dyTile + tile.getHeight());
			HashSet<GameTile> tilesToCheck = null;
			if (tile.coordinate.row == emptyTile.coordinate.row) {
				tilesToCheck = allTilesInRow(tile.coordinate.row);
			} else if (tile.coordinate.column == emptyTile.coordinate.column) {
				tilesToCheck = allTilesInColumn(tile.coordinate.column);
			}
			
			boolean candidateRectInGameboard = (gameboardRect.contains(candidateRect));
			boolean collides = candidateRectForTileCollidesWithAnyTileInSet(candidateRect, tile, tilesToCheck);
			
			impossibleMove = impossibleMove && (!candidateRectInGameboard || collides);
		}
		if (!impossibleMove) {
			for (GameTileMotionDescriptor gameTileMotionDescriptor : currentMotionDescriptors) {
				tile = gameTileMotionDescriptor.tile;
				dxTile = tile.getX() + dxEvent;
				dyTile = tile.getY() + dyEvent;
				if (!impossibleMove) {
					if (tile.coordinate.row == emptyTile.coordinate.row) {
						tile.setX(dxTile);
					} else if (tile.coordinate.column == emptyTile.coordinate.column) {
						tile.setY(dyTile);
					}
				}
			}
		}
	}

	protected boolean candidateRectForTileCollidesWithAnyTileInSet(RectF candidateRect, GameTile tile, HashSet<GameTile> set) {
		RectF otherTileRect;
		for (GameTile otherTile : set) {
			if (!otherTile.isEmpty() && otherTile != tile) {
				otherTileRect = new RectF(otherTile.getX(), otherTile.getY(), otherTile.getX() + otherTile.getWidth(), otherTile.getY() + otherTile.getHeight());
				if (RectF.intersects(otherTileRect, candidateRect)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected void animateCurrentMovedTilesToEmptySpace() {
		emptyTile.setX(movedTile.getX());
		emptyTile.setY(movedTile.getY());
		emptyTile.coordinate = movedTile.coordinate;
		ObjectAnimator animator;
		for (final GameTileMotionDescriptor motionDescriptor : currentMotionDescriptors) {
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
				}
			});
			animator.start();
		}
	}
	
	protected void animateMovedTilesBackToOrigin() {
		ObjectAnimator animator;
		if (currentMotionDescriptors != null) {
			for (final GameTileMotionDescriptor motionDescriptor : currentMotionDescriptors) {
				animator = ObjectAnimator.ofObject(
						motionDescriptor.tile,
						motionDescriptor.property,
						new FloatEvaluator(),
						motionDescriptor.currentPosition(),
						motionDescriptor.originalPosition());
				animator.setDuration(16);
				animator.addListener(new AnimatorListener() {

					public void onAnimationStart(Animator animation) { }
					public void onAnimationCancel(Animator animation) { }
					public void onAnimationRepeat(Animator animation) { }
					public void onAnimationEnd(Animator animation) {
					}
				});
				animator.start();
			}
		}
	}

	private ArrayList<GameTileMotionDescriptor> getTilesBetweenEmptyTileAndTile(GameTile tile) {
		ArrayList<GameTileMotionDescriptor> descriptors = new ArrayList<GameTileMotionDescriptor>();
		Coordinate coordinate, finalCoordinate;
		GameTile foundTile;
		GameTileMotionDescriptor motionDescriptor;
		Rect finalRect, currentRect;
		float axialDelta;
		if (tile.isToRightOf(emptyTile)) {
			for (int i = tile.coordinate.column; i > emptyTile.coordinate.column; i--) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(tile.coordinate.row, i-1);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getX() - currentRect.left);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"x",
							foundTile.getX(),
							finalRect.left
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isToLeftOf(emptyTile)) {
			for (int i = tile.coordinate.column; i < emptyTile.coordinate.column; i++) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(tile.coordinate.row, i+1);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getX() - currentRect.left);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"x",
							foundTile.getX(),
							finalRect.left
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isAbove(emptyTile)) {
			for (int i = tile.coordinate.row; i < emptyTile.coordinate.row; i++) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(i+1, tile.coordinate.column); 
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getY() - currentRect.top);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"y",
							foundTile.getY(),
							finalRect.top
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isBelow(emptyTile)) {
			for (int i = tile.coordinate.row; i > emptyTile.coordinate.row; i--) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate) ;
				finalCoordinate = new Coordinate(i-1, tile.coordinate.column);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getY() - currentRect.top);
				motionDescriptor = new GameTileMotionDescriptor(
							foundTile,
							"y",
							foundTile.getY(),
							finalRect.top
						);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		}
		return descriptors;
	}
	
	protected GameTile getTileAtCoordinate(Coordinate coordinate) {
		//Ln.d("Finding tile at %s", coordinate);
		for (GameTile tile : tiles) {
			if (tile.coordinate.matches(coordinate)) {
				//Ln.d("Found tile %s", tile);
				return tile;
			}
		}
		return null;
	}
	
	protected HashSet<GameTile> allTilesInRow(int row) {
		HashSet<GameTile> tilesInRow = new HashSet<GameTile>();
		for (GameTile tile : tiles) {
			if (tile.coordinate.row == row) {
				tilesInRow.add(tile);
			}
		}
		return tilesInRow;
	}

	protected HashSet<GameTile> allTilesInColumn(int column) {
		HashSet<GameTile> tilesInColumn = new HashSet<GameTile>();
		for (GameTile tile : tiles) {
			if (tile.coordinate.column == column) {
				tilesInColumn.add(tile);
			}
		}
		return tilesInColumn;
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
		int tileDimen = Math.round(getResources().getDimension(R.dimen.tile_size));
		tileSize = new Size(tileDimen, tileDimen);
		int gameboardWidth = tileSize.width * 4;
		int gameboardHeight = tileSize.height * 4;
		int gameboardTop = viewHeight/2 - gameboardHeight/2;
		int gameboardLeft = viewWidth/2 - gameboardWidth/2;
		gameboardRect = new RectF(gameboardLeft, gameboardTop, gameboardLeft + gameboardWidth, gameboardTop + gameboardHeight);
		createTiles();
	}

	protected Rect rectForCoordinate(Coordinate coordinate) {
		int gameboardY = (int) Math.floor(gameboardRect.top);
		int gameboardX = (int) Math.floor(gameboardRect.left);
		int top = (coordinate.row * tileSize.height) + gameboardY;
		int left = (coordinate.column * tileSize.width) + gameboardX;
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
		public float from, to, axialDelta;
		public Coordinate finalCoordinate;
		
		public GameTileMotionDescriptor(GameTile tile, String property, float from, float to) {
			super();
			this.tile = tile;
			this.from = from;
			this.to = to;
			this.property = property;
		}

		public float currentPosition() {
			if (property.equals("x")) {
				return tile.getX();
			} else if (property.equals("y")) {
				return tile.getY();
			}
			return 0;
		}

		public float originalPosition() {
			Rect originalRect = rectForCoordinate(tile.coordinate);
			if (property.equals("x")) {
				return originalRect.left;
			} else if (property.equals("y")) {
				return originalRect.top;
			}
			return 0;
		}

		@Override
		public String toString() {
			return "GameTileMotionDescriptor [property=" + property + ", tile="
					+ tile + ", from=" + from + ", to=" + to + "]";
		}
		
	}

}
