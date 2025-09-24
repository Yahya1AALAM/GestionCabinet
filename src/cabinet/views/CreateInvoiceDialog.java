package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;

public class CreateInvoiceDialog extends JDialog {
    private JComboBox<String> appointmentCombo;
    private JTextField amountField, invoiceNumberField;
    private JComboBox<String> paymentMethodCombo;
    private JButton saveButton;

    public CreateInvoiceDialog(JFrame parent) {
        super(parent, "Créer une facture", true);
        initComponents();
        loadAppointments();
        setSize(500, 300);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        formPanel.add(new JLabel("Rendez-vous:"));
        appointmentCombo = new JComboBox<>();
        formPanel.add(appointmentCombo);

        formPanel.add(new JLabel("Numéro de facture:"));
        invoiceNumberField = new JTextField();
        formPanel.add(invoiceNumberField);

        formPanel.add(new JLabel("Montant:"));
        amountField = new JTextField();
        formPanel.add(amountField);

        formPanel.add(new JLabel("Mode de paiement:"));
        paymentMethodCombo = new JComboBox<>(new String[]{"Espece", "Carte", "Autre"});
        formPanel.add(paymentMethodCombo);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> saveInvoice());
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAppointments() {
        appointmentCombo.removeAllItems();

        String sql = "SELECT r.id_rdv, p.nom, p.prenom, r.date_heure_debut " +
                "FROM RendezVous r " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "WHERE r.id_rdv NOT IN (SELECT rendezvous_id FROM Facture WHERE rendezvous_id IS NOT NULL) " +
                "ORDER BY r.date_heure_debut DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String appointmentInfo = "RDV #" + rs.getInt("id_rdv") + " - " +
                        rs.getString("nom") + " " + rs.getString("prenom") + " - " +
                        rs.getTimestamp("date_heure_debut");
                appointmentCombo.addItem(appointmentInfo);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des rendez-vous: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveInvoice() {
        if (appointmentCombo.getSelectedItem() == null ||
                invoiceNumberField.getText().isEmpty() ||
                amountField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs obligatoires",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extraire l'ID du rendez-vous
        String appointmentItem = (String) appointmentCombo.getSelectedItem();
        int appointmentId = Integer.parseInt(appointmentItem.substring(
                appointmentItem.indexOf("#") + 1,
                appointmentItem.indexOf(" - ")
        ));

        String invoiceNumber = invoiceNumberField.getText();
        double amount;

        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Montant invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Facture (rendezvous_id, numero, montant_total, mode_paiement) VALUES (?, ?, ?, ?)")) {

            stmt.setInt(1, appointmentId);
            stmt.setString(2, invoiceNumber);
            stmt.setDouble(3, amount);
            stmt.setString(4, paymentMethod);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Facture créée avec succès");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la création de la facture");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
