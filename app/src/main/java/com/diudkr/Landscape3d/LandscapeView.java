package com.diudkr.Landscape3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.diudkr.Landscape3D.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LandscapeView  extends View {

    class PathAndPaintHolder {
        Path thePath = null;
        Paint thePaint;
    }

    PathAndPaintHolder areaRect = new PathAndPaintHolder(); // the drawing area
    PathAndPaintHolder whitelines = new PathAndPaintHolder(); // white wireframe
    PathAndPaintHolder riverlines = new PathAndPaintHolder(); // blue rivers
    PathAndPaintHolder oceanwater = new PathAndPaintHolder(); // blue flood / ocean
    PathAndPaintHolder lakewater = new PathAndPaintHolder(); // blue lakes
    PathAndPaintHolder[] shadedareas = new PathAndPaintHolder[4];

    class Point3D {
        double x,y,z;

        public Point3D() {
            x = y = z = 0.0d;
        }
        public Point3D(double ax, double ay, double az) {
            x = ax;
            y = ay;
            z = az;
        }

        @Override
        public String toString() {
            return "Point3D{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

    Point3D kreuzProd(Point3D p1, Point3D p2) {
        double nx = p1.y*p2.z - p1.z*p2.y;
        double ny = p1.z*p2.x - p1.x*p2.z;
        double nz = p1.x*p2.y - p1.y*p2.x;
        // normalize so that z points up
        if (0.0d > nz) {
            nx *= -1.0d;
            ny *= -1.0d;
            nz *= -1.0d;
        }
        return new Point3D(nx, ny, nz);
    }

    public LandscapeView(Context context) {
        super(context);
        doInit();
    }

    public LandscapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        doInit();
    }

    public LandscapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        doInit();
    }

    public LandscapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        doInit();
    }

    public float transformToOutputX(int x, int y, int screenx) {
        // the screenx-2 means, that the last point is also visible in x direction
        float minx = (float) LandscapeData.getLandscape().getProjectionMinMax().getMinX();
        float maxx = (float) LandscapeData.getLandscape().getProjectionMinMax().getMaxX();
        float modelx = (float) LandscapeData.getLandscape().getProjectionX(x,y);
        return ( modelx - minx) * (screenx-2) / (maxx - minx);
    }

    public float transformToOutputY(int x, int y, int screeny) {
        // the screeny-2 means, that the last point is also visible in y direction
        float miny = (float) LandscapeData.getLandscape().getProjectionMinMax().getMinY();
        float maxy = (float) LandscapeData.getLandscape().getProjectionMinMax().getMaxY();
        float modely = (float) LandscapeData.getLandscape().getProjectionY(x,y);
        return screeny - (( modely - miny) * (screeny-2) / (maxy - miny));
    }

    private void drawSmallPoints(int delta, int wx, int wy) {
        float sx, sy;
        int ix, iy;
        for (int x = 0; x <= LandscapeData.MAX_ARRAYDIM; x += delta) {
            for (int y = 0; y <= LandscapeData.MAX_ARRAYDIM; y += delta) {
                ix = x;
                iy = y;
                sx = transformToOutputX(ix, iy, wx);
                sy = transformToOutputY(ix, iy, wy);
                // canvas.drawPoint(sx, sy, cpaintlines);
                whitelines.thePath.moveTo(sx-1,sy-1);
                whitelines.thePath.lineTo(sx+1,sy+1);
            }
        }
    }

    private void drawBigPoints(int delta, int wx, int wy) {
        float sx, sy;
        int ix, iy;
        // float iz;
        for (int x = 0; x <= LandscapeData.MAX_ARRAYDIM; x += delta) {
            for (int y = 0; y <= LandscapeData.MAX_ARRAYDIM; y += delta) {
                // Inbound mapping
                ix = x;
                iy = y;
                sx = transformToOutputX(ix, iy, wx);
                sy = transformToOutputY(ix, iy, wy);
                // canvas.drawLine(sx, sy, sx + 2, sy + 2, cpaintlines); // +2 makes it appear bigger...
                whitelines.thePath.moveTo(sx-1,sy-1);
                whitelines.thePath.lineTo(sx+1,sy+1);
                whitelines.thePath.moveTo(sx-1,sy+1);
                whitelines.thePath.lineTo(sx+1,sy-1);
            }
        }
    }


    private void drawX(int delta, int wx, int wy) {
        float sx0, sy0;
        int ix0, iy0;
        // float iz0;
        float sx1, sy1;
        int ix1, iy1;
        // float iz1;
        for (int y = 0; y <= LandscapeData.MAX_ARRAYDIM; y += delta) {
            for (int x = 0; x < LandscapeData.MAX_ARRAYDIM; x += delta) {
                if (x == 0) {
                    ix0 = x;
                    iy0 = y;
                    sx0 = transformToOutputX(ix0, iy0, wx);
                    sy0 = transformToOutputY(ix0,iy0, wy);
                    whitelines.thePath.moveTo(sx0, sy0);
                }
                ix1 = (x + delta);
                iy1 = y;
                sx1 = transformToOutputX(ix1, iy1, wx);
                sy1 = transformToOutputY(ix1, iy1, wy);
                whitelines.thePath.lineTo(sx1, sy1);
                // canvas.drawLine(sx0,sy0,sx1,sy1,cpaintlines);
            }
        }
        // canvas.drawPath(linepath, cpaintlines);
        for (int x = 0; x <= LandscapeData.MAX_ARRAYDIM; x += LandscapeData.pow2(LandscapeData.MAX_DETAIL)) {
            for (int y = 0; y < LandscapeData.MAX_ARRAYDIM; y += delta) {
                if (y == 0) {
                    ix0 = x;
                    iy0 = y;
                    sx0 = transformToOutputX(ix0, iy0, wx);
                    sy0 = transformToOutputY(ix0, iy0, wy);
                    whitelines.thePath.moveTo(sx0, sy0);
                }
                ix1 = x;
                iy1 = y + delta;
                sx1 = transformToOutputX(ix1, iy1, wx);
                sy1 = transformToOutputY(ix1, iy1, wy);
                // canvas.drawLine(sx0, sy0, sx1, sy1, cpaintlines);
                whitelines.thePath.lineTo(sx1, sy1);
            }
        }
    }

    private void drawY(int delta, int wx, int wy) {
        float sx0, sy0;
        int ix0, iy0;
        // float iz0;
        float sx2, sy2;
        int ix2, iy2;
        // float iz2;
        for (int x = 0; x <= LandscapeData.MAX_ARRAYDIM; x += delta) {
            for (int y = 0; y < LandscapeData.MAX_ARRAYDIM; y += delta) {
                if (y == 0) {
                    ix0 = x;
                    iy0 = y;
                    sx0 = transformToOutputX(ix0, iy0, wx);
                    sy0 = transformToOutputY(ix0, iy0, wy);
                    whitelines.thePath.moveTo(sx0, sy0);
                }
                ix2 = x;
                iy2 = y + delta;
                sx2 = transformToOutputX(ix2, iy2 , wx);
                sy2 = transformToOutputY(ix2, iy2, wy);
                // canvas.drawLine(sx0,sy0,sx2,sy2,cpaintlines);
                whitelines.thePath.lineTo(sx2, sy2);
            }
        }
        for (int y = 0; y <= LandscapeData.MAX_ARRAYDIM; y += LandscapeData.pow2(LandscapeData.MAX_DETAIL)) {
            for (int x = 0; x < LandscapeData.MAX_ARRAYDIM; x += delta) {
                if (x == 0) {
                    ix0 = x;
                    iy0 = y;
                    sx0 = transformToOutputX(ix0, iy0, wx);
                    sy0 = transformToOutputY(ix0, iy0, wy);
                    whitelines.thePath.moveTo(sx0, sy0);
                }
                ix2 = x + delta;
                iy2 = y;
                sx2 = transformToOutputX(ix2, iy2, wx);
                sy2 = transformToOutputY(ix2, iy2, wy);
                // canvas.drawLine(sx0, sy0, sx2, sy2, cpaintlines);
                whitelines.thePath.lineTo(sx2, sy2);
            }
        }
    }

    private void drawCross(int delta, int wx, int wy) {
        float sx0,sy0;
        int ix0, iy0;
        float sx3,sy3;
        int ix3, iy3;
        for (int yl=LandscapeData.MAX_ARRAYDIM; yl>=0; yl -= delta) {
            boolean cont = true;
            int cur_x=0, cur_y=yl; // start coord of cross line
            int xc, yc; // end coord of cross line
            ix0 = cur_x;
            iy0 = cur_y;
            sx0 = transformToOutputX(ix0, iy0, wx);
            sy0 = transformToOutputY(ix0, iy0, wy);
            whitelines.thePath.moveTo(sx0, sy0);
            while (cont) {
                xc=cur_x+delta;
                yc=cur_y+delta;
                if ( (xc<=LandscapeData.MAX_ARRAYDIM) && (yc<=LandscapeData.MAX_ARRAYDIM)) {
                    ix3 = xc;
                    iy3 = yc;
                    sx3 = transformToOutputX(ix3, iy3, wx);
                    sy3 = transformToOutputY(ix3, iy3, wy);
                    whitelines.thePath.lineTo(sx3, sy3);
                    cur_x += delta;
                    cur_y += delta;
                }
                else {
                    cont = false;
                }
            }
        }
        for (int xl=delta; xl<LandscapeData.MAX_ARRAYDIM; xl += delta) {
            boolean cont = true;
            int cur_x=xl, cur_y=0;
            int xc, yc;
            while (cont) {
                xc=cur_x+delta;
                yc=cur_y+delta;
                ix0 = cur_x;
                iy0 = cur_y;
                sx0 = transformToOutputX(ix0, iy0, wx);
                sy0 = transformToOutputY(ix0, iy0, wy);
                whitelines.thePath.moveTo(sx0, sy0);
                if ( (xc<=LandscapeData.MAX_ARRAYDIM) && (yc<=LandscapeData.MAX_ARRAYDIM)) {
                    ix3 = xc;
                    iy3 = yc;
                    sx3 = transformToOutputX(ix3, iy3, wx);
                    sy3 = transformToOutputY(ix3, iy3, wy);
                    whitelines.thePath.lineTo(sx3, sy3);
                    cur_x += delta;
                    cur_y += delta;
                }
                else {
                    cont = false;
                }
            }
        }
    }

    int orientation(Point3D p1, Point3D p2, Point3D p3) {
        Point3D r1 = new Point3D(p1.x - p2.x, p1.y-p2.y, p1.z-p2.z);
        Point3D r2 = new Point3D(p1.x - p3.x, p1.y-p3.y, p1.z-p3.z);
        Point3D normal = kreuzProd(r1, r2);
        if ( (normal.x >= 0) && (normal.y < 0)) return 1;  // most green
        if ( (normal.x >= 0) && (normal.y >= 0)) return 2; //
        if ( (normal.x < 0)  && (normal.y < 0)) return 3;
        if ( (normal.x < 0)  && (normal.y >= 0)) return 4;
        return 0; // should never happen...
    }

    private void drawShaded(int delta, int wx, int wy) {
        float sx1, sy1, sx2, sy2, sx3, sy3, sx4, sy4;
        for (int y = LandscapeData.MAX_ARRAYDIM-delta; y >= 0; y -= delta) {
            for (int x = LandscapeData.MAX_ARRAYDIM-delta; x >= 0; x -= delta) {
                // 2 triangles: p1: (x,y) - p2: (x,y+delta) - p3: (x+dela, y+delta)
                //              p1: (x,y) - p4: (x+delta,y) - p3: (x+delta, y+delta)

                Point3D p1, p2, p3, p4;
                int orient;
                int x1,x2,x3,x4;
                int y1,y2,y3,y4;
                double z1, z2, z3,z4;
                x1 = x;
                y1 = y;
                z1 = LandscapeData.getLandscape().getLandscapePoint(x1,y1);
                x2 = x;
                y2 = y+delta;
                z2 = LandscapeData.getLandscape().getLandscapePoint(x2,y2);
                x3 = x+delta;
                y3 = y+delta;
                z3 = LandscapeData.getLandscape().getLandscapePoint(x3,y3);
                x4 = x+delta;
                y4 = y;
                z4 = LandscapeData.getLandscape().getLandscapePoint(x4,y4);

                p1 = new Point3D((double)x1, (double)y1, z1);
                p2 = new Point3D((double)x2, (double)y2, z2);
                p3 = new Point3D((double)x3, (double)y3, z3);
                p4 = new Point3D((double)x4, (double)y4, z4);

                sx1 = transformToOutputX(x1, y1, wx);
                sy1 = transformToOutputY(x1, y1, wy);
                sx2 = transformToOutputX(x2, y2, wx);
                sy2 = transformToOutputY(x2, y2, wy);
                sx3 = transformToOutputX(x3, y3, wx);
                sy3 = transformToOutputY(x3, y3, wy);
                sx4 = transformToOutputX(x4, y4, wx);
                sy4 = transformToOutputY(x4, y4, wy);

                // first triangle
                orient = orientation(p1,p2,p3);
                // Log.i("diudkr", "LandscapeView1: x = " + x + " y = " + y + " ori = " + orient);
                if (orient == 0) {
                    Log.i("diudkr", "LandscapeView1: orientation = 0!!");
                    return;
                }
                orient--;
                shadedareas[orient].thePath.moveTo(sx1, sy1);
                shadedareas[orient].thePath.lineTo(sx2, sy2);
                shadedareas[orient].thePath.lineTo(sx3, sy3);
                shadedareas[orient].thePath.close();

                // second triangle
                orient = orientation(p1,p4,p3);
                // Log.i("diudkr", "LandscapeView2: x = " + x + " y = " + y + " ori = " + orient);
                if (orient == 0) {
                    Log.i("diudkr", "LandscapeView2: orientation = 0!!");
                    return;
                }
                orient--;
                shadedareas[orient].thePath.moveTo(sx1, sy1);
                shadedareas[orient].thePath.lineTo(sx4, sy4);
                shadedareas[orient].thePath.lineTo(sx3, sy3);
                shadedareas[orient].thePath.close();

                /*
                // white triangles around
                shadedareas[orient].thePath.moveTo(sx1, sy1);
                shadedareas[orient].thePath.lineTo(sx2, sy2);
                shadedareas[orient].thePath.lineTo(sx3, sy3);
                shadedareas[orient].thePath.lineTo(sx1, sy1);
                shadedareas[orient].thePath.lineTo(sx4, sy4);
                shadedareas[orient].thePath.lineTo(sx3, sy3);
                */
            }
        }
    }

    private void drawRiver(int wx, int wy) {
        float sx0,sy0;
        int ix0, iy0;
        float sx1,sy1;
        int ix1, iy1;
        for (int x=0; x<=LandscapeData.MAX_ARRAYDIM; x++) {
            for (int y=0; y<=LandscapeData.MAX_ARRAYDIM; y++) {
                if (LandscapeData.getLandscape().getPointInfo(x, y).state == 1) { // start of River
                    LandscapeData.River theRiver = LandscapeData.getLandscape().getPointInfo(x, y).river;
                    if ((theRiver != null) && (theRiver.next != null)) {
                        ix0 = theRiver.x;
                        iy0 = theRiver.y;
                        sx0 = transformToOutputX(ix0, iy0, wx);
                        sy0 = transformToOutputY(ix0, iy0, wy);
                        riverlines.thePath.moveTo(sx0, sy0);
                    }
                    while ((theRiver != null) && (theRiver.next != null)) {
                        ix1 = theRiver.next.x;
                        iy1 = theRiver.next.y;
                        sx1 = transformToOutputX(ix1, iy1, wx);
                        sy1 = transformToOutputY(ix1, iy1, wy);
                        riverlines.thePath.lineTo(sx1, sy1);
                        theRiver = theRiver.next;
                    }
                }
            }
        }
    }

    private void drawLake(int wx, int wy) {
        float sx0,sy0;
        int ix0, iy0;
        for (int x=0; x<=LandscapeData.MAX_ARRAYDIM; x++) {
            for (int y=0; y<=LandscapeData.MAX_ARRAYDIM; y++) {
                if (LandscapeData.getLandscape().getPointInfo(x, y).state == 3) {
                    ix0 = x;
                    iy0 = y;
                    sx0 = transformToOutputX(ix0, iy0, wx);
                    sy0 = transformToOutputY(ix0, iy0, wy);
                    lakewater.thePath.moveTo(sx0-2, sy0);
                    lakewater.thePath.lineTo(sx0+2, sy0);
                }
            }
        }
    }

    private void drawOcean(int wx, int wy) {
        float sx0,sy0;
        int ix0, iy0;
        float sx1,sy1;
        int ix1, iy1;
        int tox;
        for (int y=0; y<=LandscapeData.MAX_ARRAYDIM; y++) {
            for (int x=0; x<=LandscapeData.MAX_ARRAYDIM; x++) {
                if (LandscapeData.getLandscape().getPointInfo(x, y).state == 4) {
                    tox = x;
                    while ( (tox < LandscapeData.MAX_ARRAYDIM) && (LandscapeData.getLandscape().getPointInfo(tox+1,y).state == 4)) {
                        tox++;
                    }
                    ix0 = x;
                    iy0 = y;
                    sx0 = transformToOutputX(ix0, iy0, wx);
                    sy0 = transformToOutputY(ix0, iy0, wy);
                    ix1 = tox;
                    iy1 = y;
                    sx1 = transformToOutputX(ix1, iy1, wx);
                    sy1 = transformToOutputY(ix1, iy1, wy);
                    oceanwater.thePath.moveTo(sx0-2, sy0);
                    oceanwater.thePath.lineTo(sx1+2, sy1);
                    x = tox;  // maybe only one point
                }
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas); // necessary?
        //Draw rectangle;
        int wx = this.getWidth();
        int wy = this.getHeight();
        areaRect.thePath.reset();
        areaRect.thePath.addRect(0, 0, wx, wy, Path.Direction.CW);
        if (isInEditMode()) {
            areaRect.thePaint.setColor(Color.GREEN);
            canvas.drawPath(areaRect.thePath, areaRect.thePaint);
            return;
        }

        int id = this.getId();
        if (id == R.id.landscapeView) { // just the background view
            // Points, Lines, ...
            areaRect.thePaint.setColor(Color.GRAY);
            canvas.drawPath(areaRect.thePath, areaRect.thePaint);
            areaRect.thePaint.setColor(Color.CYAN);
            canvas.drawLine(0, 0, wx - 2, 0, areaRect.thePaint);
            canvas.drawLine(0, 0, 0, wy - 2, areaRect.thePaint);
            canvas.drawLine(0, wy - 2, wx - 2, wy - 2, areaRect.thePaint);
            canvas.drawLine(wx - 2, 0, wx - 2, wy - 2, areaRect.thePaint);
            // return;
        }

        // draw wireframe (for source and target view)
        int drawmethod = ViewSettings.getViewSettings().getDrawmethod();
        int delta = LandscapeData.MAX_ARRAYDIM / LandscapeData.pow2(ViewSettings.getViewSettings().getDetailLevel());

        if (drawmethod == 0) { // small points
            whitelines.thePath.reset();
            drawSmallPoints(delta, wx, wy);
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        if (drawmethod == 1) { // big points
            whitelines.thePath.reset();
            drawBigPoints(delta, wx, wy);
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        if (drawmethod == 2) { // Lines X
            whitelines.thePath.reset();
            drawX(delta, wx, wy);
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        if (drawmethod == 3) { // Lines Y
            whitelines.thePath.reset();
            drawY(delta, wx, wy);
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        if (drawmethod == 4) { // 3d Squares
            whitelines.thePath.reset();
            drawX(delta, wx, wy);
            drawY(delta, wx, wy);
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        if (drawmethod == 5) { // 3d Triangles
            whitelines.thePath.reset();
            drawX(delta, wx, wy);
            drawY(delta, wx, wy);
            drawCross(delta, wx, wy);
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        if (drawmethod == 6) { // Shaded Triangles
            for (int i=0; i<4; i++) {
                shadedareas[i].thePath.reset();
            }
            whitelines.thePath.reset();
            drawShaded(delta, wx, wy);
            drawX(delta, wx, wy);
            drawY(delta, wx, wy);
            drawCross(delta, wx, wy);
            for (int i=0; i<4; i++) {
                canvas.drawPath(shadedareas[i].thePath, shadedareas[i].thePaint);
            }
            canvas.drawPath(whitelines.thePath, whitelines.thePaint);
        }

        boolean hasWater = LandscapeData.getLandscape().hasWater();
        if (hasWater) {
            riverlines.thePath.reset();
            drawRiver(wx, wy);
            canvas.drawPath(riverlines.thePath, riverlines.thePaint);

            lakewater.thePath.reset();
            drawLake(wx, wy);
            canvas.drawPath(lakewater.thePath, lakewater.thePaint);
        }

        boolean hasOcean = LandscapeData.getLandscape().hasOcean();
        if (hasOcean) {
            oceanwater.thePath.reset();
            drawOcean(wx, wy);
            canvas.drawPath(oceanwater.thePath, oceanwater.thePaint);
        }

    }
    void doInit() {
        // used colors
        int lakecolor = Color.rgb(0,120,225);
        int rivercolor = Color.rgb(0,170,255);
        int oceancolor = Color.rgb(0,0,255);
        int linecolor = Color.WHITE;

        // the drawing area and its border
        areaRect.thePath = new Path();
        areaRect.thePaint = new Paint();
        areaRect.thePaint.setColor(Color.CYAN);
        areaRect.thePaint.setStyle(Paint.Style.STROKE);
        areaRect.thePaint.setAntiAlias(false);

        // white wireframe
        whitelines.thePath = new Path();
        whitelines.thePaint = new Paint();
        whitelines.thePaint.setColor(linecolor);
        whitelines.thePaint.setStyle(Paint.Style.STROKE);
        whitelines.thePaint.setAntiAlias(false);

        // 4 shaded areas
        for (int i=0; i<4; i++) {
            shadedareas[i] = new PathAndPaintHolder();
            shadedareas[i].thePath = new Path();
            shadedareas[i].thePath.setFillType(Path.FillType.EVEN_ODD);
            shadedareas[i].thePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadedareas[i].thePaint.setStyle(Paint.Style.FILL_AND_STROKE); // solid
            shadedareas[i].thePaint.setAntiAlias(false);
        }
        shadedareas[0].thePaint.setColor(Color.rgb(10, 230, 10));
        shadedareas[1].thePaint.setColor(Color.rgb(10, 200, 10));
        shadedareas[2].thePaint.setColor(Color.rgb(10, 180, 10));
        shadedareas[3].thePaint.setColor(Color.rgb(10, 150, 10));

        // blue river
        riverlines.thePath = new Path();
        riverlines.thePaint = new Paint();
        riverlines.thePaint.setColor(rivercolor);
        riverlines.thePaint.setStyle(Paint.Style.STROKE);
        riverlines.thePaint.setStrokeWidth(4f);
        riverlines.thePaint.setAntiAlias(false);

        // blue lake
        lakewater.thePath = new Path();
        lakewater.thePaint = new Paint();
        lakewater.thePaint.setColor(lakecolor);
        lakewater.thePaint.setStyle(Paint.Style.STROKE);
        lakewater.thePaint.setStrokeWidth(4f);
        lakewater.thePaint.setAntiAlias(false);

        // blue ocean
        oceanwater.thePath = new Path();
        oceanwater.thePaint = new Paint();
        oceanwater.thePaint.setColor(oceancolor);
        oceanwater.thePaint.setStyle(Paint.Style.STROKE);
        oceanwater.thePaint.setStrokeWidth(4f);
        oceanwater.thePaint.setAntiAlias(false);
    }
}
