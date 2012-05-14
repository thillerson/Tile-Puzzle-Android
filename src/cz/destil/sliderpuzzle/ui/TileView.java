package cz.destil.sliderpuzzle.ui;

import cz.destil.sliderpuzzle.data.Coordinate;
import android.content.Context;
import android.widget.ImageView;

/**
 * 
 * ImageView displaying tile of the puzzle. Contains useful functions for
 * comparing with other tiles.
 * 
 * Based on:
 * https://github.com/thillerson/Android-Slider-Puzzle/blob/master/src/
 * com/tackmobile/GameTile.java
 * 
 * @author David Vavra
 */
public class TileView extends ImageView {

	public Coordinate coordinate;
	public int originalIndex;
	private boolean empty;

	public TileView(Context context, int originalIndex) {
		super(context);
		this.originalIndex = originalIndex;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
		if (empty) {
			setBackgroundDrawable(null);
			setAlpha(0);
		}
	}

	public boolean isInRowOrColumnOf(TileView otherTile) {
		return (coordinate.sharesAxisWith(otherTile.coordinate));
	}

	public boolean isToRightOf(TileView tile) {
		return coordinate.isToRightOf(tile.coordinate);
	}

	public boolean isToLeftOf(TileView tile) {
		return coordinate.isToLeftOf(tile.coordinate);
	}

	public boolean isAbove(TileView tile) {
		return coordinate.isAbove(tile.coordinate);
	}

	public boolean isBelow(TileView tile) {
		return coordinate.isBelow(tile.coordinate);
	}

}
