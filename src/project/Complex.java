package project;
/** Immutable complex number (i is the imaginary unit). */
public final class Complex {
    public static final Complex ZERO = new Complex(0, 0);
    public static final Complex ONE = new Complex(1, 0);
 
    public final double re;
    public final double im;
 
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }
 
    public Complex add(Complex o) {
        return new Complex(re + o.re, im + o.im);
    }
 
    public Complex sub(Complex o) {
        return new Complex(re - o.re, im - o.im);
    }
 
    public Complex mul(Complex o) {
        return new Complex(re * o.re - im * o.im, re * o.im + im * o.re);
    }
 
    /** Division; caller must make sure the divisor is not zero. */
    public Complex div(Complex o) {
        double d = o.re * o.re + o.im * o.im;
        return new Complex((re * o.re + im * o.im) / d, (im * o.re - re * o.im) / d);
    }
 
    public double abs() {
        return Math.hypot(re, im);
    }
 
    @Override
    public String toString() {
        double a = Math.abs(re), b = Math.abs(im);
        if (b <= 1e-9 * a) return Fmt.num(re);
        if (a <= 1e-9 * b) return Fmt.num(im) + "i";
        return Fmt.num(re) + (im < 0 ? " - " : " + ") + Fmt.num(b) + "i";
    }
}