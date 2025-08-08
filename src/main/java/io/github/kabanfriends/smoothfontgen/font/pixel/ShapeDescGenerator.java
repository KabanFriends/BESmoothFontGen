package io.github.kabanfriends.smoothfontgen.font.pixel;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Convert a pixel-art glyph (white strokes on transparent background) provided
 * as java.awt.Image into an msdfgen shape description string.
 *
 * Coordinates in the output use a bottom-left origin (y increases upward),
 * and units are pixels of the input image. By default edges are colored by
 * cycling {"m","y","c"} so adjacent right-angle edges share exactly one
 * MSDF channel (m=magenta, y=yellow, c=cyan).
 */
public class ShapeDescGenerator {

    private ShapeDescGenerator() {
    }

    private static final class Pt {
        final int x, y;
        Pt(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Pt)) return false;
            Pt p = (Pt) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return 31 * x + y; }
        @Override public String toString() { return x + "," + y; }
    }

    private static final class UndirectedEdge {
        final Pt a, b; // lexicographically a <= b
        UndirectedEdge(Pt p1, Pt p2) {
            if (comparePt(p1, p2) <= 0) { a = p1; b = p2; }
            else { a = p2; b = p1; }
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof UndirectedEdge)) return false;
            UndirectedEdge e = (UndirectedEdge) o;
            return a.equals(e.a) && b.equals(e.b);
        }
        @Override public int hashCode() { return 31 * a.hashCode() + b.hashCode(); }
    }

    private static int comparePt(Pt p1, Pt p2) {
        if (p1.x != p2.x) return Integer.compare(p1.x, p2.x);
        return Integer.compare(p1.y, p2.y);
    }

    /** Convenience default entry (alphaThreshold=0, colorCycle=m,y,c). */
    public static String imageToShapeDesc(Image img) {
        return imageToShapeDesc(img, 25, new String[] { "m", "y", "c" },
                1.0, 0.0, 0.0);
    }

    /**
     * Main conversion function.
     * - img: input image (white strokes, transparent background)
     * - alphaThreshold: alpha > threshold considered foreground
     * - colorCycle: sequence of tokens like {"m","y","c"} (repeated across edges)
     * - scale / translate: optional transform applied to output coordinates
     */
    public static String imageToShapeDesc(Image img, int alphaThreshold,
                                          String[] colorCycle, double scale, double translateX,
                                          double translateY) {

        if (img == null) throw new IllegalArgumentException("img == null");

        // Convert to BufferedImage ARGB if needed
        BufferedImage bi;
        if (img instanceof BufferedImage) bi = (BufferedImage) img;
        else {
            int w0 = img.getWidth(null), h0 = img.getHeight(null);
            if (w0 <= 0 || h0 <= 0) throw new IllegalArgumentException("Image has no size");
            bi = new BufferedImage(w0, h0, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
        }

        final int w = bi.getWidth();
        final int h = bi.getHeight();

        // Build boolean mask (top-left origin: mask[row][col])
        boolean[][] mask = new boolean[h][w];
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int argb = bi.getRGB(xx, yy);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                mask[yy][xx] = (a > alphaThreshold) && ((r + g + b) > 0);
            }
        }

        // Pool Pt instances keyed by (x<<32)|y
        final Map<Long, Pt> pool = new HashMap<>();
        java.util.function.BiFunction<Integer,Integer,Pt> getPt = (xi, yi) -> {
            long key = (((long) xi) << 32) | (yi & 0xFFFFFFFFL);
            Pt p = pool.get(key);
            if (p == null) { p = new Pt(xi, yi); pool.put(key, p); }
            return p;
        };

        // netCounts for undirected edges: positive means a->b (a==min,b==max)
        final Map<UndirectedEdge, Integer> netCounts = new HashMap<>();

        // Add directed edge 'from -> to' by updating undirected net count
        java.util.function.BiConsumer<Pt,Pt> addDirected = (from, to) -> {
            UndirectedEdge key = new UndirectedEdge(from, to);
            int delta = key.a.equals(from) ? +1 : -1;
            Integer cur = netCounts.get(key);
            netCounts.put(key, (cur == null) ? delta : (cur + delta));
        };

        // For each foreground pixel (top-left coords yy,xx) add its CCW unit-square
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                if (!mask[yy][xx]) continue;
                int xl = xx;
                int xr = xx + 1;
                int yb = h - yy - 1; // convert to bottom-left coords
                int yt = yb + 1;
                Pt p0 = getPt.apply(xl, yb);
                Pt p1 = getPt.apply(xr, yb);
                Pt p2 = getPt.apply(xr, yt);
                Pt p3 = getPt.apply(xl, yt);
                // add CCW edges: p0->p1->p2->p3->p0
                addDirected.accept(p0, p1);
                addDirected.accept(p1, p2);
                addDirected.accept(p2, p3);
                addDirected.accept(p3, p0);
            }
        }

        // Build outgoing adjacency lists for remaining directed edges
        final Map<Pt, List<Pt>> outAdj = new HashMap<>();
        for (Map.Entry<UndirectedEdge, Integer> e : netCounts.entrySet()) {
            int v = e.getValue();
            if (v == 0) continue;
            Pt a = e.getKey().a;
            Pt b = e.getKey().b;
            if (v > 0) {
                List<Pt> list = outAdj.computeIfAbsent(a, k -> new ArrayList<>());
                for (int i = 0; i < v; i++) list.add(b);
            } else {
                List<Pt> list = outAdj.computeIfAbsent(b, k -> new ArrayList<>());
                for (int i = 0; i < -v; i++) list.add(a);
            }
        }

        // Convert adjacency lists to queues and walk cycles consuming edges
        final Map<Pt, Deque<Pt>> outQ = new HashMap<>();
        for (Map.Entry<Pt, List<Pt>> e : outAdj.entrySet()) {
            outQ.put(e.getKey(), new ArrayDeque<>(e.getValue()));
        }

        final List<List<Pt>> loops = new ArrayList<>();
        // While any directed edge remains
        while (true) {
            Pt startFrom = null;
            for (Map.Entry<Pt, Deque<Pt>> e : outQ.entrySet()) {
                if (!e.getValue().isEmpty()) { startFrom = e.getKey(); break; }
            }
            if (startFrom == null) break;
            Deque<Pt> dq = outQ.get(startFrom);
            Pt startTo = dq.removeFirst();
            Pt u = startFrom;
            Pt v = startTo;
            List<Pt> poly = new ArrayList<>();
            int safety = 0;
            while (true) {
                poly.add(u);
                Deque<Pt> dqV = outQ.get(v);
                if (dqV == null || dqV.isEmpty()) {
                    // degenerate / incomplete boundary, break defensively
                    break;
                }
                Pt nxt = dqV.removeFirst();
                u = v;
                v = nxt;
                if (u.equals(startFrom) && v.equals(startTo)) break; // closed
                if (++safety > (pool.size() * 4 + 10000)) break;
            }
            if (poly.size() >= 3) loops.add(simplifyAxisAligned(poly));
        }

        // Canonicalize and dedupe loops
        List<List<Pt>> norm = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (List<Pt> p : loops) {
            if (p.size() < 3) continue;
            int minIdx = 0;
            for (int i = 1; i < p.size(); i++) {
                if (comparePt(p.get(i), p.get(minIdx)) < 0) minIdx = i;
            }
            List<Pt> rot = new ArrayList<>();
            for (int i = 0; i < p.size(); i++) rot.add(p.get((minIdx + i) % p.size()));
            String key = serializePoly(rot);
            if (!seen.contains(key)) { seen.add(key); norm.add(rot); }
        }

        // Classify by signed area: area>0 => CCW => filled (outer),
        // area<0 => CW => hole. Then ensure outer CCW, hole CW.
        List<String> blocks = new ArrayList<>();
        for (List<Pt> poly : norm) {
            double area = polygonSignedArea(poly);
            if (Math.abs(area) < 1e-9) continue; // ignore degenerate
            if (area > 0) {
                // outer contour (ensure CCW)
                List<Pt> outPoly = new ArrayList<>(poly);
                if (polygonSignedArea(outPoly) < 0) Collections.reverse(outPoly);
                blocks.add(polygonToShapedesc(outPoly, colorCycle, scale,
                        translateX, translateY));
            } else {
                // hole (ensure CW)
                List<Pt> hole = new ArrayList<>(poly);
                if (polygonSignedArea(hole) > 0) Collections.reverse(hole);
                blocks.add(polygonToShapedesc(hole, colorCycle, scale,
                        translateX, translateY));
            }
        }

        return String.join("\n", blocks);
    }

    // ----- helpers -----------------------------------------------------

    private static List<Pt> simplifyAxisAligned(List<Pt> poly) {
        if (poly.size() <= 2) return new ArrayList<>(poly);
        List<Pt> out = new ArrayList<>();
        int n = poly.size();
        for (int i = 0; i < n; i++) {
            Pt a = poly.get((i + n - 1) % n);
            Pt b = poly.get(i);
            Pt c = poly.get((i + 1) % n);
            if ((a.x == b.x && b.x == c.x) || (a.y == b.y && b.y == c.y)) continue;
            out.add(b);
        }
        if (out.size() < 3) return new ArrayList<>(poly);
        return out;
    }

    private static double polygonSignedArea(List<Pt> poly) {
        double a = 0.0;
        int n = poly.size();
        for (int i = 0; i < n; i++) {
            Pt p0 = poly.get(i);
            Pt p1 = poly.get((i + 1) % n);
            a += (double) p0.x * p1.y - (double) p1.x * p0.y;
        }
        return 0.5 * a;
    }

    private static String polygonToShapedesc(List<Pt> poly, String[] colorCycle,
                                             double scale, double translateX, double translateY) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        int n = poly.size();
        for (int i = 0; i < n; i++) {
            double x = poly.get(i).x * scale + translateX;
            double y = poly.get(i).y * scale + translateY;
            sb.append(formatCoord(x)).append(",").append(formatCoord(y));
            sb.append("; ");
            String color = colorCycle[i % colorCycle.length];
            sb.append(color).append("; ");
        }
        sb.append("# }");
        return sb.toString();
    }

    private static String serializePoly(List<Pt> poly) {
        StringBuilder sb = new StringBuilder();
        for (Pt p : poly) {
            if (sb.length() > 0) sb.append(";");
            sb.append(p.x).append(",").append(p.y);
        }
        return sb.toString();
    }

    private static String formatCoord(double v) {
        double r = Math.rint(v);
        if (Math.abs(v - r) < 1e-9) return String.format(Locale.US, "%d", (long) r);
        String s = String.format(Locale.US, "%.6f", v);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}