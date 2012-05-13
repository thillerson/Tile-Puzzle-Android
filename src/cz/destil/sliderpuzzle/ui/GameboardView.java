package cz.destil.sliderpuzzle.ui;

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
import cz.destil.sliderpuzzle.R;
import cz.destil.sliderpuzzle.data.Coordinate;
import cz.destil.sliderpuzzle.util.TileSlicer;

/**
 * 
 * Layout handling creation and interaction of the game tiles. Captures gestures
 * and performs animations.
 * 
 * Based on:
 * https://github.com/thillerson/Android-Slider-Puzzle/blob/master/src/
 * com/tackmobile/GameboardView.java
 * 
 * @author David Vavra
 * 
 */
public class GameboardView extends RelativeLayout implements OnTouchListener {

	public static final int GRID_SIZE = 4; // 4x4
	private RectF gameboardRect;
	private ArrayList<GameTile> tiles;
	private GameTile emptyTile, movedTile;
	private boolean boardCreated;
	private PointF lastDragPoint;
	private int tileSize;
	private ArrayList<GameTileMotionDescriptor> currentMotionDescriptors;

	public GameboardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (!boardCreated) {
			determineGameboardSizes();
			// load image to slicer
			Drawable globe = getResources().getDrawable(R.drawable.globe);
			Bitmap original = ((BitmapDrawable) globe).getBitmap();
			TileSlicer tileSlicer = new TileSlicer(original, GRID_SIZE);
			
			fillTiles(tileSlicer);
			boardCreated = true;
		}
	}
	
	/**
	 * Detect gameboard size and tile size based on current screen.
	 */
	private void determineGameboardSizes() {
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		// fit in portrait or landscape
		if (viewWidth > viewHeight) {
			tileSize = viewHeight / GRID_SIZE;
		} else {
			tileSize = viewWidth / GRID_SIZE;
		}
		// leave a bit on the sides
		tileSize -= 5;
		int gameboardSize = tileSize * GRID_SIZE;
		// center gameboard
		int gameboardTop = viewHeight / 2 - gameboardSize / 2;
		int gameboardLeft = viewWidth / 2 - gameboardSize / 2;
		gameboardRect = new RectF(gameboardLeft, gameboardTop, gameboardLeft + gameboardSize, gameboardTop
				+ gameboardSize);
	}

	/**
	 * Fills gameboard with tiles
	 * @param tileSlicer TileSlicer with loaded image
	 */
	private void fillTiles(TileSlicer tileSlicer) {
		tiles = new ArrayList<GameTile>();
		for (int rowI = 0; rowI < GRID_SIZE; rowI++) {
			for (int colI = 0; colI < GRID_SIZE; colI++) {
				GameTile tile = new GameTile(getContext(), new Coordinate(rowI, colI));
				tile.setOnTouchListener(this);
				if (rowI == GRID_SIZE - 1 && colI == GRID_SIZE - 1) {
					// empty tile
					emptyTile = tile;
					tile.setEmpty(true);
				} else {
					// tile with image - set random tile from slicer
					tile.setImageBitmap(tileSlicer.getRandomSlice());				
				}
				placeTile(tile);
				tiles.add(tile);
			}
		}
	}
	
	/**
	 * Places tile on appropriate place in the layout
	 * @param tile Tile to place
	 */
	private void placeTile(GameTile tile) {
		Rect tileRect = rectForCoordinate(tile.coordinate);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(tileSize, tileSize);
		params.topMargin = tileRect.top;
		params.leftMargin = tileRect.left;
		addView(tile, params);	
	}

	public boolean onTouch(View v, MotionEvent event) {
		try {
			GameTile touchedTile = (GameTile) v;
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
					// if last move was a dragging move and the move was over
					// half way to the empty tile
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
			if (firstMotionDescriptor.axialDelta > tileSize / 2) {
				return true;
			}
		}
		return false;
	}

	private void moveDraggedTilesByMotionEventDelta(MotionEvent event) {
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

	private boolean candidateRectForTileCollidesWithAnyTileInSet(RectF candidateRect, GameTile tile,
			HashSet<GameTile> set) {
		RectF otherTileRect;
		for (GameTile otherTile : set) {
			if (!otherTile.isEmpty() && otherTile != tile) {
				otherTileRect = new RectF(otherTile.getX(), otherTile.getY(), otherTile.getX() + otherTile.getWidth(),
						otherTile.getY() + otherTile.getHeight());
				if (RectF.intersects(otherTileRect, candidateRect)) {
					return true;
				}
			}
		}
		return false;
	}

	private void animateCurrentMovedTilesToEmptySpace() {
		emptyTile.setX(movedTile.getX());
		emptyTile.setY(movedTile.getY());
		emptyTile.coordinate = movedTile.coordinate;
		ObjectAnimator animator;
		for (final GameTileMotionDescriptor motionDescriptor : currentMotionDescriptors) {
			animator = ObjectAnimator.ofObject(motionDescriptor.tile, motionDescriptor.property, new FloatEvaluator(),
					motionDescriptor.from, motionDescriptor.to);
			animator.setDuration(16);
			animator.addListener(new AnimatorListener() {

				public void onAnimationStart(Animator animation) {
				}

				public void onAnimationCancel(Animator animation) {
				}

				public void onAnimationRepeat(Animator animation) {
				}

				public void onAnimationEnd(Animator animation) {
					motionDescriptor.tile.coordinate = motionDescriptor.finalCoordinate;
					motionDescriptor.tile.setX(motionDescriptor.finalRect.left);
					motionDescriptor.tile.setY(motionDescriptor.finalRect.top);
				}
			});
			animator.start();
		}
	}

	private void animateMovedTilesBackToOrigin() {
		ObjectAnimator animator;
		if (currentMotionDescriptors != null) {
			for (final GameTileMotionDescriptor motionDescriptor : currentMotionDescriptors) {
				animator = ObjectAnimator.ofObject(motionDescriptor.tile, motionDescriptor.property,
						new FloatEvaluator(), motionDescriptor.currentPosition(), motionDescriptor.originalPosition());
				animator.setDuration(16);
				animator.addListener(new AnimatorListener() {

					public void onAnimationStart(Animator animation) {
					}

					public void onAnimationCancel(Animator animation) {
					}

					public void onAnimationRepeat(Animator animation) {
					}

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
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate);
				finalCoordinate = new Coordinate(tile.coordinate.row, i - 1);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getX() - currentRect.left);
				motionDescriptor = new GameTileMotionDescriptor(foundTile, "x", foundTile.getX(), finalRect.left);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isToLeftOf(emptyTile)) {
			for (int i = tile.coordinate.column; i < emptyTile.coordinate.column; i++) {
				coordinate = new Coordinate(tile.coordinate.row, i);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate);
				finalCoordinate = new Coordinate(tile.coordinate.row, i + 1);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getX() - currentRect.left);
				motionDescriptor = new GameTileMotionDescriptor(foundTile, "x", foundTile.getX(), finalRect.left);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isAbove(emptyTile)) {
			for (int i = tile.coordinate.row; i < emptyTile.coordinate.row; i++) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate);
				finalCoordinate = new Coordinate(i + 1, tile.coordinate.column);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getY() - currentRect.top);
				motionDescriptor = new GameTileMotionDescriptor(foundTile, "y", foundTile.getY(), finalRect.top);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		} else if (tile.isBelow(emptyTile)) {
			for (int i = tile.coordinate.row; i > emptyTile.coordinate.row; i--) {
				coordinate = new Coordinate(i, tile.coordinate.column);
				foundTile = (tile.coordinate.matches(coordinate)) ? tile : getTileAtCoordinate(coordinate);
				finalCoordinate = new Coordinate(i - 1, tile.coordinate.column);
				currentRect = rectForCoordinate(foundTile.coordinate);
				finalRect = rectForCoordinate(finalCoordinate);
				axialDelta = Math.abs(foundTile.getY() - currentRect.top);
				motionDescriptor = new GameTileMotionDescriptor(foundTile, "y", foundTile.getY(), finalRect.top);
				motionDescriptor.finalCoordinate = finalCoordinate;
				motionDescriptor.finalRect = finalRect;
				motionDescriptor.axialDelta = axialDelta;
				descriptors.add(motionDescriptor);
			}
		}
		return descriptors;
	}

	private GameTile getTileAtCoordinate(Coordinate coordinate) {
		for (GameTile tile : tiles) {
			if (tile.coordinate.matches(coordinate)) {
				return tile;
			}
		}
		return null;
	}

	private HashSet<GameTile> allTilesInRow(int row) {
		HashSet<GameTile> tilesInRow = new HashSet<GameTile>();
		for (GameTile tile : tiles) {
			if (tile.coordinate.row == row) {
				tilesInRow.add(tile);
			}
		}
		return tilesInRow;
	}

	private HashSet<GameTile> allTilesInColumn(int column) {
		HashSet<GameTile> tilesInColumn = new HashSet<GameTile>();
		for (GameTile tile : tiles) {
			if (tile.coordinate.column == column) {
				tilesInColumn.add(tile);
			}
		}
		return tilesInColumn;
	}

	private Rect rectForCoordinate(Coordinate coordinate) {
		int gameboardY = (int) Math.floor(gameboardRect.top);
		int gameboardX = (int) Math.floor(gameboardRect.left);
		int top = (coordinate.row * tileSize) + gameboardY;
		int left = (coordinate.column * tileSize) + gameboardX;
		return new Rect(left, top, left + tileSize, top + tileSize);
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

	}

}
