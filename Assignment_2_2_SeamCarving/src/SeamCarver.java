import java.awt.Color;

public class SeamCarver {
    private static final double EDGE_ENERGY = 195075.0;

    private Picture mCurrent;
    // private int mCurrentWidth;
    // private int mCurrentHeight;

    private Color colorUp;
    private Color colorDown;
    private Color colorLeft;
    private Color colorRight;

    private boolean[][] energyMapDirty;
    private double[][] energyMap;

    private EnergyPoint[][] energyHeap;

    public SeamCarver(Picture picture) {
        int w = picture.width();
        int h = picture.height();

        mCurrent = new Picture(picture);

        energyMap = new double[w][h];
        energyMapDirty = new boolean[w][h];

        energyHeap = new EnergyPoint[w][h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                energyMapDirty[x][y] = true;
                energyHeap[x][y] = new EnergyPoint(x, y, energy(x, y), null);
            }
        }
    }

    private void buildVerticalEnergyHeap() {
        for (int y = height() - 1; y >= 0; y--) {
            for (int x = width() - 1; x >= 0; x--) {
                // it's energy = the smallest one out of 3 under lying points +
                // its own energy
                EnergyPoint cur, lowest;
                cur = energyHeap[x][y];

                if (y >= height() - 1) {
                    cur.setEnergy(energy(x, y));
                    cur.setPrev(null);
                    continue;
                }

                lowest = energyHeap[x][y + 1];

                if ((x - 1) >= 0) {
                    // StdOut.print(energyHeap[x-1][y+1].energy());
                    if (energyHeap[x - 1][y + 1].energy() < lowest.energy()) {
                        lowest = energyHeap[x - 1][y + 1];
                    }
                }

                // StdOut.print(" --- " + energyHeap[x][y+1].energy());

                if ((x + 1) <= (width() - 1)) {
                    // StdOut.print(" --- " + energyHeap[x+1][y+1].energy());
                    if (energyHeap[x + 1][y + 1].energy() < lowest.energy()) {
                        lowest = energyHeap[x + 1][y + 1];
                    }
                }
                // StdOut.println("lowest : " + lowest.energy());

                cur.setEnergy(energy(x, y) + lowest.energy());
                cur.setPrev(lowest);
            }
        }
    }

    private void buildHorizontalEnergyHeap() {
        for (int x = width() - 1; x >= 0; x--) {
            for (int y = height() - 1; y >= 0; y--) {
                // it's energy = the smallest one out of 3 right points +
                // its own energy
                EnergyPoint cur, lowest;
                cur = energyHeap[x][y];

                if (x >= width() - 1) {
                    cur.setEnergy(energy(x, y));
                    cur.setPrev(null);
                    continue;
                }

                lowest = energyHeap[x + 1][y];

                if ((y - 1) >= 0) {
                    // StdOut.print(energyHeap[x+1][y-1].energy());
                    if (energyHeap[x + 1][y - 1].energy() < lowest.energy()) {
                        lowest = energyHeap[x + 1][y - 1];
                    }
                }

                // StdOut.print(" --- " + energyHeap[x+1][y].energy());

                if ((y + 1) <= (height() - 1)) {
                    // StdOut.print(" --- " + energyHeap[x+1][y+1].energy());
                    if (energyHeap[x + 1][y + 1].energy() < lowest.energy()) {
                        lowest = energyHeap[x + 1][y + 1];
                    }
                }
                // StdOut.println("        lowest : " + lowest.energy());

                cur.setEnergy(energy(x, y) + lowest.energy());
                cur.setPrev(lowest);
            }
        }

    }

    // current picture
    public Picture picture() {
        return mCurrent;
    }

    // width of current picture
    public int width() {
        return mCurrent.width();
    }

    // height of current picture
    public int height() {
        return mCurrent.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x > (width() - 1) || y > (height() - 1))
            throw new IndexOutOfBoundsException();

        if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1)
            return EDGE_ENERGY;

        // if in energyMap, look up
        if (!energyMapDirty[x][y]) {
            return energyMap[x][y];
        }
        /*
         * x-1 x x+1 y-1(255,101,51) (255,101,153) (255,101,255) y (255,203,51)
         * (255,204,153) (255,205,255) y+1(255,255,51) (255,255,153)
         * (255,255,255)
         * 
         * Rx(x, y) = 255 − 255 = 0, Gx(x, y) = 205 − 203 = 2, Bx(x, y) = 255 −
         * 51 = 204, yielding Δx^2(1, 2) = 2^2 + 204^2 = 41620.
         * 
         * Ry(x, y) = 255 − 255 = 0, Gy(x, y) = 255 − 153 = 102, By(x, y) = 153
         * − 153 = 0, yielding Δy^2(x, y) = 102^2 = 10404.
         * 
         * Thus, the energy of pixel (1, 2) is 41620 + 10404 = 52024
         */
        colorUp = mCurrent.get(x, y - 1);
        colorDown = mCurrent.get(x, y + 1);
        colorLeft = mCurrent.get(x - 1, y);
        colorRight = mCurrent.get(x + 1, y);
        int rx = colorRight.getRed() - colorLeft.getRed();
        int gx = colorRight.getGreen() - colorLeft.getGreen();
        int bx = colorRight.getBlue() - colorLeft.getBlue();
        double deltaXSquared = rx * rx + gx * gx + bx * bx;

        int ry = colorDown.getRed() - colorUp.getRed();
        int gy = colorDown.getGreen() - colorUp.getGreen();
        int by = colorDown.getBlue() - colorUp.getBlue();
        double deltaYSquared = ry * ry + gy * gy + by * by;

        energyMap[x][y] = deltaXSquared + deltaYSquared;
        energyMapDirty[x][y] = false;

        return energyMap[x][y];
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int[] ret = new int[width()];
        int min = 0;
        double energy = Double.MAX_VALUE;

        buildHorizontalEnergyHeap();

        for (int i = 1; i < height(); i++) {
            if (energyHeap[1][i].energy() < energy) {
                min = i;
                energy = energyHeap[1][i].energy();
                // StdOut.println("found smaller:" + min + ", energy = " +
                // energy);
            }
        }

        ret[0] = min;
        EnergyPoint trace = energyHeap[1][min];
        while (trace != null) {
            // StdOut.println("trace: (" + trace.x() + ", " + trace.y() + ")");
            ret[trace.x()] = trace.y();
            trace = trace.prev();
        }

        return ret;
        //
        // int[] ret = new int[width()];
        // int min = 0;
        // double energy = Double.MAX_VALUE;
        //
        // buildHorizontalEnergyHeap();
        //
        // for (int i = 1; i < height(); i++) {
        // if (energyHeap[1][i].energy() < energy) {
        // min = i;
        // energy = energyHeap[1][i].energy();
        // // StdOut.println("found smaller:" + min + ", energy = " +
        // // energy);
        // }
        // }
        //
        // // StdOut.println("horizontal min = :" + min + ", energy = " +
        // energy);
        //
        // ret[0] = min;
        // EnergyPoint trace = energyHeap[1][min];
        // while (trace != null) {
        // // StdOut.println("trace: (" + trace.x() + ", " + trace.y() + ")");
        // ret[trace.x()] = trace.y();
        // // mEnergy.set(trace.x(), trace.y(), Color.WHITE);
        // mCurrent.set(trace.x(), trace.y(), Color.RED);
        // trace = trace.prev();
        // }
        //
        // return ret;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int[] ret = new int[height()];
        int min = 0;
        double energy = Double.MAX_VALUE;

        buildVerticalEnergyHeap();

        for (int i = 1; i < width(); i++) {
            if (energyHeap[i][1].energy() < energy) {
                min = i;
                energy = energyHeap[i][1].energy();
                // StdOut.println("found smaller:" + min + ", energy = " +
                // energy);
            }
        }

        ret[0] = min;
        EnergyPoint trace = energyHeap[min][1];
        while (trace != null) {
            // StdOut.println("trace: (" + trace.x() + ", " + trace.y() + ")");
            ret[trace.y()] = trace.x();
            trace = trace.prev();
        }

        return ret;
    }

    // remove horizontal seam from picture
    public void removeHorizontalSeam(int[] a) {
        if (null == a || a.length != width())
            throw new RuntimeException();

        int[] seam = findHorizontalSeam();

        // copy pixel to new picture
        Picture newPic = new Picture(width(), height() - 1);

        for (int x = 0; x < newPic.width(); x++) {
            int removed = seam[x];
            for (int y = 0; y < removed; y++) {
                newPic.set(x, y, mCurrent.get(x, y));
            }
            for (int y = removed; y < newPic.height(); y++) {
                newPic.set(x, y, mCurrent.get(x, y + 1));
                energyMap[x][y] = energyMap[x][y + 1];
                energyMapDirty[x][y] = energyMapDirty[x][y + 1];
            }

            // energy map near the pixel is dirty
            energyMapDirty[x][removed] = true;
            if (removed - 1 >= 0)
                energyMapDirty[x][removed - 1] = true;
            if (x - 1 >= 0)
                energyMapDirty[x - 1][removed] = true;
            if (x + 1 <= width() - 1)
                energyMapDirty[x + 1][removed] = true;
        }

        for (int i = 0; i < width(); i++) {
            energyHeap[i][newPic.height()].setPrev(null);
        }

        mCurrent = newPic;
        //
        // // move the under lying pixel upwards
        // for (int x = 0; x < width(); x++) {
        // int y = seam[x];
        // for (; y < height() - 1; y++) {
        // mCurrent.set(x, y, mCurrent.get(x, y + 1));
        // energyMap[x][y] = energyMap[x][y + 1];
        // energyMapDirty[x][y] = energyMapDirty[x][y + 1];
        // }
        //
        // // energy map near the pixel is dirty
        // energyMapDirty[x][y] = true;
        // if (y - 1 >= 0)
        // energyMapDirty[x][y - 1] = true;
        // if (x - 1 >= 0)
        // energyMapDirty[x - 1][y] = true;
        // if (x + 1 <= width() - 1)
        // energyMapDirty[x + 1][y] = true;
        // }
        //
        // mCurrentHeight--;
        // for (int i = 0; i < width(); i++) {
        // mCurrent.set(i, mCurrentHeight, Color.WHITE);
        // energyHeap[i][mCurrentHeight].setPrev(null);
        // }
    }

    // remove vertical seam from picture
    public void removeVerticalSeam(int[] a) {
        if (null == a || a.length != height())
            throw new RuntimeException();

        int[] seam = findVerticalSeam();

        // copy pixel to new picture
        Picture newPic = new Picture(width() - 1, height());

        for (int y = 0; y < newPic.height(); y++) {
            int removed = seam[y];
            for (int x = 0; x < removed; x++) {
                newPic.set(x, y, mCurrent.get(x, y));
            }
            for (int x = removed; x < newPic.width(); x++) {
                newPic.set(x, y, mCurrent.get(x + 1, y));
                energyMap[x][y] = energyMap[x + 1][y];
                energyMapDirty[x][y] = energyMapDirty[x + 1][y];
            }

            // energy map near the pixel is dirty
            energyMapDirty[removed][y] = true;
            if (removed - 1 >= 0)
                energyMapDirty[removed - 1][y] = true;
            if (y - 1 >= 0)
                energyMapDirty[removed][y - 1] = true;
            if (y + 1 <= height() - 1)
                energyMapDirty[removed][y + 1] = true;
        }

        for (int i = 0; i < height(); i++) {
            energyHeap[newPic.width()][i].setPrev(null);
        }

        mCurrent = newPic;

        //
        // // move the right pixels left-wards, as well as energy map
        // for (int y = 0; y < height(); y++) {
        // int x = seam[y];
        // for (; x < width() - 1; x++) {
        // mCurrent.set(x, y, mCurrent.get(x + 1, y));
        // energyMap[x][y] = energyMap[x + 1][y];
        // energyMapDirty[x][y] = energyMapDirty[x + 1][y];
        // }
        //
        // // energy map near the pixel is dirty
        // energyMapDirty[x][y] = true;
        // if (x - 1 >= 0)
        // energyMapDirty[x - 1][y] = true;
        // if (y - 1 >= 0)
        // energyMapDirty[x][y - 1] = true;
        // if (y + 1 <= height() - 1)
        // energyMapDirty[x][y + 1] = true;
        // }
        // mCurrentWidth--;
        // for (int i = 0; i < height(); i++) {
        // mCurrent.set(mCurrentWidth, i, Color.WHITE);
        // energyHeap[mCurrentWidth][i].setPrev(null);
        // }
    }

    public static void main(String[] args) {
        SeamCarver carver = new SeamCarver(new Picture("HJoceanSmall.png"));

        for (int i = 0; i < 200; i++) {
            carver.removeVerticalSeam(carver.findVerticalSeam());
        }
        carver.mCurrent.show();

        for (int i = 0; i < 150; i++) {
            carver.removeHorizontalSeam(carver.findHorizontalSeam());
        }
        carver.mCurrent.show();
    }

    private class EnergyPoint implements Comparable<EnergyPoint> {
        private int mX;
        private int mY;
        private double mEnergy;

        private EnergyPoint mPrev;

        EnergyPoint(int x, int y, double energy, EnergyPoint prev) {
            mX = x;
            mY = y;
            mEnergy = energy;
            mPrev = prev;
        }

        int x() {
            return mX;
        }

        int y() {
            return mY;
        }

        double energy() {
            return mEnergy;
        }

        void setEnergy(double energy) {
            mEnergy = energy;
        }

        EnergyPoint prev() {
            return mPrev;
        }

        void setPrev(EnergyPoint prev) {
            mPrev = prev;
        }

        @Override
        public int compareTo(EnergyPoint p) {
            if (mEnergy < p.energy())
                return -1;
            else if (mEnergy > p.energy())
                return 1;
            return 0;
        }
    }

}
