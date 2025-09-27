package cabinet.views;

import cabinet.model.DatabaseUtil;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddAppointmentDialog extends JDialog {
    private JComboBox<String> patientCombo, doctorCombo, testTypeCombo, statusCombo;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JButton saveButton;

    // Maps pour stocker la correspondance entre l'affichage et les IDs
    private Map<String, Integer> patientMap;
    private Map<String, Integer> doctorMap;
    private Map<String, Integer> testTypeMap;

    // Constructeur existant (pour création manuelle de RDV)
    public AddAppointmentDialog(JFrame parent) {
        super(parent, "Nouveau Rendez-vous", true);
        patientMap = new HashMap<>();
        doctorMap = new HashMap<>();
        testTypeMap = new HashMap<>();
        initComponents();
        loadComboBoxData();
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    // NOUVEAU CONSTRUCTEUR pour ouverture avec patient pré-sélectionné
    public AddAppointmentDialog(JFrame parent, int patientId, String patientName) {
        super(parent, "Nouveau Rendez-vous pour " + patientName, true);
        patientMap = new HashMap<>();
        doctorMap = new HashMap<>();
        testTypeMap = new HashMap<>();
        initComponents();
        loadComboBoxData();
        preselectPatient(patientId, patientName); // Pré-sélectionner le patient
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        formPanel.add(new JLabel("Patient:"));
        patientCombo = new JComboBox<>();
        formPanel.add(patientCombo);

        formPanel.add(new JLabel("Médecin:"));
        doctorCombo = new JComboBox<>();
        formPanel.add(doctorCombo);

        formPanel.add(new JLabel("Date:"));
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        formPanel.add(dateChooser);

        formPanel.add(new JLabel("Heure:"));
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        formPanel.add(timeSpinner);

        formPanel.add(new JLabel("Type de test:"));
        testTypeCombo = new JComboBox<>();
        testTypeCombo.addItem(""); // Option vide
        formPanel.add(testTypeCombo);

        formPanel.add(new JLabel("Statut:"));
        statusCombo = new JComboBox<>(new String[]{"Prévu", "Confirmé", "Annulé", "Passé"});
        formPanel.add(statusCombo);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> saveAppointment());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadComboBoxData() {
        // Vider les ComboBox avant de les remplir
        patientCombo.removeAllItems();
        doctorCombo.removeAllItems();
        testTypeCombo.removeAllItems();

        // Vider les maps
        patientMap.clear();
        doctorMap.clear();
        testTypeMap.clear();

        // Charger les patients
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_patient, nom, prenom FROM Patient ORDER BY nom, prenom")) {

            while (rs.next()) {
                String displayText = rs.getString("nom") + " " + rs.getString("prenom");
                int patientId = rs.getInt("id_patient");
                patientCombo.addItem(displayText);
                patientMap.put(displayText, patientId);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des patients: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        // Charger les médecins
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_medecin, nom, prenom FROM Medecin ORDER BY nom, prenom")) {

            while (rs.next()) {
                String displayText = rs.getString("nom") + " " + rs.getString("prenom");
                int doctorId = rs.getInt("id_medecin");
                doctorCombo.addItem(displayText);
                doctorMap.put(displayText, doctorId);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des médecins: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        // Charger les types de test
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_type_test, nom FROM TypeTest ORDER BY nom")) {

            // Ajouter une option vide pour "Aucun test"
            testTypeCombo.addItem("Aucun test");
            testTypeMap.put("Aucun test", null);

            while (rs.next()) {
                String displayText = rs.getString("nom");
                int testTypeId = rs.getInt("id_type_test");
                testTypeCombo.addItem(displayText);
                testTypeMap.put(displayText, testTypeId);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des types de test: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Méthode pour pré-sélectionner un patient
    private void preselectPatient(int patientId, String patientName) {
        // Rechercher le patient dans la map
        for (Map.Entry<String, Integer> entry : patientMap.entrySet()) {
            if (entry.getValue() == patientId) {
                patientCombo.setSelectedItem(entry.getKey());
                break;
            }
        }

        // Désactiver la sélection du patient puisque c'est déjà choisi
        patientCombo.setEnabled(false);
    }

    private void saveAppointment() {
        if (patientCombo.getSelectedItem() == null || doctorCombo.getSelectedItem() == null ||
                dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs obligatoires",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Récupérer les IDs à partir des maps
        String patientDisplay = (String) patientCombo.getSelectedItem();
        Integer patientId = patientMap.get(patientDisplay);

        String doctorDisplay = (String) doctorCombo.getSelectedItem();
        Integer doctorId = doctorMap.get(doctorDisplay);

        if (patientId == null || doctorId == null) {
            JOptionPane.showMessageDialog(this, "Erreur: impossible de trouver l'ID du patient ou du médecin",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Date et heure
        java.util.Date date = dateChooser.getDate();
        java.util.Date time = (java.util.Date) timeSpinner.getValue();

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        Calendar combined = Calendar.getInstance();
        combined.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), dateCal.get(Calendar.DAY_OF_MONTH),
                timeCal.get(Calendar.HOUR_OF_DAY), timeCal.get(Calendar.MINUTE));

        Timestamp dateTime = new Timestamp(combined.getTimeInMillis());

        // Type de test (optionnel)
        Integer testTypeId = null;
        if (testTypeCombo.getSelectedItem() != null && !testTypeCombo.getSelectedItem().equals("Aucun test")) {
            String testDisplay = (String) testTypeCombo.getSelectedItem();
            testTypeId = testTypeMap.get(testDisplay);
        }

        String status = (String) statusCombo.getSelectedItem();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO RendezVous (patient_id, medecin_id, date_heure_debut, statut, type_test_id) " +
                             "VALUES (?, ?, ?, ?, ?)")) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setTimestamp(3, dateTime);
            stmt.setString(4, status);

            if (testTypeId != null) {
                stmt.setInt(5, testTypeId);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Rendez-vous créé avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la création du rendez-vous");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}