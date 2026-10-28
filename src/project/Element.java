package project;
/** One electrical element (resistor, capacitor or inductor). */
public final class Element {
    public enum Type {
        RESISTOR("R", "Resistance", "\u03A9"),
        CAPACITOR("C", "Capacitance", "F"),
        INDUCTOR("L", "Inductance", "H");
 
        public final String prefix;
        public final String parameter;
        public final String unit;
 
        Type(String prefix, String parameter, String unit) {
            this.prefix = prefix;
            this.parameter = parameter;
            this.unit = unit;
        }
    }
 
    public final Type type;
    public final String name;
    public final double value;
 
    public Element(Type type, String name, double value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }
 
    public String valueText() {
        return Fmt.si(value, type.unit);
    }
}