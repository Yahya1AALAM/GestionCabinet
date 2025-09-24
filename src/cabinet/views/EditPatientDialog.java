package cabinet.views;

import com.toedter.calendar.JDateChooser;
import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditPatientDialog extends JDialog {
    private JTextField nomField, prenomField, cinField, telField, emailField;
    private JTextArea adresseArea;
    private JComboBox<String> sexeCombo;
    private JButton saveButton;
    private JDateChooser dateNaissanceChooser;
    private int patientId;

    public EditPatientDialog(JFrame parent, int patientId) {
        super(parent, "Modifier le patient", true);
        this.patientId = patientId;
        initComponents();
        loadPatientData();
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        formPanel.add(new JLabel("Nom:"));
        nomField = new JTextField();
        formPanel.add(nomField);

        formPanel.add(new JLabel("Prénom:"));
        prenomField = new JTextField();
        formPanel.add(prenomField);

        formPanel.add(new JLabel("CIN:"));
        cinField = new JTextField();
        formPanel.add(cinField);

        formPanel.add(new JLabel("Téléphone:"));
        telField = new JTextField();
        formPanel.add(telField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Sexe:"));
        sexeCombo = new JComboBox<>(new String[]{"M", "F"});
        formPanel.add(sexeCombo);

        formPanel.add(new JLabel("Date de naissance:"));
        dateNaissanceChooser = new JDateChooser();
        dateNaissanceChooser.setDateFormatString("yyyy-MM-dd");
        formPanel.add(dateNaissanceChooser);

        formPanel.add(new JLabel("Adresse:"));
        adresseArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(adresseArea));

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Enregistrer les modifications");
        saveButton.addActionListener(e -> savePatient());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPatientData() {
        String sql = "SELECT * FROM Patient WHERE id_patient = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nomField.setText(rs.getString("nom"));
                    prenomField.setText(rs.getString("prenom"));
                    cinField.setText(rs.getString("cin"));
                    telField.setText(rs.getString("telephone"));
                    emailField.setText(rs.getString("email"));

                    String sexe = rs.getString("sexe");
                    if (sexe != null) {
                        sexeCombo.setSelectedItem(sexe);
                    }

                    java.sql.Date dateNaiss = rs.getDate("date_naissance");
                    if (dateNaiss != null) {
                        dateNaissanceChooser.setDate(new java.util.Date(dateNaiss.getTime()));
                    }

                    adresseArea.setText(rs.getString("adresse"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void savePatient() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String tel = telField.getText().trim();
        String email = emailField.getText().trim();
        String sexe = (String) sexeCombo.getSelectedItem();
        String adresse = adresseArea.getText().trim();

        java.sql.Date dateNaissance = null;
        if (dateNaissanceChooser.getDate() != null) {
            dateNaissance = new java.sql.Date(dateNaissanceChooser.getDate().getTime());
        }

        try {
            boolean success = DatabaseUtil.updatePatient(
                    patientId, nom, prenom, cin, dateNaissance, tel, email, sexe, adresse
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Patient modifié avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la modification du patient");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
