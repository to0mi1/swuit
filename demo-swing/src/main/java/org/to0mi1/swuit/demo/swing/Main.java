package org.to0mi1.swuit.demo.swing;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.to0mi1.swuit.demo.common.DemoPanels;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Swing Layouts Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(DemoPanels.createDemoPane());
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
