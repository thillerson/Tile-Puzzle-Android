package cz.destil.sliderpuzzle.ui;

import java.util.ArrayList;
import java.util.LinkedList;

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
public class GameBoardView extends RelativeLayout implements OnTouchListener {

	public static final int GRID_SIZE = 4; // 4x4
	private int tileSize;
	private ArrayList<TileView> tiles;
	private TileView emptyTile, movedTile;
	private boolean boardCreated;
	private RectF gameboardRect;
	private PointF lastDragPoint;
	private ArrayList<GameTileMotionDescriptor> currentMotionDescriptors;
	private LinkedList<Integer> tileOrder;

	public GameBoardView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (!boardCreated) {
			determineGameboardSizes();
			fillTiles();
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
		int gameboardSize = tileSize * GRID_SIZE;
		// center gameboard
		int gameboardTop = viewHeight / 2 - gameboardSize / 2;
		int gameboardLeft = viewWidth / 2 - gameboardSize / 2;
		gameboardRect = new RectF(gameboardLeft, gameboardTop, gameboardLeft + gameboardSize, gameboardTop
				+ gameboardSize);
	}

	/**
	 * Fills gameboard with tiles sliced from the globe image.
	 */
	public void fillTiles() {
		removeAllViews();
		// load image to slicer
		Drawable globe = getResources().getDrawable(R.drawable.globe);
		Bitmap original = ((BitmapDrawable) globe).getBitmap();
		TileSlicer tileSlicer = new TileSlicer(original, GRID_SIZE, getContext());
		// fill gameboard with slices
		if (tileOrder == null) {
			tileSlicer.randomizeSlices();
		} else {
			tileSlicer.setSliceOrder(tileOrder);
		}
		tiles = new ArrayList<TileView>();
		for (int rowI = 0; rowI < GRID_SIZE; rowI++) {
			for (int colI = 0; colI < GRID_SIZE; colI++) {
				TileView tile;
				if (tileOrder == null) {
					tile = tileSlicer.getTile();
				} else {
					tile = tileSlicer.getTile();
				}
				tile.coordinate = new Coordinate(rowI, colI);
				if (tile.isEmpty()) {
					emptyTile = tile;
				}
				placeTile(tile);
				tiles.add(tile);
			}
		}
	}

	/**
	 * Places tile on appropriate place in the layout
	 * 
	 * @param tile
	 *            Tile to place
	 */
	private void placeTile(TileView tile) {
		Rect tileRect = rectForCoordinate(tile.coordinate);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(tileSize, tileSize);
		params.topMargin = tileRect.top;
		params.leftMargin = tileRect.left;
		addView(tile, params);
		tile.setOnTouchListener(this);
	}

	/**
	 * Handling of touch events. High-level logic for moving tiles on gameboard.
	 */
	public boolean onTouch(View v, MotionEvent event) {
		TileView touchedTile = (TileView) v;
		if (touchedTile.isEmpty() || !touchedTile.isInRowOrColumnOf(emptyTile)) {
			return false;
		} else {
			// start of the gesture
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				movedTile = touchedTile;
				currentMotionDescriptors = getTilesBetweenEmptyTileAndTile(movedTile);
				// during the gesture
			} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				if (lastDragPoint != null) {
					followFinger(event);
				}
				lastDragPoint = new PointF(event.getRawX(), event.getRawY());
				// end of gesture
			} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				// reload the motion descriptors in case of position change.
				currentMotionDescriptors = getTilesBetweenEmptyTileAndTile(movedTile);
				// if drag was over 50%, do the move
				if (lastDragPoint != null && lastDragMovedAtLeastHalfWay()) {
					animateTilesToEmptySpace();
					// if it was a click, do the move
				} else if (lastDragPoint == null || lastDragMovedMinimally()) {
					animateTilesToEmptySpace();
					// if it was a drag less than 50%, animate tiles back
				} else {
					animateTilesBackToOrigin();
				}
				currentMotionDescriptors = null;
				lastDragPoint = null;
				movedTile = null;
			}
			return true;
		}
	}

	/**
	 * @return Whether last drag moved with the tile more than 50% of its size
	 */
	private boolean lastDragMovedAtLeastHalfWay() {
		if (currentMotionDescriptors != null && currentMotionDescriptors.size() > 0) {
			GameTileMotionDescriptor firstMotionDescriptor = currentMotionDescriptors.get(0);
			if (firstMotionDescriptor.axialDelta > tileSize / 2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Whether last drag moved just a little = involuntary move during
	 *         click
	 */
	private boolean lastDragMovedMinimally() {
		if (currentMotionDescriptors != null && currentMotionDescriptors.size() > 0) {
			GameTileMotionDescriptor firstMotionDescriptor = currentMotionDescriptors.get(0);
			if (firstMotionDescriptor.axialDelta < tileSize / 20) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Follows finger while dragging all currently moved tiles. Allows movement
	 * only along x axis for row and y axis for column.
	 * 
	 * @param event
	 */
	private void followFinger(MotionEvent event) {
		boolean impossibleMove = true;
		float dxTile, dyTile;
		float dxEvent = event.getRawX() - lastDragPoint.x;
		float dyEvent = event.getRawY() - lastDragPoint.y;
		TileView tile;
		for (GameTileMotionDescriptor gameTileMotionDescriptor : currentMotionDescriptors) {
			tile = gameTileMotionDescriptor.tile;
			dxTile = tile.getX() + dxEvent;
			dyTile = tile.getY() + dyEvent;
			// detect if this move is valid
			RectF candidateRect = new RectF(dxTile, dyTile, dxTile + tile.getWidth(), dyTile + tile.getHeight());
			ArrayList<TileView> tilesToCheck = null;
			if (tile.coordinate.row == emptyTile.coordinate.row) {
				tilesToCheck = allTilesInRow(tile.coordinate.row);
			} else if (tile.coordinate.column == emptyTile.coordinate.column) {
				tilesToCheck = allTilesInColumn(tile.coordinate.column);
			}

			boolean candidateRectInGameboard = (gameboardRect.contains(candidateRect));
			boolean collides = collidesWithTitles(candidateRect, tile, tilesToCheck);

			impossibleMove = impossibleMove && (!candidateRectInGameboard || collides);
		}
		if (!impossibleMove) {
			// perform move for all moved tiles in the descriptors
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

	/**
	 * @param candidateRect
	 *            rectangle to check
	 * @param tile
	 *            tile belonging to rectangle
	 * @param tilesToCheck
	 *            list of tiles to check
	 * @return Whether candidateRect collides with any tilesToCheck
	 */
	private boolean collidesWithTitles(RectF candidateRect, TileView tile, ArrayList<TileView> tilesToCheck) {
		RectF otherTileRect;
		for (TileView otherTile : tilesToCheck) {
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

	/**
	 * Performs animation of currently moved tiles into empty space. Happens
	 * when valid tile is clicked or is dragged over 50%.
	 */
	private void animateTilesToEmptySpace() {
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

	/**
	 * Performs animation of currently moved tiles back to origin. Happens when
	 * the drag was less than 50%.
	 */
	private void animateTilesBackToOrigin() {
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

	/**
	 * Finds tiles between checked tile and empty tile and initializes motion
	 * descriptors for those tiles.
	 * 
	 * @param tile
	 *            A tile to be checked
	 * @return list of tiles between checked tile and empty tile
	 */
	private ArrayList<GameTileMotionDescriptor> getTilesBetweenEmptyTileAndTile(TileView tile) {
		ArrayList<GameTileMotionDescriptor> descriptors = new ArrayList<GameTileMotionDescriptor>();
		Coordinate coordinate, finalCoordinate;
		TileView foundTile;
		GameTileMotionDescriptor motionDescriptor;
		Rect finalRect, currentRect;
		float axialDelta;
		if (tile.isToRightOf(emptyTile)) {
			// add all tiles left of the tile
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
			// add all tiles right of the tile
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
			// add all tiles bellow the tile
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
			// add all tiles above the tile
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

	/**
	 * @param coordinate
	 *            coordinate of the tile
	 * @return tile at given coordinate
	 */
	private TileView getTileAtCoordinate(Coordinate coordinate) {
		for (TileView tile : tiles) {
			if (tile.coordinate.matches(coordinate)) {
				return tile;
			}
		}
		return null;
	}

	/**
	 * @param row
	 *            number of row
	 * @return list of tiles in the row
	 */
	private ArrayList<TileView> allTilesInRow(int row) {
		ArrayList<TileView> tilesInRow = new ArrayList<TileView>();
		for (TileView tile : tiles) {
			if (tile.coordinate.row == row) {
				tilesInRow.add(tile);
			}
		}
		return tilesInRow;
	}

	/**
	 * @param column
	 *            number of column
	 * @return list of tiles in the column
	 */
	private ArrayList<TileView> allTilesInColumn(int column) {
		ArrayList<TileView> tilesInColumn = new ArrayList<TileView>();
		for (TileView tile : tiles) {
			if (tile.coordinate.column == column) {
				tilesInColumn.add(tile);
			}
		}
		return tilesInColumn;
	}

	/**
	 * @param coordinate
	 * @return Rectangle for given coordinate
	 */
	private Rect rectForCoordinate(Coordinate coordinate) {
		int gameboardY = (int) Math.floor(gameboardRect.top);
		int gameboardX = (int) Math.floor(gameboardRect.left);
		int top = (coordinate.row * tileSize) + gameboardY;
		int left = (coordinate.column * tileSize) + gameboardX;
		return new Rect(left, top, left + tileSize, top + tileSize);
	}

	/**
	 * Returns current tile locations. Useful for preserving state when
	 * orientation changes.
	 * 
	 * @return current tile locations
	 */
	public LinkedList<Integer> getTileOrder() {
		LinkedList<Integer> tileLocations = new LinkedList<Integer>();
		for (int rowI = 0; rowI < GRID_SIZE; rowI++) {
			for (int colI = 0; colI < GRID_SIZE; colI++) {
				TileView tile = getTileAtCoordinate(new Coordinate(rowI, colI));
				tileLocations.add(tile.originalIndex);
			}
		}
		return tileLocations;
	}

	/**
	 * Sets tile locations from previous state.
	 * 
	 * @param tileLocations list of integers marking order
	 */
	public void setTileOrder(LinkedList<Integer> tileLocations) {
		this.tileOrder = tileLocations;
	}

	/**
	 * Describes movement of the tile. It is used to move several tiles at once.
	 */
	public class GameTileMotionDescriptor {

		public Rect finalRect;
		public String property; // "x" or "y"
		public TileView tile;
		public float from, to, axialDelta;
		public Coordinate finalCoordinate;

		public GameTileMotionDescriptor(TileView tile, String property, float from, float to) {
			super();
			this.tile = tile;
			this.from = from;
			this.to = to;
			this.property = property;
		}

		/**
		 * @return current position of the tile
		 */
		public float currentPosition() {
			if (property.equals("x")) {
				return tile.getX();
			} else if (property.equals("y")) {
				return tile.getY();
			}
			return 0;
		}

		/**
		 * @return original position of the tile. It is used in movement to
		 *         original position.
		 */
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
