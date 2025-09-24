package cabinet.views;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import cabinet.model.DatabaseUtil;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView() {
        initComponents();
        setLocationRelativeTo(null); // Centrer la fenêtre
    }

    private void initComponents() {
        setTitle("Connexion - Cabinet Médical");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 10));
        formPanel.add(new JLabel("Nom d'utilisateur:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginButton = new JButton("Connexion");
        JButton cancelButton = new JButton("Annuler");

        loginButton.addActionListener(e -> authenticate());
        cancelButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(cancelButton);
        buttonPanel.add(loginButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Enter key pour soumettre le formulaire
        getRootPane().setDefaultButton(loginButton);
    }

    private void authenticate() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un nom d'utilisateur",
                    "Champ requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un mot de passe",
                    "Champ requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM utilisateur WHERE nom_utilisateur = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("mot_de_passe");
                        String inputHash = DatabaseUtil.sha256(new String(password));

                        if (inputHash.equals(storedHash)) {
                            String role = rs.getString("role");
                            int userId = rs.getInt("id_utilisateur");
                            openDashboard(role, userId);
                        } else {
                            showError("Mot de passe incorrect");
                        }
                    } else {
                        showError("Utilisateur non trouvé");
                    }
                }
            }
        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Arrays.fill(password, '0');
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Erreur d'authentification", JOptionPane.ERROR_MESSAGE);
        passwordField.setText("");
        passwordField.requestFocus();
    }

    private void openDashboard(String role, int userId) {
        JOptionPane.showMessageDialog(this,
                "Connexion réussie en tant que " + role,
                "Bienvenue", JOptionPane.INFORMATION_MESSAGE);

        dispose();

        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard(role, userId);
            dashboard.setVisible(true);
        });
    }
}
