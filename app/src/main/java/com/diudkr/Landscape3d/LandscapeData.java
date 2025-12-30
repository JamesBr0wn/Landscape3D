package com.diudkr.Landscape3d;

import android.os.SystemClock;
import android.util.Log;

import java.util.Vector;

public class LandscapeData {

    private static LandscapeData theLandscape;
    private double eye_x;
    private double eye_y;
    private double eye_z;
    MinMax projectionMinMax = new MinMax();
    // private WaterState[][] water; // 0=normal point, 1=riverstart, 2=riverflow 3=lake 4=ocean
    private boolean hasWater;
    private boolean hasOcean;
    private boolean hasThread;

    private static int MAXLEG = 201;
    @SuppressWarnings("FieldCanBeLocal")
    private static int MAX_DURATION = 8000;

    public static final int MAX_DETAIL = 8;
    public static final int MIN_DETAIL = 0;
    public static final int MAX_ARRAYDIM = pow2(MAX_DETAIL); // e.g. 256 = 2**8

    ThreadGroup riverthreadgroup = new ThreadGroup("RiverThreadGroup");

    protected PointInfo[][] landscape;

    public double getProjectionX(int x, int y) {
        return landscape[x][y].projectionX;
    }

    public double getProjectionY(int x, int y) {
        return landscape[x][y].projectionY;
    }

    public MinMax getProjectionMinMax() {
        return projectionMinMax;
    }

    public class PointInfo {
        protected double height;
        protected double projectionX;
        protected double projectionY;
        protected byte state; // 0=normal point, 1=river start, 2=followup river, 3=lake
        protected River river = null; // != null, if state == 1
        LakePoint lp = null;
    }

    class MinMax {
        private double minx, maxx, miny, maxy;
        public MinMax() {
            reset();
        }
        void reset() {
            minx =  1000000f;
            maxx = -1000000f;
            miny =  1000000f;
            maxy = -1000000f;
        }
        private void insertX(double x) {
            if (x < minx) minx = x;
            if (x > maxx) maxx = x;
        }
        private void insertY(double y) {
            if (y < miny) miny = y;
            if (y > maxy) maxy = y;
        }
        public double getMinX() {
            return minx;
        }
        public double getMaxX() {
            return maxx;
        }
        public double getMinY() {
            return miny;
        }
        public double getMaxY() {
            return maxy;
        }
    }

    public class Point2d_int {
        public int x,y;
        public Point2d_int() {
            x = y = 0;
        }
        public Point2d_int(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public String toString() {
            return "Point X = " + x + " Y = " + y;
        }
    }

    public class River extends Point2d_int {
        public River next = null;
        void addNext(Point2d_int n) {
            next = new River();
            next.x = n.x;
            next.y = n.y;
            next.next = null;
        }

        @Override
        public String toString() {
            return "River X = " + x + " Y = " + y + " next = " + next;
        }
    }

    private class LakePoint {
        boolean isMiddleOfLake;
        LakePoint() {
            super();
            isMiddleOfLake = false;
        }
    }

    public PointInfo getPointInfo(int x, int y) {
        return landscape[x][y];
    }

    private class RiverFlow {
        long starttime;
        long curtime;
        public RiverFlow() {
            super();
            starttime = SystemClock.elapsedRealtime();
        }
        public boolean isOvertime() {
            curtime = SystemClock.elapsedRealtime();
            boolean b = ( (curtime - starttime) > MAX_DURATION);
            if (b) {
                Log.i("diudkr", "Overtime!" + this);
            }
            return b;
        }

        @Override
        public String toString() {
            return "RiverFlow: starttime = " + starttime + " current = " + curtime + " delta = " + (curtime - starttime);
        }
    }

    private class FillState {
        double lakeheight;
        double minNextPointHeight;
        boolean hasExit;
        Point2d_int newp;
        Vector<Point2d_int> exitp;
        Point2d_int realexitp;
        int minx, miny, maxx, maxy;

        FillState() {
            super();
            for (int x=0; x<=MAX_ARRAYDIM; x++) {
                for (int y=0; y<=MAX_ARRAYDIM; y++) {
                    landscape[x][y].lp = null;
                }
            }
            exitp = new Vector<>(10);
        }

        void setBoundsByStartpoint(Point2d_int p) {
            minx = maxx = p.x;
            miny = maxy = p.y;
        }

        void addLakepoint(Point2d_int p) {
            if (null == landscape[p.x][p.y].lp) {
                landscape[p.x][p.y].lp = new LakePoint();
            }
            if (landscape[p.x][p.y].river != null) {
                landscape[p.x][p.y].river = null;
            }
            if (p.x < minx) minx = p.x; // new bounds
            if (p.x > maxx) maxx = p.x;
            if (p.y < miny) miny = p.y;
            if (p.y > maxy) maxy = p.y;
        }

        void addExitPoint(Point2d_int ep) {
            exitp.add(ep);
        }

        @Override
        public String toString() {
            return "FillState: lakeheight = " + lakeheight +
                    " minNextPointHeight = " + minNextPointHeight +
                    " minx = " + minx +
                    " maxx = " + maxx +
                    " miny = " + miny +
                    " maxy = " + maxy +
                    " newp = " + newp +
                    " realexitp = " + realexitp;
        }
    }

    /*
    public double getEye_x() {
        return eye_x;
    }

    public double getEye_y() {
        return eye_y;
    }

    public double getEye_z() {
        return eye_z;
    }
    */

    public static LandscapeData getLandscape() {
        if (theLandscape == null) {
            theLandscape = new LandscapeData();
            Log.i("diudkr", "Landscape Singleton created");
        }
        return theLandscape;
    }

    public static int pow2(int n) {
        int i = 1;
        for (int j=1; j<=n; j++) {
            i = i*2;
        }
        return i;
    }

    public double getLandscapePoint(int x, int y) {
        return landscape[x][y].height;
    }

    public double getLandscapePoint(Point2d_int p) {
        return landscape[p.x][p.y].height;
    }

    public boolean hasWater() {
        return this.hasWater;
    }

    public boolean hasOcean() {
        return this.hasOcean;
    }

    double getScreenX(int ax, int ay) {
        double fax, fay;
        double fres;

        fax = ax;
        fay = ay;
        fres = ((eye_x * fay) + (fax * eye_y)) / (fay + eye_y);

        return fres;
    }

    double getScreenY(double az, int ay) {
        double faz, fay;
        double fres;

        faz = az;
        fay = ay;
        fres = ((eye_z * fay) + (faz * eye_y)) / (fay + eye_y);

        return fres;
    }

    protected void calculateMinMax(boolean adjustMinMax) {
        double sx,sy;
        int ix, iy;
        double iz;
        LandscapeData ld = LandscapeData.getLandscape();
        for (int x=0; x<=LandscapeData.MAX_ARRAYDIM; x++) {
            for (int y=0; y<=LandscapeData.MAX_ARRAYDIM; y++) {
                // Inbound mapping
                ix = x;
                iy = y;
                iz = ld.getLandscapePoint(x,y);
                sx = getScreenX(ix, iy);
                sy = getScreenY(iz, iy);
                landscape[ix][iy].projectionX = sx;
                landscape[ix][iy].projectionY = sy;
                if (adjustMinMax) {
                    projectionMinMax.insertX(sx);
                    projectionMinMax.insertY(sy);
                }
            }
        }
    }

    public void calculateLandscape() {
        initLandscape();
        for (int d=0; d<MAX_DETAIL; d++) {
            calculateDetail(d);
        }
        calculateMinMax(true);
    }

    private boolean pointIsOnLand(int x, int y) {
        return !((x < 0) || (y < 0) || (x > MAX_ARRAYDIM) || (y > MAX_ARRAYDIM));
    }

    private boolean pointIsOnBorder(Point2d_int p) {
        return (p.x == 0) || (p.y == 0) || (p.x == MAX_ARRAYDIM) || (p.y == MAX_ARRAYDIM);
    }

    private boolean pointIsOnBorder(int x, int y) {
        return (x == 0) || (y == 0) || (x == MAX_ARRAYDIM) || (y == MAX_ARRAYDIM);
    }

    private boolean isNewPoint(Point2d_int p, int dx, int dy) {
        LakePoint testp = landscape[p.x+dx][p.y+dy].lp;
        return (testp == null);
    }

    private void checkPoint(FillState fs, Point2d_int p, int dx, int dy) {
        int cx, cy;
        cx = p.x + dx;
        cy = p.y + dy;
        if (!pointIsOnLand(cx, cy)) {
            // System.out.println("Point outside!! x = " + cx + " y = " + cy);
            return;
        }
        if (isNewPoint(p, dx, dy)) {
            // System.out.println("test new point " + np);
            if (fs.lakeheight > landscape[cx][cy].height) {
                // exit criteria reached!!
                Point2d_int ep = new Point2d_int(cx, cy);
                fs.addExitPoint(ep);
                // System.out.println("Exitpoint = " + ep + " height = " + landscape[cx][cy]);
            }
            else {
                if (landscape[cx][cy].height < fs.minNextPointHeight ) {
                    fs.minNextPointHeight = landscape[cx][cy].height;
                    fs.newp = new Point2d_int(cx, cy);
                }
            }
        }
    }

    private int isLakePointInProgress(Point2d_int p, int dx, int dy) {
        if (!pointIsOnLand(p.x+dx, p.y+dy)) {
            return 0;
        }
        LakePoint lp = landscape[p.x+dx][p.y+dy].lp;
        if (lp != null) {
            return 1;
        }
        return 0;
    }

    private void floodLakePoints(FillState fs) {
        Point2d_int p = new Point2d_int();
        for (int x=fs.minx; x<=fs.maxx; x++) {
            p.x = x;
            for (int y = fs.miny; y <= fs.maxy; y++) {
                p.y = y;
                LakePoint lp = landscape[x][y].lp;
                if (null != lp) { // check adjacent points
                    if (!lp.isMiddleOfLake) {
                        checkPoint(fs, p, 1, 1);
                        checkPoint(fs, p, 1, 0);
                        checkPoint(fs, p, 1, -1);
                        checkPoint(fs, p, 0, -1);
                        checkPoint(fs, p, -1, -1);
                        checkPoint(fs, p, -1, 0);
                        checkPoint(fs, p, -1, 1);
                        checkPoint(fs, p, 0, 1);
                    }
                }
            }
        }
        // choose the "best" exitpoint
        if (fs.exitp.size() == 1) { // several
            fs.realexitp = fs.exitp.firstElement();
        }
        else {
            // System.out.println("floodLakePoints: multiple exitpoint!!");
            double erg = 0f;
            for (int i=0; i<fs.exitp.size(); i++) {
                Point2d_int pp = fs.exitp.elementAt(i);
                if (i==0) { // startval
                    erg = landscape[pp.x][pp.y].height;
                    fs.realexitp = pp;
                }
                if (erg > landscape[pp.x][pp.y].height) {
                    erg = landscape[pp.x][pp.y].height;
                    fs.realexitp = pp;
                }
            }
        }
        // mark points in the middle of a lake as irrelevant, mark points in the lake as such
        int neigbours;
        for (int x=fs.minx; x<=fs.maxx; x++) {
            p.x = x;
            for (int y = fs.miny; y <= fs.maxy; y++) {
                p.y = y;
                LakePoint lp = landscape[x][y].lp;
                if (null != lp) { // check adjacent points
                    landscape[x][y].height = fs.minNextPointHeight;
                    landscape[x][y].state = 3;
                    neigbours = 0;
                    neigbours += isLakePointInProgress(p, 1, 1);
                    neigbours += isLakePointInProgress(p, 1, 0);
                    neigbours += isLakePointInProgress(p, 1, -1);
                    neigbours += isLakePointInProgress(p, 0, -1);
                    neigbours += isLakePointInProgress(p, -1, -1);
                    neigbours += isLakePointInProgress(p, -1, 0);
                    neigbours += isLakePointInProgress(p, -1, 1);
                    neigbours += isLakePointInProgress(p, 0, 1);
                    if (neigbours == 8) {
                        lp.isMiddleOfLake = true;
                        // System.out.println("Mark Point in Lake x = " + x + " y = " + y);
                    }
                }
            }
        }
    }

    private boolean isOldLake(Point2d_int p) {
        return (landscape[p.x][p.y].state == 3) && (isLakePointInProgress(p, 0, 0) == 0);
    }

    private void acquireOldLakepoint(FillState fs, Point2d_int p) {
        if (!pointIsOnLand(p.x, p.y)) return;
        if (!isOldLake(p)) return;
        landscape[p.x][p.y].height = fs.minNextPointHeight;
        fs.addLakepoint(p);
        try {
            Point2d_int tp = new Point2d_int();
            tp.x = p.x;
            tp.y = p.y - 1;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x + 1;
            tp.y = p.y - 1;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x + 1;
            tp.y = p.y;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x + 1;
            tp.y = p.y + 1;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x;
            tp.y = p.y + 1;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x - 1;
            tp.y = p.y + 1;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x - 1;
            tp.y = p.y;
            acquireOldLakepoint(fs, tp);
            tp.x = p.x - 1;
            tp.y = p.y - 1;
            acquireOldLakepoint(fs, tp);
        }
        catch (StackOverflowError e) {
            // System.out.println("caught e = " + e);
        }
    }

    private void acquireOldLake(FillState fs) {
        acquireOldLakepoint(fs, fs.newp);
    }

    private void fillLake(Point2d_int startpoint, int leg, RiverFlow rf) {
        FillState fs = new FillState();
        fs.lakeheight = landscape[startpoint.x][startpoint.y].height;
        fs.addLakepoint(startpoint);
        fs.setBoundsByStartpoint(startpoint);
        fs.hasExit = false;
        landscape[startpoint.x][startpoint.y].state = 3;
        boolean onborder = false;
        // System.out.println("----LandscapeData.fillLake " + startpoint + " Leg = " + leg);
        int counter = 0;
        while (!fs.hasExit && !onborder && (counter < 20000000) && (!rf.isOvertime() )) {
            fs.minNextPointHeight = 1000000f; // higher than all possible points...
            counter++;
            fs.newp = null;
            fs.realexitp = null;
            fs.exitp.clear();
            floodLakePoints(fs);
            if ((fs.realexitp == null) && (fs.newp) == null) { // one of both must be found...
                // System.out.println("++++All wrong!!!!!!!");
                getLandscapePoint(40000,40000); // will dump...
            }
            if (fs.realexitp == null) {
                // System.out.println("no exit found yet");
                if (fs.newp != null) {
                    if (isOldLake(fs.newp)) {
                        // Log.v("diudkr", "acquire...");
                        acquireOldLake(fs);
                    }
                    fs.addLakepoint(fs.newp);
                    landscape[fs.newp.x][fs.newp.y].height = fs.minNextPointHeight;
                    landscape[fs.newp.x][fs.newp.y].state = 3;
                    // System.out.println("added " + fs.newp);
                    if (pointIsOnBorder(fs.newp)) {
                        // System.out.println("on border " + fs.newp);
                        onborder = true;
                    }
                }
                fs.lakeheight = fs.minNextPointHeight;
            }
            else {
                fs.hasExit = true;
                // System.out.println("Exit found at " + fs.realexitp);
                if (leg < MAXLEG) {
                    // System.out.println("Erg vor startRiver: exitp " + fs.realexitp + " counter = " + counter + " fs.hasExit " + fs.hasExit);
                    if (!rf.isOvertime()) {
                        startRiver(fs.realexitp.x, fs.realexitp.y, leg + 1, rf);
                    }
                }
            }
        }
    }

    private class RiverCreator implements Runnable {
        int swx;
        int swy;
        int l;
        RiverFlow rif;

        RiverCreator(int sx, int sy, int leg, RiverFlow rf) {
            super();
            swx = sx;
            swy = sy;
            l = leg;
            rif = rf;
        }

        @Override
        public void run() {
            //noinspection EmptyCatchBlock
            try {
                startRiver(swx, swy, l, rif);
            }
            catch (Exception ignored) {
            }
        }
    }

    private void startRiverByThreading(int swx, int swy, int leg, RiverFlow rf) {
        if (hasThread) {
            Log.e("diudkr", "startRiverByThreading while thread!!");
        }
        if (!pointIsOnLand(swx, swy)) {
            // System.out.println("River started outside!!!");
            return;
        }
        if (pointIsOnBorder(swx, swy)) {
            // System.out.println("River started on border!!!");
            return;
        }
        if (landscape[swx][swy].state > 0) {
            // System.out.println("River started on water!!!");
            return;
        }

        RiverCreator rc = new RiverCreator(swx, swy, leg, rf);
        //noinspection EmptyCatchBlock
        try {
            Thread t = new Thread(riverthreadgroup, rc, "river", 10000000);
            t.setPriority(Thread.MAX_PRIORITY);
            hasThread = true;
            t.start();
            // locker.wait(); // wait until called thread has created the river + lakes
            t.join();
            hasThread = false;
        }
        catch (Exception ignored) {
        }
    }

    private void startRiver(int swx, int swy, int leg, RiverFlow rf) {
        if (!pointIsOnLand(swx, swy)) {
            // System.out.println("River started outside!!!");
            return;
        }
        if (pointIsOnBorder(swx, swy)) {
            // System.out.println("River started on border!!!");
            return;
        }
        if (landscape[swx][swy].state > 0) {
            // System.out.println("River started on water!!!");
            return;
        }
        boolean cont = true;
        Point2d_int cur = new Point2d_int();
        Point2d_int cand = new Point2d_int();
        Point2d_int to = new Point2d_int();
        double toh;
        cur.x = swx;
        cur.y = swy;
        landscape[cur.x][cur.y].state = 1;
        River theRiver = new River();
        // River theRiverStart = theRiver;
        // System.out.println("----LandscapeData.startRiver at " + cur + " Leg = " + leg);
        landscape[cur.x][cur.y].river = theRiver;
        landscape[cur.x][cur.y].river.x = cur.x; // start of river
        landscape[cur.x][cur.y].river.y = cur.y;
        toh = getLandscapePoint(cur);
        while (cont && (!rf.isOvertime()) ) { // float downwards until a) border reached, or b) no downwards c) timeout
            boolean found = false;
            cand.x = cur.x; cand.y = cur.y+1;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x+1; cand.y = cur.y+1;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x+1; cand.y = cur.y;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x+1; cand.y = cur.y-1;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x; cand.y = cur.y-1;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x-1; cand.y = cur.y-1;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x-1; cand.y = cur.y;
            if (getLandscapePoint(cand) < toh) {
                toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            cand.x = cur.x-1; cand.y = cur.y+1;
            if (getLandscapePoint(cand) < toh) {
                // toh = getLandscapePoint(cand);
                to.x = cand.x; to.y = cand.y;
                found = true;
            }
            if (found) {
                // System.out.println("floated to " + to);
                if (landscape[to.x][to.y].state > 0) { // other river or lake -> stop
                    // System.out.println("reached water!!! " + water[to.x][to.y].state);
                    cont = false;
                }
                cur.x = to.x;
                cur.y = to.y;
                if (landscape[cur.x][cur.y].state != 1) { // don't overwrite a former river start
                    landscape[cur.x][cur.y].state = 2;
                }
                theRiver.addNext(to);
                theRiver = theRiver.next;
                if (pointIsOnBorder(cur)) {
                    // System.out.println("reached border!!");
                    cont = false;
                }
            }
            else { // we are in a hole
                // System.out.println("reached hole!!");
                if (leg < MAXLEG) {
                    if (!rf.isOvertime()) {
                        fillLake(cur, leg + 1, rf);
                    }
                }
                return;
            }
            // System.out.println("New River = " + theRiverStart);
        }
    }

    public void createRiver(int swx, int swy) {
        // System.out.println("LandscapeData.createRiver swx = " + swx + " swy = " + swy);
        RiverFlow rf = new RiverFlow();
        startRiverByThreading(swx, swy, 0, rf);
    }

    public void createRiver() {
        int swx, swy;
        swx = (int) (Math.random() * (MAX_ARRAYDIM -8)) + 4;
        swy = (int) (Math.random() * (MAX_ARRAYDIM -8)) + 4;
        createRiver(swx, swy);
        hasWater = true;
        calculateMinMax(false);
    }

    public void floodOcean() {
        // double sum = 0.0d;
        // int c = 0;
        if (hasThread) {
            Log.e("diudkr", "floodOcean while thread!!");
        }
        double min=0f, max=0f;
        hasOcean = true;
        for (int x=0; x<=MAX_ARRAYDIM; x++) {
            for (int y=0; y<=MAX_ARRAYDIM; y++) {
                if ( x==0 && y==0 ) {
                    min = max = landscape[x][y].height;
                }
                if (landscape[x][y].height > max) max = landscape[x][y].height;
                if (min > landscape[x][y].height) min = landscape[x][y].height;
                // sum += landscape[x][y];
                // c++;
            }
        }
        double tmax = max-min;
        double surface = (tmax / 20) + min;
        // System.out.println("floodOcean average = " + sum/c + " min = " + min + " max = " + max);
        // System.out.println("floodOcean height = " + tmax + " surface = " + surface);
        hasWater = true;
        for (int x=0; x<=MAX_ARRAYDIM; x++) {
            for (int y=0; y<=MAX_ARRAYDIM; y++) {
                if ( landscape[x][y].height < surface ) {
                    landscape[x][y].height = surface;
                    landscape[x][y].state = 4;
                }
            }
        }
        calculateMinMax(false);
    }

    protected void calculateDetail(int det) {
        int delta = MAX_ARRAYDIM / pow2(det); // cell length
        int middle = MAX_ARRAYDIM / pow2(det+1); // half of the cell length

        double surroundingz;
        double rz;
        // y = const lines
        for (int y=0; y<=MAX_ARRAYDIM; y +=delta) {
            for (int x=0; x<MAX_ARRAYDIM; x += delta) {
                surroundingz = landscape[x][y].height + landscape[x+delta][y].height;
                rz = Math.random() * delta - middle; // proportional
                landscape[x+middle][y].height = surroundingz/2.0f + rz;
            }
        }

        // x = const lines
        for (int x=0; x<=MAX_ARRAYDIM; x +=delta) {
            for (int y=0; y<MAX_ARRAYDIM; y += delta) {
                surroundingz = landscape[x][y].height + landscape[x][y+delta].height;
                rz = Math.random() * delta - middle; // proportional
                landscape[x][y+middle].height = surroundingz/2.0f + rz;
            }
        }

        // points in middle
        for (int x=0; x<MAX_ARRAYDIM; x +=delta) {
            for (int y=0; y<MAX_ARRAYDIM; y += delta) {
                surroundingz = landscape[x][y].height + landscape[x][y+delta].height + landscape[x+delta][y].height + landscape[x+delta][y+delta].height;
                rz = Math.random() * delta - middle; // proportional
                landscape[x+middle][y+middle].height = surroundingz/4.0f + rz;
            }
        }
    }

    protected void initLandscape() {
        for (int i=0; i<=MAX_ARRAYDIM; i++) {
            for (int j=0;j<=MAX_ARRAYDIM; j++) {
                landscape[i][j] = new PointInfo();
            }
        }
        landscape[0][MAX_ARRAYDIM].height = (double) (MAX_ARRAYDIM / 2); // start value
        hasWater = false;
        hasOcean = false;
        hasThread = false;
        projectionMinMax.reset();
    }

    protected LandscapeData () {
        landscape = new PointInfo[MAX_ARRAYDIM+1][MAX_ARRAYDIM+1];
        eye_x = 340.0f;
        eye_y = 440.0f;
        eye_z = 1000f;
        initLandscape();
    }

}