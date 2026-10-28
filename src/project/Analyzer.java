package project;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Circuit analysis for a series or a parallel circuit with a single source.
 * All quantities are complex numbers. A resistance of {@code null} means infinity
 * (a capacitor in a DC circuit).
 */
public final class Analyzer {
    private Analyzer() {}
 
    private static final double EPS = 1e-9;
 
    public static final class Result {
        public String error;          // non-null => short circuit (or other failure)
        public Complex[] u;           // voltage per element (null entry = undetermined)
        public Complex[] i;           // current per element
        public Complex[] r;           // resistance per element (null entry = infinity)
        public Complex req;           // equivalent resistance
        public boolean reqInfinite;   // true when equivalent resistance is infinite
        public String note;           // optional remark for the user
 
        public boolean ok() {
            return error == null;
        }
    }
 
    /** Resistance (impedance) of an element; null means infinity. */
    static Complex resistance(Element e, Source s) {
        switch (e.type) {
            case RESISTOR:
                return new Complex(e.value, 0);
            case CAPACITOR:
                if (s.ac) return new Complex(0, -1.0 / (2 * Math.PI * s.frequency * e.value));
                return null; // infinity in DC
            default: // INDUCTOR
                if (s.ac) return new Complex(0, 2 * Math.PI * s.frequency * e.value);
                return Complex.ZERO; // 0 in DC
        }
    }
 
    public static Result analyze(boolean parallel, Source s, List<Element> els) {
        return parallel ? analyzeParallel(s, els) : analyzeSeries(s, els);
    }
 
    private static String join(List<String> names) {
        return String.join(", ", names);
    }
 
    private static Result analyzeSeries(Source s, List<Element> els) {
        int n = els.size();
        Result res = new Result();
        res.r = new Complex[n];
        res.i = new Complex[n];
        res.u = new Complex[n];
        Complex vs = new Complex(s.voltage, 0);
 
        Complex sum = Complex.ZERO;
        double sumAbs = 0;
        int infCount = 0;
        for (int k = 0; k < n; k++) {
            res.r[k] = resistance(els.get(k), s);
            if (res.r[k] == null) {
                infCount++;
            } else {
                sum = sum.add(res.r[k]);
                sumAbs += res.r[k].abs();
            }
        }
 
        // Open circuit: some element has infinite resistance -> no current flows.
        if (infCount > 0) {
            res.reqInfinite = true;
            for (int k = 0; k < n; k++) {
                res.i[k] = Complex.ZERO;
                if (res.r[k] == null) res.u[k] = infCount == 1 ? vs : null;
                else res.u[k] = Complex.ZERO;
            }
            if (infCount > 1) {
                res.note = "More than one capacitor in a DC series circuit: "
                        + "the voltage split between them cannot be determined (N/A).";
            }
            return res;
        }
 
        // Short circuit: equivalent resistance is zero -> division by zero.
        if (sum.abs() <= EPS * sumAbs) {
            List<String> causes = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                if (res.r[k].abs() == 0) causes.add(els.get(k).name);
            }
            if (causes.isEmpty()) {
                for (Element e : els) causes.add(e.name);
            }
            res.error = "Short circuit detected in the series circuit, caused by: " + join(causes)
                    + ".\nPlease change these elements.";
            return res;
        }
 
        res.req = sum;
        Complex current = vs.div(sum);
        for (int k = 0; k < n; k++) {
            res.i[k] = current;
            res.u[k] = current.mul(res.r[k]);
        }
        return res;
    }
 
    private static Result analyzeParallel(Source s, List<Element> els) {
        int n = els.size();
        Result res = new Result();
        res.r = new Complex[n];
        res.i = new Complex[n];
        res.u = new Complex[n];
        Complex vs = new Complex(s.voltage, 0);
 
        // An inductor in a parallel DC circuit is a short circuit.
        if (!s.ac) {
            List<String> inductors = new ArrayList<>();
            for (Element e : els) {
                if (e.type == Element.Type.INDUCTOR) inductors.add(e.name);
            }
            if (!inductors.isEmpty()) {
                res.error = "Short circuit detected: " + join(inductors)
                        + " (inductor) in a parallel DC circuit has zero resistance.\n"
                        + "Please change this circuit element.";
                return res;
            }
        }
 
        Complex admittance = Complex.ZERO;
        double admAbs = 0;
        List<String> zero = new ArrayList<>();
        for (int k = 0; k < n; k++) {
            Element e = els.get(k);
            res.r[k] = resistance(e, s);
            res.u[k] = vs;
            if (res.r[k] == null) {
                res.i[k] = Complex.ZERO; // divide by infinity -> 0
            } else if (res.r[k].abs() == 0) {
                zero.add(e.name);
            } else {
                Complex y = Complex.ONE.div(res.r[k]);
                admittance = admittance.add(y);
                admAbs += y.abs();
                res.i[k] = vs.mul(y);
            }
        }
        if (!zero.isEmpty()) {
            res.error = "Short circuit detected, caused by: " + join(zero)
                    + " (zero resistance).\nPlease change these elements.";
            return res;
        }
 
        if (admittance.abs() <= EPS * admAbs) {
            res.reqInfinite = true; // no conducting branch at all
        } else {
            res.req = Complex.ONE.div(admittance);
        }
        return res;
    }
}