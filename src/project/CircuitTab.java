package project;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** One tab of the simulator: either the parallel or the series circuit builder. */
public class CircuitTab extends JPanel {
    private static final int MAX_ELEMENTS = 5;

    /** Thrown for invalid user input. */
    private static class InputException extends Exception {
        InputException(String msg) {
            super(msg);
        }
    }

    /** One row in the element panel. */
    private static class Row {
        final Element.Type type;
        final String name;
        final JTextField field = new JTextField(9);

        Row(Element.Type type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private final boolean parallel;
    private final List<Row> rows = new ArrayList<>();
    private int counter = 0;

    private final JComboBox<String> sourceType = new JComboBox<>(new String[]{"DC", "AC"});
    private final JTextField voltageField = new JTextField("5", 7);
    private final JTextField freqField = new JTextField("50", 7);
    private final JPanel freqRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    private final JPanel rowsPanel = new JPanel();
    private final JButton addR = new JButton("+ Resistor");
    private final JButton addC = new JButton("+ Capacitor");
    private final JButton addL = new JButton("+ Inductor");

    private final CircuitPanel diagram = new CircuitPanel();
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel reqLabel = new JLabel(" ");
    private final JLabel status = new JLabel(" ");

    public CircuitTab(boolean parallel) {
        this.parallel = parallel;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        add(buildInputPanel(), BorderLayout.WEST);
        add(buildOutputPanel(), BorderLayout.CENTER);
        updateSourceFields();
    }

    // ------------------------------------------------------------ UI construction

    private JPanel buildInputPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(330, 0));

        // voltage source
        JPanel sp = new JPanel(new GridLayout(0, 1, 2, 2));
        sp.setBorder(new TitledBorder("Voltage source"));
        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        r1.add(new JLabel("Type:"));
        r1.add(sourceType);
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        r2.add(new JLabel("Voltage (V):"));
        r2.add(voltageField);
        freqRow.add(new JLabel("Frequency (Hz):"));
        freqRow.add(freqField);
        sp.add(r1);
        sp.add(r2);
        sp.add(freqRow);
        sourceType.addActionListener(e -> updateSourceFields());
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, sp.getPreferredSize().height + 10));

        // add-element buttons
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        bp.setBorder(new TitledBorder("Add element (max " + MAX_ELEMENTS + ")"));
        bp.add(addR);
        bp.add(addC);
        bp.add(addL);
        addR.addActionListener(e -> addRow(Element.Type.RESISTOR));
        addC.addActionListener(e -> addRow(Element.Type.CAPACITOR));
        addL.addActionListener(e -> addRow(Element.Type.INDUCTOR));
        bp.setAlignmentX(LEFT_ALIGNMENT);
        bp.setMaximumSize(new Dimension(Integer.MAX_VALUE, bp.getPreferredSize().height + 10));

        // element rows
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        JPanel holder = new JPanel(new BorderLayout());
        holder.add(rowsPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(holder);
        scroll.setBorder(new TitledBorder("Elements"));
        scroll.setAlignmentX(LEFT_ALIGNMENT);

        // submit / reset
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton submit = new JButton("Submit");
        JButton reset = new JButton("Reset");
        submit.addActionListener(e -> submit());
        reset.addActionListener(e -> reset());
        actions.add(submit);
        actions.add(reset);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, actions.getPreferredSize().height));

        left.add(sp);
        left.add(Box.createVerticalStrut(6));
        left.add(bp);
        left.add(Box.createVerticalStrut(6));
        left.add(scroll);
        left.add(actions);
        return left;
    }

    private JPanel buildOutputPanel() {
        JPanel right = new JPanel(new BorderLayout(6, 6));
        diagram.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        right.add(diagram, BorderLayout.CENTER);

        table.setRowHeight(26);
        table.setEnabled(false);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(300, 112));

        reqLabel.setFont(reqLabel.getFont().deriveFont(Font.BOLD, 14f));
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        tableScroll.setAlignmentX(LEFT_ALIGNMENT);
        reqLabel.setAlignmentX(LEFT_ALIGNMENT);
        status.setAlignmentX(LEFT_ALIGNMENT);
        south.add(tableScroll);
        south.add(Box.createVerticalStrut(4));
        south.add(reqLabel);
        south.add(status);
        right.add(south, BorderLayout.SOUTH);
        return right;
    }

    private void updateSourceFields() {
        boolean ac = "AC".equals(sourceType.getSelectedItem());
        freqRow.setVisible(ac);
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------ element rows

    private void addRow(Element.Type type) {
        if (rows.size() >= MAX_ELEMENTS) {
            setStatus("A maximum of " + MAX_ELEMENTS + " elements can be added.", true);
            return;
        }
        counter++;
        Row row = new Row(type, type.prefix + counter);
        rows.add(row);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JLabel name = new JLabel(row.name);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setPreferredSize(new Dimension(30, 20));
        p.add(name);
        p.add(new JLabel(type.parameter + ":"));
        p.add(row.field);
        p.add(new JLabel(type.unit));
        rowsPanel.add(p);
        rowsPanel.revalidate();
        rowsPanel.repaint();
        setStatus(" ", false);
        row.field.requestFocusInWindow();
    }

    private void reset() {
        rows.clear();
        counter = 0;
        rowsPanel.removeAll();
        rowsPanel.revalidate();
        rowsPanel.repaint();
        diagram.clear();
        model.setDataVector(new Object[0][0], new Object[0]);
        reqLabel.setText(" ");
        setStatus(" ", false);
    }

    // ------------------------------------------------------------ submit

    private double parsePositive(String text, String what) throws InputException {
        double v;
        try {
            v = Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new InputException("Invalid number for " + what + ".");
        }
        if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0) {
            throw new InputException(what + " must be a positive number.");
        }
        return v;
    }

    private void submit() {
        try {
            boolean ac = "AC".equals(sourceType.getSelectedItem());
            double volt = parsePositive(voltageField.getText(), "source voltage");
            Source source = ac ? Source.ac(volt, parsePositive(freqField.getText(), "source frequency"))
                               : Source.dc(volt);
            if (rows.isEmpty()) throw new InputException("Please add at least one element.");

            List<Element> els = new ArrayList<>();
            for (Row r : rows) {
                els.add(new Element(r.type, r.name,
                        parsePositive(r.field.getText(), r.type.parameter.toLowerCase() + " of " + r.name)));
            }

            Analyzer.Result res = Analyzer.analyze(parallel, source, els);
            if (!res.ok()) {
                setStatus(res.error.replace('\n', ' '), true);
                JOptionPane.showMessageDialog(this, res.error, "Short circuit", JOptionPane.WARNING_MESSAGE);
                return;
            }
            diagram.setCircuit(parallel, source, els, res);
            fillTable(els, res);
            reqLabel.setText("Equivalent resistance: "
                    + (res.reqInfinite ? "\u221E" : res.req.toString()) + " \u03A9");
            setStatus(res.note == null ? " " : res.note, res.note != null);
        } catch (InputException ex) {
            setStatus(ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String cell(Complex c) {
        return c == null ? "N/A" : c.toString();
    }

    private void fillTable(List<Element> els, Analyzer.Result res) {
        int n = els.size();
        Object[] cols = new Object[n + 2];
        Object[][] data = new Object[3][n + 2];
        cols[0] = "";
        cols[n + 1] = "";
        data[0][0] = "U (Voltage)";
        data[1][0] = "I (Current intensity)";
        data[2][0] = "R (Resistance)";
        data[0][n + 1] = "V";
        data[1][n + 1] = "A";
        data[2][n + 1] = "\u03A9";
        for (int k = 0; k < n; k++) {
            cols[k + 1] = els.get(k).name;
            data[0][k + 1] = cell(res.u[k]);
            data[1][k + 1] = cell(res.i[k]);
            data[2][k + 1] = res.r[k] == null ? "\u221E" : res.r[k].toString();
        }
        model.setDataVector(data, cols);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 1; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(center);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
    }

    private void setStatus(String text, boolean error) {
        status.setText(text);
        status.setForeground(error ? new Color(0xB00020) : Color.DARK_GRAY);
    }
}