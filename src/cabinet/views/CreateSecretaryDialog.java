package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CreateSecretaryDialog extends JDialog {
    private final int createdBy; // ID du médecin qui crée le compte
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public CreateSecretaryDialog(JFrame parent, int createdBy) {
        super(parent, "Créer un compte secrétaire", true);
        this.createdBy = createdBy;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(400, 250);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Champ nom d'utilisateur
        formPanel.add(new JLabel("Nom d'utilisateur:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        // Champ mot de passe
        formPanel.add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        // Confirmation mot de passe
        formPanel.add(new JLabel("Confirmer mot de passe:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        add(formPanel, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton("Créer");
        JButton cancelButton = new JButton("Annuler");

        createButton.addActionListener(e -> createSecretaryAccount());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createSecretaryAccount() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Vérifier si l'utilisateur existe déjà
            String checkSql = "SELECT COUNT(*) FROM utilisateur WHERE nom_utilisateur = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Ce nom d'utilisateur existe déjà", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Créer le compte
            String insertSql = "INSERT INTO utilisateur (nom_utilisateur, mot_de_passe, role, created_by) VALUES (?, SHA2(?, 256), 'Secretaire', ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setInt(3, createdBy);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Compte secrétaire créé avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la création du compte", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de base de données: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
