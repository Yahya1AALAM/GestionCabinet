package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddDoctorDialog extends JDialog {
    private JTextField nomField, prenomField, specialiteField, telephoneField, emailField;
    private JButton saveButton;

    public AddDoctorDialog(JFrame parent) {
        super(parent, "Ajouter un Médecin", true);
        initComponents();
        setSize(400, 300);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        formPanel.add(new JLabel("Nom:"));
        nomField = new JTextField();
        formPanel.add(nomField);

        formPanel.add(new JLabel("Prénom:"));
        prenomField = new JTextField();
        formPanel.add(prenomField);

        formPanel.add(new JLabel("Spécialité:"));
        specialiteField = new JTextField();
        formPanel.add(specialiteField);

        formPanel.add(new JLabel("Téléphone:"));
        telephoneField = new JTextField();
        formPanel.add(telephoneField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> saveDoctor());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveDoctor() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String specialite = specialiteField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom et le prénom sont obligatoires",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Medecin (nom, prenom, specialite, telephone, email) VALUES (?, ?, ?, ?, ?)")) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, specialite);
            stmt.setString(4, telephone);
            stmt.setString(5, email);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Médecin ajouté avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de l'ajout du médecin");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
