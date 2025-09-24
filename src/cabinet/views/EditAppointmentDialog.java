package cabinet.views;

import cabinet.model.DatabaseUtil;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.function.Function;

public class EditAppointmentDialog extends JDialog {
    private JComboBox<String> patientCombo, doctorCombo, testTypeCombo, statusCombo;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JButton saveButton;
    private int appointmentId;

    public EditAppointmentDialog(JFrame parent, int appointmentId) {
        super(parent, "Modifier Rendez-vous", true);
        this.appointmentId = appointmentId;
        initComponents();
        loadComboBoxData();
        loadAppointmentData();
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
        saveButton = new JButton("Enregistrer les modifications");
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

        // Ajouter l'option "Aucun test" en premier
        testTypeCombo.addItem("Aucun test");

        // Charger les patients
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_patient, nom, prenom FROM Patient ORDER BY nom, prenom")) {

            while (rs.next()) {
                String patientInfo = rs.getString("nom") + " " + rs.getString("prenom") + " (ID:" + rs.getInt("id_patient") + ")";
                patientCombo.addItem(patientInfo);
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
                String doctorInfo = rs.getString("nom") + " " + rs.getString("prenom") + " (ID:" + rs.getInt("id_medecin") + ")";
                doctorCombo.addItem(doctorInfo);
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

            while (rs.next()) {
                String testInfo = rs.getString("nom") + " (ID:" + rs.getInt("id_type_test") + ")";
                testTypeCombo.addItem(testInfo);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des types de test: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadAppointmentData() {
        String sql = "SELECT r.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
                "m.nom as medecin_nom, m.prenom as medecin_prenom, " +
                "t.nom as type_test_nom, t.id_type_test " +  // Ajouter l'ID du type de test
                "FROM RendezVous r " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "JOIN Medecin m ON r.medecin_id = m.id_medecin " +
                "LEFT JOIN TypeTest t ON r.type_test_id = t.id_type_test " +
                "WHERE r.id_rdv = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Charger les données du rendez-vous dans le formulaire

                    // Patient
                    String patientInfo = rs.getString("patient_nom") + " " +
                            rs.getString("patient_prenom") +
                            " (ID:" + rs.getInt("patient_id") + ")";

                    // Trouver l'élément correspondant dans le ComboBox
                    for (int i = 0; i < patientCombo.getItemCount(); i++) {
                        if (patientCombo.getItemAt(i).equals(patientInfo)) {
                            patientCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    // Médecin
                    String doctorInfo = rs.getString("medecin_nom") + " " +
                            rs.getString("medecin_prenom") +
                            " (ID:" + rs.getInt("medecin_id") + ")";

                    for (int i = 0; i < doctorCombo.getItemCount(); i++) {
                        if (doctorCombo.getItemAt(i).equals(doctorInfo)) {
                            doctorCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    // Date et heure
                    Timestamp dateHeure = rs.getTimestamp("date_heure_debut");
                    if (dateHeure != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateHeure);

                        // Date
                        dateChooser.setDate(cal.getTime());

                        // Heure
                        timeSpinner.setValue(cal.getTime());
                    }

                    // Type de test
                    Integer testTypeId = rs.getInt("type_test_id");
                    if (!rs.wasNull() && testTypeId > 0) {
                        String testTypeName = rs.getString("type_test_nom");
                        String testInfo = testTypeName + " (ID:" + testTypeId + ")";

                        for (int i = 0; i < testTypeCombo.getItemCount(); i++) {
                            if (testTypeCombo.getItemAt(i).equals(testInfo)) {
                                testTypeCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    } else {
                        testTypeCombo.setSelectedIndex(0); // Sélectionner "Aucun test"
                    }

                    // Statut
                    String status = rs.getString("statut");
                    statusCombo.setSelectedItem(status);
                } else {
                    JOptionPane.showMessageDialog(this, "Rendez-vous non trouvé",
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

    private void saveAppointment() {
        // Vérification des champs obligatoires
        if (patientCombo.getSelectedItem() == null || doctorCombo.getSelectedItem() == null ||
                dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs obligatoires",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Méthode utilitaire pour extraire l'ID d'un élément de ComboBox
        Function<String, Integer> extractId = item -> {
            try {
                int startIndex = item.lastIndexOf("(ID:") + 4;
                int endIndex = item.lastIndexOf(")");
                if (startIndex > 0 && endIndex > startIndex) {
                    String idStr = item.substring(startIndex, endIndex);
                    return Integer.parseInt(idStr.trim());
                }
            } catch (Exception e) {
                // En cas d'erreur, retourner null
            }
            return null;
        };

        // Extraire les IDs des combobox
        String patientItem = (String) patientCombo.getSelectedItem();
        Integer patientId = extractId.apply(patientItem);
        if (patientId == null) {
            JOptionPane.showMessageDialog(this, "Format de patient invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String doctorItem = (String) doctorCombo.getSelectedItem();
        Integer doctorId = extractId.apply(doctorItem);
        if (doctorId == null) {
            JOptionPane.showMessageDialog(this, "Format de médecin invalide",
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
        combined.set(dateCal.get(Calendar.YEAR),
                dateCal.get(Calendar.MONTH),
                dateCal.get(Calendar.DAY_OF_MONTH),
                timeCal.get(Calendar.HOUR_OF_DAY),
                timeCal.get(Calendar.MINUTE),
                0); // Secondes à 0

        Timestamp dateTime = new Timestamp(combined.getTimeInMillis());

        // Type de test (optionnel)
        Integer testTypeId = null;
        Object selectedTest = testTypeCombo.getSelectedItem();
        if (selectedTest != null && !selectedTest.toString().equals("Aucun test") &&
                !selectedTest.toString().isEmpty()) {
            String testItem = selectedTest.toString();
            testTypeId = extractId.apply(testItem);
            if (testTypeId == null) {
                JOptionPane.showMessageDialog(this, "Format de type de test invalide",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String status = (String) statusCombo.getSelectedItem();

        // Utilisation de la requête UPDATE
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE RendezVous SET patient_id=?, medecin_id=?, date_heure_debut=?, statut=?, type_test_id=? " +
                             "WHERE id_rdv=?")) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setTimestamp(3, dateTime);
            stmt.setString(4, status);

            if (testTypeId != null) {
                stmt.setInt(5, testTypeId);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            // Ajouter l'ID du rendez-vous comme dernier paramètre
            stmt.setInt(6, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Rendez-vous modifié avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la modification du rendez-vous");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    }


    private int extractIdFromComboItem(String item) {
        try {
            int startIndex = item.lastIndexOf("(ID:") + 4;
            int endIndex = item.lastIndexOf(")");
            if (startIndex > 0 && endIndex > startIndex) {
                String idStr = item.substring(startIndex, endIndex);
                return Integer.parseInt(idStr.trim());
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner -1 ou lancer une exception selon vos besoins
        }
        return -1; // ou throw new IllegalArgumentException("Format d'ID invalide");
    }
}
