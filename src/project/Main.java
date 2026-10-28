package project;
import javax.swing.*;
import java.awt.Dimension;
import java.util.Locale;

/** Electrical circuit simulator: entry point. */
public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            JFrame frame = new JFrame("Electrical Circuit Simulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Parallel Circuit", new CircuitTab(true));
            tabs.addTab("Series Circuit", new CircuitTab(false));

            frame.setContentPane(tabs);
            frame.setMinimumSize(new Dimension(900, 560));
            frame.setSize(1050, 640);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}