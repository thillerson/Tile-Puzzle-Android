package cz.destil.sliderpuzzle;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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

	@SuppressWarnings("unused")
	private static final String TAG = "TileSlicer";
	private Bitmap original;
	private int tileSize, gridSize;
	private ArrayList<Bitmap> slices;
	private Random random;

	/**
	 * Initializes TileSlicer.
	 * 
	 * @param original
	 *            Bitmap which should be sliced
	 * @param gridSize
	 *            Grid size, for example 4 for 4x4 grid
	 */
	public TileSlicer(Bitmap original, int gridSize) {
		super();
		this.original = original;
		this.gridSize = gridSize;
		this.tileSize = original.getWidth() / gridSize;
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
		original.recycle();
		original = null;
	}

	/**
	 * Serves random slice and frees it from memory
	 * 
	 * @return Bitmap of random slice
	 */
	public Bitmap getRandomSlice() {
		if (slices.size() > 0) {
			int randomIndex = random.nextInt(slices.size());
			Bitmap drawable = slices.remove(randomIndex);
			return drawable;
		}
		return null;
	}

}
