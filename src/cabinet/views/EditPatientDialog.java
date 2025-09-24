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

    // Nouveaux champs
    private JComboBox<String> groupeSanguinCombo, assuranceCombo, maladieChroniqueCombo;
    private JTextField secuSocialeField, contactUrgenceField, allergiesField, traitementField;
    private JTextArea antecedentsMedicauxArea, antecedentsChirurgicauxArea, motifConsultationArea;

    public EditPatientDialog(JFrame parent, int patientId) {
        super(parent, "Modifier le patient", true);
        this.patientId = patientId;
        initComponents();
        loadPatientData();
        setSize(600, 700);
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

        // Boutons
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Enregistrer les modifications");
        saveButton.addActionListener(e -> savePatient());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

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

    private void loadPatientData() {
        String sql = "SELECT * FROM patient WHERE id_patient = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Informations de base
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

                    // Nouveaux champs médicaux
                    String groupeSanguin = rs.getString("groupe_sanguin");
                    if (groupeSanguin != null) {
                        groupeSanguinCombo.setSelectedItem(groupeSanguin);
                    }

                    String assurance = rs.getString("assurance");
                    if (assurance != null) {
                        assuranceCombo.setSelectedItem(assurance);
                    }

                    secuSocialeField.setText(rs.getString("securite_sociale"));
                    contactUrgenceField.setText(rs.getString("contact_urgence"));
                    allergiesField.setText(rs.getString("allergies"));
                    traitementField.setText(rs.getString("traitement_cours"));

                    String maladieChronique = rs.getString("maladie_chronique");
                    if (maladieChronique != null) {
                        maladieChroniqueCombo.setSelectedItem(maladieChronique);
                    }

                    antecedentsMedicauxArea.setText(rs.getString("antecedents_medicaux"));
                    antecedentsChirurgicauxArea.setText(rs.getString("antecedents_chirurgicaux"));
                    motifConsultationArea.setText(rs.getString("motif_consultation"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
            boolean success = updatePatientWithAllFields(
                    patientId, nom, prenom, cin, dateNaissance, tel, email, sexe, adresse,
                    groupeSanguin, assurance, secuSociale, contactUrgence, allergies,
                    traitement, maladieChronique, antecedentsMedicaux,
                    antecedentsChirurgicaux, motifConsultation
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

    private boolean updatePatientWithAllFields(int id, String nom, String prenom, String cin,
                                               java.sql.Date dateNaissance, String tel, String email,
                                               String sexe, String adresse, String groupeSanguin,
                                               String assurance, String secuSociale, String contactUrgence,
                                               String allergies, String traitement, String maladieChronique,
                                               String antecedentsMedicaux, String antecedentsChirurgicaux,
                                               String motifConsultation) throws SQLException {

        String sql = "UPDATE patient SET nom=?, prenom=?, cin=?, date_naissance=?, telephone=?, " +
                "email=?, sexe=?, adresse=?, groupe_sanguin=?, assurance=?, securite_sociale=?, " +
                "contact_urgence=?, allergies=?, traitement_cours=?, maladie_chronique=?, " +
                "antecedents_medicaux=?, antecedents_chirurgicaux=?, motif_consultation=? " +
                "WHERE id_patient=?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, cin);

            if (dateNaissance != null) {
                stmt.setDate(4, dateNaissance);
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, tel);
            stmt.setString(6, email);
            stmt.setString(7, sexe);
            stmt.setString(8, adresse);
            stmt.setString(9, groupeSanguin);
            stmt.setString(10, assurance);
            stmt.setString(11, secuSociale);
            stmt.setString(12, contactUrgence);
            stmt.setString(13, allergies);
            stmt.setString(14, traitement);
            stmt.setString(15, maladieChronique);
            stmt.setString(16, antecedentsMedicaux);
            stmt.setString(17, antecedentsChirurgicaux);
            stmt.setString(18, motifConsultation);
            stmt.setInt(19, id);

            return stmt.executeUpdate() > 0;
        }
    }

    private boolean validatePatientFields() {
        // Validation des champs obligatoires
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