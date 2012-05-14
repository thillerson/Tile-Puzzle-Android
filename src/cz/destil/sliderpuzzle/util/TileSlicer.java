package cz.destil.sliderpuzzle.util;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import cz.destil.sliderpuzzle.ui.TileView;

/**
 * 
 * Slices original bitmap into tiles and adds border. Provides randomized access
 * to tiles.
 * 
 * Based on
 * https://github.com/thillerson/Android-Slider-Puzzle/blob/master/src/com
 * /tackmobile/TileServer.java
 * 
 * @author David Vavra
 */
public class TileSlicer {

	public static final int RANDOM_SLICE = -1;
	private Bitmap original;
	private int tileSize, gridSize;
	private ArrayList<Bitmap> slices;
	private Random random;
	private Context context;

	/**
	 * Initializes TileSlicer.
	 * 
	 * @param original
	 *            Bitmap which should be sliced
	 * @param gridSize
	 *            Grid size, for example 4 for 4x4 grid
	 */
	public TileSlicer(Bitmap original, int gridSize, Context context) {
		super();
		this.original = original;
		this.gridSize = gridSize;
		this.tileSize = original.getWidth() / gridSize;
		this.context = context;
		random = new Random();
		slices = new ArrayList<Bitmap>();
		sliceOriginal();
	}

	/**
	 * Slices original bitmap and adds border to slices.
	 */
	private void sliceOriginal() {
		int x, y;
		Bitmap bitmap;
		for (int rowI = 0; rowI < gridSize; rowI++) {
			for (int colI = 0; colI < gridSize; colI++) {
				// don't slice last part - empty slice
				if (rowI == gridSize - 1 && colI == gridSize - 1) {
					continue;
				}
				x = rowI * tileSize;
				y = colI * tileSize;
				// slice
				bitmap = Bitmap.createBitmap(original, x, y, tileSize, tileSize);
				// draw border lines
				Canvas canvas = new Canvas(bitmap);
				Paint paint = new Paint();
				paint.setColor(Color.parseColor("#fbfdff"));
				int end = tileSize - 1;
				canvas.drawLine(0, 0, 0, end, paint);
				canvas.drawLine(0, end, end, end, paint);
				canvas.drawLine(end, end, end, 0, paint);
				canvas.drawLine(end, 0, 0, 0, paint);
				slices.add(bitmap);
			}
		}
		// remove original bitmap from memory
		original = null;
	}

	/**
	 * Serves slice and frees it from memory
	 * 
	 * @param index
	 *            index of the slice. Serves random slice if
	 *            TileSlicer.RANDOM_SLICE.
	 * @return TileView with the image or empty tile if there are no such
	 *         slices.
	 */
	public TileView getSlice(int index) {
		TileView tile = null;
		if (slices.size() > 0) {
			if (index == RANDOM_SLICE) {
				index = random.nextInt(slices.size());
			}
			tile = new TileView(context, index);
			tile.setImageBitmap(slices.remove(index));
		} else {
			// empty slice
			tile = new TileView(context, index);
			tile.setEmpty(true);
		}
		return tile;
	}

}
