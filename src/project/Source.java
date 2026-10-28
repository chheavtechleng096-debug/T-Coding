package project;
/** A voltage source: DC (voltage only) or AC (voltage and frequency). */
public final class Source {
    public final boolean ac;
    public final double voltage;
    public final double frequency; // Hz, only meaningful when ac == true
 
    private Source(boolean ac, double voltage, double frequency) {
        this.ac = ac;
        this.voltage = voltage;
        this.frequency = frequency;
    }
 
    public static Source dc(double voltage) {
        return new Source(false, voltage, Double.POSITIVE_INFINITY);
    }
 
    public static Source ac(double voltage, double frequency) {
        return new Source(true, voltage, frequency);
    }
 
    public String label() {
        String s = Fmt.si(voltage, "V");
        return ac ? s + ", " + Fmt.si(frequency, "Hz") : s;
    }
}