package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditDoctorDialog extends JDialog {
    private JTextField nomField, prenomField, specialiteField, telephoneField, emailField;
    private JButton saveButton;
    private int doctorId;

    public EditDoctorDialog(JFrame parent, int doctorId) {
        super(parent, "Modifier un Médecin", true);
        this.doctorId = doctorId;
        initComponents();
        loadDoctorData();
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
        saveButton = new JButton("Enregistrer les modifications");
        saveButton.addActionListener(e -> saveDoctor());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDoctorData() {
        String sql = "SELECT * FROM Medecin WHERE id_medecin = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nomField.setText(rs.getString("nom"));
                    prenomField.setText(rs.getString("prenom"));
                    specialiteField.setText(rs.getString("specialite"));
                    telephoneField.setText(rs.getString("telephone"));
                    emailField.setText(rs.getString("email"));
                } else {
                    JOptionPane.showMessageDialog(this, "Médecin non trouvé",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
                     "UPDATE Medecin SET nom=?, prenom=?, specialite=?, telephone=?, email=? WHERE id_medecin=?")) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, specialite);
            stmt.setString(4, telephone);
            stmt.setString(5, email);
            stmt.setInt(6, doctorId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Médecin modifié avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la modification du médecin");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
