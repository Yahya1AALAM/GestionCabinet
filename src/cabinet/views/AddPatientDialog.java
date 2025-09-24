package cabinet.views;

import com.toedter.calendar.JDateChooser; // Correction de l'import
import javax.swing.*;
import java.awt.*; // Correction de java.art en java.awt
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import cabinet.model.DatabaseUtil; // Correction de DataBaseUtil en DatabaseUtil

public class AddPatientDialog extends JDialog {
    // Correction des noms de variables
    private JTextField nomField, prenomField, cinField, telField, emailField;
    private JTextArea adresseArea;
    private JComboBox<String> sexeCombo;
    private JButton saveButton;
    private JDateChooser dateNaissanceChooser;

    public AddPatientDialog(JFrame parent) {
        super(parent, "Ajouter un nouveau patient", true);
        initComponents();
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

        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> savePatient());
        add(saveButton, BorderLayout.SOUTH);
    }

    private void savePatient() {
        if (!validatePatientFields()) {
            return;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String tel = telField.getText().trim();
        String email = emailField.getText().trim();
        String sexe = (String) sexeCombo.getSelectedItem();
        String adresse = adresseArea.getText().trim();

        // Récupération de la date de naissance
        java.sql.Date dateNaissance = null;
        if (dateNaissanceChooser.getDate() != null) {
            dateNaissance = new java.sql.Date(dateNaissanceChooser.getDate().getTime());
        }

        try {
            System.out.println("Tentative de sauvegarde du patient...");
            boolean success = DatabaseUtil.savePatient(
                    nom, prenom, cin, dateNaissance, tel, email, sexe, adresse
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Patient enregistré avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de l'enregistrement du patient");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Méthode de validation
    private boolean validatePatientFields() {
        // Validation du nom
        if (nomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire");
            nomField.requestFocus();
            return false;
        }

        // Validation du prénom
        if (prenomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le prénom est obligatoire");
            prenomField.requestFocus();
            return false;
        }

        // Validation du CIN
        if (cinField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le CIN est obligatoire");
            cinField.requestFocus();
            return false;
        }

        // Validation du téléphone
        String tel = telField.getText().trim();
        if (!tel.isEmpty() && !tel.matches("^[0-9+]{10,15}$")) {
            JOptionPane.showMessageDialog(this, "Numéro de téléphone invalide");
            telField.requestFocus();
            return false;
        }

        // Validation de l'email
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Email invalide");
            emailField.requestFocus();
            return false;
        }

        return true;
    }
}