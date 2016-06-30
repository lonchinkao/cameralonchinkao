package org.opencv.lonchinkao;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;


/**
 * This class is a controller for puzzle game.
 * It converts the image from Camera into the shuffled image
 */
public class Cam2Processor {

    private static final int GRID_SIZE = 2;
    private static final int GRID_2SIZE = 4;
    private static final int GRID_AREA = GRID_SIZE * GRID_2SIZE;
    private static final String TAG = "cam2Processor";

    private int[]   mIndexes;

    private Mat mRgba15;
    private Mat[] mCells15;


    public Cam2Processor() {

        mIndexes = new int [GRID_AREA];

        for (int i = 0; i < GRID_AREA; i++)
            mIndexes[i] = i;
    }

    /* this method is intended to make processor prepared for a new game */
    public synchronized void prepareNewGame() {
        do {
            shuffle(mIndexes);
        } while (!isPuzzleSolvable());
    }

    /* This method is to make the processor know the size of the frames that
     * will be delivered via puzzleFrame.
     * If the frames will be different size - then the result is unpredictable
     */
    public synchronized void prepareGameSize(int width, int height) {
        mRgba15 = new Mat(height, width, CvType.CV_8UC4);
        mCells15 = new Mat[GRID_AREA * 2];

        for (int i = 0; i < (GRID_SIZE * 2); i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int k = i * GRID_SIZE + j;
                mCells15[k] = mRgba15.submat(i * height / GRID_SIZE, (i + 1) * height / GRID_SIZE, j * width / GRID_SIZE, (j + 1) * width / GRID_SIZE);
            }
        }

    }

    /* this method to be called from the outside. it processes the frame and shuffles
     * the tiles as specified by mIndexes array
     */
    public synchronized Mat puzzleFrame(Mat inputPicture) {
        Mat[] cells = new Mat[GRID_AREA];
        int rows = inputPicture.rows();
        int cols = inputPicture.cols();

        rows = rows - rows%4;
        cols = cols - cols%4;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int k = i * GRID_SIZE + j;
                cells[k] = inputPicture.submat(i * inputPicture.rows() / GRID_SIZE, (i + 1) * inputPicture.rows() / GRID_SIZE, j * inputPicture.cols()/ GRID_SIZE, (j + 1) * inputPicture.cols() / GRID_SIZE);
            }
        }

        rows = rows - rows%4;
        cols = cols - cols%4;

        // copy shuffled tiles
        for (int i = 0; i < GRID_AREA * 2; i++) {
            int idx = mIndexes[i];
            cells[idx].copyTo(mCells15[i]);

        }

        for (int i = 0; i < GRID_AREA; i++)
            cells[i].release();

        drawGrid(cols, rows, mRgba15);

        return mRgba15;
    }


    public void deliverTouchEvent(int x, int y) {
        int rows = mRgba15.rows();
        int cols = mRgba15.cols();

        int row = (int) Math.floor(y * GRID_SIZE / rows);
        int col = (int) Math.floor(x * GRID_SIZE / cols);

        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            Log.e(TAG, "It is not expected to get touch event outside of picture");
            return ;
        }

        int idx = row * GRID_SIZE + col;
        int idxtoswap = -1;

        // left
        if (idxtoswap < 0 && col > 0)

                idxtoswap = idx - 1;
        // right
        if (idxtoswap < 0 && col < GRID_SIZE - 1)

                idxtoswap = idx + 1;
        // top
        if (idxtoswap < 0 && row > 0)

                idxtoswap = idx - GRID_SIZE;
        // bottom
        if (idxtoswap < 0 && row < GRID_SIZE - 1)

                idxtoswap = idx + GRID_SIZE;

        // swap
        if (idxtoswap >= 0) {
            synchronized (this) {
                int touched = mIndexes[idx];
                mIndexes[idx] = mIndexes[idxtoswap];
                mIndexes[idxtoswap] = touched;
            }
        }
    }

    private void drawGrid(int cols, int rows, Mat drawMat) {
     }

    private static void shuffle(int[] array) {

    }

    private boolean isPuzzleSolvable() {

        int sum = 0;
        for (int i = 0; i < GRID_AREA; i++) {

                int smaller = 0;
                for (int j = i + 1; j < GRID_AREA; j++) {
                    if (mIndexes[j] < mIndexes[i])
                        smaller++;
                }
                sum += smaller;
            }

        return sum % 2 == 0;
    }
}
