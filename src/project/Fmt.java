package project;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
 
/** Small helpers to format numbers nicely. */
public final class Fmt {
    private Fmt() {}
 
    /** Format a double with up to 4 significant digits, no trailing zeros. */
    public static String num(double v) {
        if (v == 0 || Double.isNaN(v)) return v == 0 ? "0" : "NaN";
        if (Double.isInfinite(v)) return v > 0 ? "\u221E" : "-\u221E";
        double a = Math.abs(v);
        if (a < 1e-4 || a >= 1e7) {
            return String.format(Locale.US, "%.3e", v);
        }
        BigDecimal bd = new BigDecimal(v).round(new MathContext(4)).stripTrailingZeros();
        return bd.toPlainString();
    }
 
    /** Format a value with an SI prefix, e.g. 1e-6 F -> "1 µF". */
    public static String si(double v, String unit) {
        if (v == 0) return "0 " + unit;
        String[] prefixes = {"p", "n", "\u00B5", "m", "", "k", "M", "G"};
        double a = Math.abs(v);
        int exp = (int) Math.floor(Math.log10(a) / 3.0) * 3;
        exp = Math.max(-12, Math.min(9, exp));
        double scaled = v / Math.pow(10, exp);
        return num(scaled) + " " + prefixes[(exp + 12) / 3] + unit;
    }
}