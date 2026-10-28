package project;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Draws the circuit diagram. Parallel: every element in its own vertical branch, source in the
 * left-most branch. Series: elements in the upper horizontal branch, source in the lower branch.
 * Clicking an element shows its voltage and current.
 */
public class CircuitPanel extends JPanel {
    private static final int SRC_R = 20;
 
    private boolean parallel;
    private Source source;
    private List<Element> elements;
    private Analyzer.Result result;
    private final List<Rectangle> hit = new ArrayList<>();
 
    public CircuitPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(620, 340));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = elementAt(e.getPoint());
                if (idx >= 0) showInfo(idx);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(
                        elementAt(e.getPoint()) >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }
 
    public void setCircuit(boolean parallel, Source source, List<Element> elements, Analyzer.Result result) {
        this.parallel = parallel;
        this.source = source;
        this.elements = new ArrayList<>(elements);
        this.result = result;
        repaint();
    }
 
    public void clear() {
        this.elements = null;
        this.result = null;
        this.source = null;
        hit.clear();
        repaint();
    }
 
    private int elementAt(Point p) {
        for (int i = 0; i < hit.size(); i++) {
            if (hit.get(i).contains(p)) return i;
        }
        return -1;
    }
 
    private static String cs(Complex c) {
        return c == null ? "N/A" : c.toString();
    }
 
    private void showInfo(int i) {
        if (result == null || elements == null || i >= elements.size()) return;
        Element e = elements.get(i);
        String text = e.name + "  (" + e.valueText() + ")\n\n"
                + "Voltage U = " + cs(result.u[i]) + " V\n"
                + "Current I = " + cs(result.i[i]) + " A\n"
                + "Resistance R = " + (result.r[i] == null ? "\u221E" : result.r[i].toString()) + " \u03A9";
        JOptionPane.showMessageDialog(this, text, "Element " + e.name, JOptionPane.INFORMATION_MESSAGE);
    }
 
    // ------------------------------------------------------------------ painting
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2f));
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        hit.clear();
 
        if (elements == null || source == null) {
            g2.setColor(Color.GRAY);
            drawCentered(g2, "Build a circuit and press Submit to see the diagram", getWidth() / 2, getHeight() / 2);
        } else {
            g2.setColor(Color.BLACK);
            if (parallel) drawParallel(g2); else drawSeries(g2);
        }
        g2.dispose();
    }
 
    private void drawParallel(Graphics2D g2) {
        int n = elements.size();
        int W = getWidth(), H = getHeight();
        int left = 70, right = W - 70, top = 60, bottom = H - 60;
        int cy = (top + bottom) / 2;
        int len = 80;
        int gap = Math.min(150, (right - left) / n);
        int lastX = left + n * gap;
 
        g2.drawLine(left, top, lastX, top);
        g2.drawLine(left, bottom, lastX, bottom);
 
        // source branch
        g2.drawLine(left, top, left, cy - SRC_R);
        g2.drawLine(left, cy + SRC_R, left, bottom);
        drawSource(g2, left, cy, true);
        g2.setColor(Color.BLACK);
        drawLeft(g2, Fmt.si(source.voltage, "V"), left + SRC_R + 6, cy - (source.ac ? 4 : 0));
        if (source.ac) drawLeft(g2, Fmt.si(source.frequency, "Hz"), left + SRC_R + 6, cy + 14);
 
        // element branches
        for (int i = 0; i < n; i++) {
            Element e = elements.get(i);
            int x = left + (i + 1) * gap;
            g2.setColor(Color.BLACK);
            g2.drawLine(x, top, x, cy - len / 2);
            g2.drawLine(x, cy + len / 2, x, bottom);
            g2.fillOval(x - 3, top - 3, 6, 6);
            g2.fillOval(x - 3, bottom - 3, 6, 6);
            drawSymbol(g2, e.type, x, cy, len, true);
            g2.setColor(Color.BLACK);
            drawLeft(g2, e.name, x + 16, cy - 4);
            drawLeft(g2, e.valueText(), x + 16, cy + 14);
            hit.add(new Rectangle(x - 24, cy - len / 2 - 4, Math.min(gap, 110), len + 8));
        }
    }
 
    private void drawSeries(Graphics2D g2) {
        int n = elements.size();
        int W = getWidth(), H = getHeight();
        int left = 60, right = W - 60, top = 100, bottom = H - 70;
        int spacing = (right - left) / (n + 1);
        int len = Math.min(70, spacing - 10);
 
        int prev = left;
        for (int i = 0; i < n; i++) {
            Element e = elements.get(i);
            int x = left + (i + 1) * spacing;
            g2.setColor(Color.BLACK);
            g2.drawLine(prev, top, x - len / 2, top);
            drawSymbol(g2, e.type, x, top, len, false);
            g2.setColor(Color.BLACK);
            drawCentered(g2, e.name, x, top - 34);
            drawCentered(g2, e.valueText(), x, top - 18);
            hit.add(new Rectangle(x - len / 2, top - 44, len, 60));
            prev = x + len / 2;
        }
        g2.setColor(Color.BLACK);
        g2.drawLine(prev, top, right, top);
        g2.drawLine(left, top, left, bottom);
        g2.drawLine(right, top, right, bottom);
 
        int cx = (left + right) / 2;
        g2.drawLine(left, bottom, cx - SRC_R, bottom);
        g2.drawLine(cx + SRC_R, bottom, right, bottom);
        drawSource(g2, cx, bottom, false);
        g2.setColor(Color.BLACK);
        drawCentered(g2, source.label(), cx, bottom + SRC_R + 18);
    }
 
    // ------------------------------------------------------------------ symbols
 
    /** Draws an element symbol centered at (cx, cy); horizontal or rotated by 90 degrees. */
    private void drawSymbol(Graphics2D g2, Element.Type type, int cx, int cy, int len, boolean vertical) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);
        if (vertical) g2.rotate(Math.PI / 2);
        g2.setColor(Color.BLACK);
        int x0 = -len / 2;
        switch (type) {
            case RESISTOR: {
                int lead = len / 5;
                int a = x0 + lead, b = x0 + len - lead;
                g2.drawLine(x0, 0, a, 0);
                Path2D zig = new Path2D.Double();
                zig.moveTo(a, 0);
                for (int k = 1; k <= 6; k++) {
                    double x = a + k * (b - a) / 6.0;
                    double y = (k == 6) ? 0 : (k % 2 == 1 ? -9 : 9);
                    zig.lineTo(x, y);
                }
                g2.draw(zig);
                g2.drawLine(b, 0, x0 + len, 0);
                break;
            }
            case CAPACITOR: {
                g2.drawLine(x0, 0, -5, 0);
                g2.drawLine(5, 0, x0 + len, 0);
                g2.drawLine(-5, -15, -5, 15);
                g2.drawLine(5, -15, 5, 15);
                break;
            }
            default: { // INDUCTOR
                int lead = len * 15 / 100;
                int coil = len - 2 * lead;
                double w = coil / 4.0;
                g2.drawLine(x0, 0, x0 + lead, 0);
                for (int k = 0; k < 4; k++) {
                    g2.draw(new Arc2D.Double(x0 + lead + k * w, -w / 2, w, w, 0, 180, Arc2D.OPEN));
                }
                g2.drawLine((int) (x0 + lead + coil), 0, x0 + len, 0);
                break;
            }
        }
        g2.setTransform(old);
    }
 
    private void drawSource(Graphics2D g2, int cx, int cy, boolean vertical) {
        g2.setColor(getBackground());
        g2.fillOval(cx - SRC_R, cy - SRC_R, 2 * SRC_R, 2 * SRC_R);
        g2.setColor(Color.BLACK);
        g2.drawOval(cx - SRC_R, cy - SRC_R, 2 * SRC_R, 2 * SRC_R);
        if (source.ac) {
            Path2D p = new Path2D.Double();
            p.moveTo(cx - 12, cy);
            p.curveTo(cx - 6, cy - 14, cx - 2, cy - 14, cx, cy);
            p.curveTo(cx + 2, cy + 14, cx + 6, cy + 14, cx + 12, cy);
            g2.draw(p);
        } else if (vertical) { // + on top
            g2.drawLine(cx - 4, cy - 9, cx + 4, cy - 9);
            g2.drawLine(cx, cy - 13, cx, cy - 5);
            g2.drawLine(cx - 4, cy + 9, cx + 4, cy + 9);
        } else { // + on the right
            g2.drawLine(cx + 5, cy, cx + 13, cy);
            g2.drawLine(cx + 9, cy - 4, cx + 9, cy + 4);
            g2.drawLine(cx - 13, cy, cx - 5, cy);
        }
    }
 
    private void drawCentered(Graphics2D g2, String text, int cx, int baseline) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, baseline);
    }
 
    private void drawLeft(Graphics2D g2, String text, int x, int baseline) {
        g2.drawString(text, x, baseline);
    }
}