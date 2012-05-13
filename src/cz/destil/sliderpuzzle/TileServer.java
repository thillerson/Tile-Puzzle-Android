package cz.destil.sliderpuzzle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import android.graphics.Bitmap;

public class TileServer {
	
	Bitmap original, scaledImage;
	int rows, columns, width, tileSize;
	HashSet<Bitmap> slices;
	ArrayList<Bitmap> unservedSlices;
	Random random;
	
	public TileServer(Bitmap original, int rows, int columns, int tileSize) {
		super();
		this.original = original;
		this.rows = rows;
		this.columns = columns;
		this.tileSize = tileSize;
		
		random = new Random();
		slices = new HashSet<Bitmap>();
		sliceOriginal();
	}

	protected void sliceOriginal() {
		int fullWidth = tileSize * rows;
		int fullHeight = tileSize * columns;
		scaledImage = Bitmap.createScaledBitmap(original, fullWidth, fullHeight, true);
		
		int x, y;
		Bitmap bitmap;
		for (int rowI=0; rowI<4; rowI++) {
			for (int colI=0; colI<4; colI++) {
				x = rowI * tileSize;
				y = colI * tileSize;
				bitmap = Bitmap.createBitmap(scaledImage, x, y, tileSize, tileSize);
				slices.add(bitmap);
			}
		}
		unservedSlices = new ArrayList<Bitmap>();
		unservedSlices.addAll(slices);
	}
	
	public Bitmap serveRandomSlice() {
		if (unservedSlices.size() > 0) {
			int randomIndex = random.nextInt(unservedSlices.size());
			Bitmap drawable = unservedSlices.remove(randomIndex);
			return drawable;
		}
		return null;
	}

}
