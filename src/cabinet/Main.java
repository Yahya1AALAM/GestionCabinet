package cabinet;

import com.formdev.flatlaf.FlatLightLaf;
import cabinet.views.LoginView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Appliquer FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Gestion des erreurs non attrapées
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Erreur critique: " + throwable.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        });

        java.awt.EventQueue.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}