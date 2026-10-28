package project;
import java.util.Arrays;
import java.util.List;

/** Quick console check of the analysis formulae (no GUI needed). */
public class AnalyzerTest {
    static Element r(String n, double v) { return new Element(Element.Type.RESISTOR, n, v); }
    static Element c(String n, double v) { return new Element(Element.Type.CAPACITOR, n, v); }
    static Element l(String n, double v) { return new Element(Element.Type.INDUCTOR, n, v); }

    static void run(String title, boolean parallel, Source s, Element... els) {
        List<Element> list = Arrays.asList(els);
        Analyzer.Result res = Analyzer.analyze(parallel, s, list);
        System.out.println("== " + title);
        if (!res.ok()) {
            System.out.println("   ERROR: " + res.error.replace('\n', ' '));
            return;
        }
        for (int k = 0; k < els.length; k++) {
            System.out.println("   " + els[k].name + ": U=" + res.u[k] + "  I=" + res.i[k]
                    + "  R=" + (res.r[k] == null ? "inf" : res.r[k]));
        }
        System.out.println("   Req = " + (res.reqInfinite ? "inf" : res.req));
    }

    public static void main(String[] args) {
        run("Series DC 10V, R1=100, R2=400", false, Source.dc(10), r("R1", 100), r("R2", 400));
        run("Series DC with capacitor (open circuit)", false, Source.dc(10), r("R1", 100), c("C2", 1e-6));
        run("Series DC only inductor (short)", false, Source.dc(10), l("L1", 1e-3));
        run("Series AC 1V 1kHz R=100, C=1uF, L=1mH", false, Source.ac(1, 1000), r("R1", 100), c("C2", 1e-6), l("L3", 1e-3));
        run("Parallel DC 10V, R1=100, R2=100", true, Source.dc(10), r("R1", 100), r("R2", 100));
        run("Parallel DC with R and C", true, Source.dc(10), r("R1", 100), c("C2", 1e-6));
        run("Parallel DC with inductor (short)", true, Source.dc(10), r("R1", 100), l("L2", 1e-3));
        run("Parallel AC 1V 1kHz R=100, C=1uF, L=1mH", true, Source.ac(1, 1000), r("R1", 100), c("C2", 1e-6), l("L3", 1e-3));
    }
}