package cabinet.views;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import cabinet.model.DatabaseUtil;


public class AddPatientDialog extends JDialog {
    // Champs existants
    private JTextField nomField, prenomField, cinField, telField, emailField;
    private JTextArea adresseArea;
    private JComboBox<String> sexeCombo;
    private JButton saveButton;
    private JDateChooser dateNaissanceChooser;

    // Nouveaux champs
    private JComboBox<String> groupeSanguinCombo, assuranceCombo, maladieChroniqueCombo;
    private JTextField secuSocialeField, contactUrgenceField, allergiesField, traitementField;
    private JTextArea antecedentsMedicauxArea, antecedentsChirurgicauxArea, motifConsultationArea;

    public AddPatientDialog(JFrame parent) {
        super(parent, "Ajouter un nouveau patient", true);
        initComponents();
        setSize(600, 700); // Augmentation de la taille pour accommoder les nouveaux champs
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Panel pour les informations de base
        JPanel infoBasiquePanel = createPanelTitre("Informations de base");
        infoBasiquePanel.setLayout(new GridLayout(8, 2, 5, 5));

        addField(infoBasiquePanel, "Nom:", nomField = new JTextField());
        addField(infoBasiquePanel, "Prénom:", prenomField = new JTextField());
        addField(infoBasiquePanel, "CIN:", cinField = new JTextField());
        addField(infoBasiquePanel, "Téléphone:", telField = new JTextField());
        addField(infoBasiquePanel, "Email:", emailField = new JTextField());

        infoBasiquePanel.add(new JLabel("Sexe:"));
        sexeCombo = new JComboBox<>(new String[]{"M", "F"});
        infoBasiquePanel.add(sexeCombo);

        infoBasiquePanel.add(new JLabel("Date de naissance:"));
        dateNaissanceChooser = new JDateChooser();
        dateNaissanceChooser.setDateFormatString("yyyy-MM-dd");
        infoBasiquePanel.add(dateNaissanceChooser);

        infoBasiquePanel.add(new JLabel("Adresse:"));
        adresseArea = new JTextArea(2, 20);
        infoBasiquePanel.add(new JScrollPane(adresseArea));

        // Panel pour les informations médicales
        JPanel infoMedicalPanel = createPanelTitre("Informations médicales");
        infoMedicalPanel.setLayout(new GridLayout(8, 2, 5, 5));

        infoMedicalPanel.add(new JLabel("Groupe sanguin:"));
        groupeSanguinCombo = new JComboBox<>(new String[]{"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        infoMedicalPanel.add(groupeSanguinCombo);

        infoMedicalPanel.add(new JLabel("Assurance:"));
        assuranceCombo = new JComboBox<>(new String[]{"", "CNSS", "CNOPS", "RAMED", "Assurance privée", "Autre"});
        infoMedicalPanel.add(assuranceCombo);

        addField(infoMedicalPanel, "Sécurité sociale:", secuSocialeField = new JTextField());
        addField(infoMedicalPanel, "Contact d'urgence:", contactUrgenceField = new JTextField());
        addField(infoMedicalPanel, "Allergies:", allergiesField = new JTextField());
        addField(infoMedicalPanel, "Traitement en cours:", traitementField = new JTextField());

        infoMedicalPanel.add(new JLabel("Maladie chronique:"));
        maladieChroniqueCombo = new JComboBox<>(new String[]{"", "Diabète", "Hypertension", "Asthme", "Cardiopathie", "Autre"});
        infoMedicalPanel.add(maladieChroniqueCombo);

        // Panel pour les antécédents et motif
        JPanel antecedentsPanel = createPanelTitre("Antécédents et motif de consultation");
        antecedentsPanel.setLayout(new GridLayout(3, 1, 5, 5));

        antecedentsPanel.add(new JLabel("Antécédents médicaux:"));
        antecedentsMedicauxArea = new JTextArea(3, 20);
        antecedentsPanel.add(new JScrollPane(antecedentsMedicauxArea));

        antecedentsPanel.add(new JLabel("Antécédents chirurgicaux:"));
        antecedentsChirurgicauxArea = new JTextArea(3, 20);
        antecedentsPanel.add(new JScrollPane(antecedentsChirurgicauxArea));

        antecedentsPanel.add(new JLabel("Motif de consultation:"));
        motifConsultationArea = new JTextArea(3, 20);
        antecedentsPanel.add(new JScrollPane(motifConsultationArea));

        // Assemblage des panels
        formPanel.add(infoBasiquePanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(infoMedicalPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(antecedentsPanel);

        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Bouton de sauvegarde
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> savePatient());
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createPanelTitre(String titre) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), titre));
        return panel;
    }

    private void addField(JPanel panel, String label, JComponent field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    private void savePatient() {
        if (!validatePatientFields()) {
            return;
        }

        // Récupération des données de base
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String tel = telField.getText().trim();
        String email = emailField.getText().trim();
        String sexe = (String) sexeCombo.getSelectedItem();
        String adresse = adresseArea.getText().trim();

        // Récupération des nouveaux champs
        String groupeSanguin = (String) groupeSanguinCombo.getSelectedItem();
        String assurance = (String) assuranceCombo.getSelectedItem();
        String secuSociale = secuSocialeField.getText().trim();
        String contactUrgence = contactUrgenceField.getText().trim();
        String allergies = allergiesField.getText().trim();
        String traitement = traitementField.getText().trim();
        String maladieChronique = (String) maladieChroniqueCombo.getSelectedItem();
        String antecedentsMedicaux = antecedentsMedicauxArea.getText().trim();
        String antecedentsChirurgicaux = antecedentsChirurgicauxArea.getText().trim();
        String motifConsultation = motifConsultationArea.getText().trim();

        // Récupération de la date de naissance
        java.sql.Date dateNaissance = null;
        if (dateNaissanceChooser.getDate() != null) {
            dateNaissance = new java.sql.Date(dateNaissanceChooser.getDate().getTime());
        }

        try {
            System.out.println("Tentative de sauvegarde du patient...");
            boolean success = DatabaseUtil.savePatient(
                    nom, prenom, cin, dateNaissance, tel, email, sexe, adresse,
                    groupeSanguin, assurance, secuSociale, contactUrgence, allergies,
                    traitement, maladieChronique, antecedentsMedicaux,
                    antecedentsChirurgicaux, motifConsultation
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

    private boolean validatePatientFields() {
        // Validation des champs obligatoires existants
        if (nomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire");
            nomField.requestFocus();
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le prénom est obligatoire");
            prenomField.requestFocus();
            return false;
        }

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

        // Validation du contact d'urgence
        String contactUrgence = contactUrgenceField.getText().trim();
        if (!contactUrgence.isEmpty() && !contactUrgence.matches("^[0-9+]{10,15}$")) {
            JOptionPane.showMessageDialog(this, "Numéro de contact d'urgence invalide");
            contactUrgenceField.requestFocus();
            return false;
        }

        return true;
    }
}