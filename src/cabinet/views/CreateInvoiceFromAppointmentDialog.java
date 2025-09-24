package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class CreateInvoiceFromAppointmentDialog extends JDialog {
    private JTextField numeroField, montantField;
    private JComboBox<String> modePaiementCombo;
    private int appointmentId;

    public CreateInvoiceFromAppointmentDialog(JFrame parent, int appointmentId) {
        super(parent, "Créer Facture à partir du Rendez-vous", true);
        this.appointmentId = appointmentId;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Numéro de facture (saisie manuelle)
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Numéro:"), gbc);

        gbc.gridx = 1;
        numeroField = new JTextField(20);
        add(numeroField, gbc);

        // Montant
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Montant (DH):"), gbc);

        gbc.gridx = 1;
        montantField = new JTextField(20);
        add(montantField, gbc);

        // Mode de paiement - Utiliser les valeurs de l'ENUM
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Mode de paiement:"), gbc);

        gbc.gridx = 1;
        modePaiementCombo = new JComboBox<>(new String[]{"Espece", "Carte", "Autre"});
        add(modePaiementCombo, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton saveButton = new JButton("Créer la facture");
        saveButton.addActionListener(this::createFacture);
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, gbc);
    }

    private void createFacture(ActionEvent e) {
        // Validation du numéro de facture
        if (numeroField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un numéro de facture",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si le numéro de facture existe déjà
        if (isInvoiceNumberExists(numeroField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Ce numéro de facture existe déjà",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validation du montant
        try {
            double montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                JOptionPane.showMessageDialog(this, "Le montant doit être positif",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Montant invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Facture (numero, date_emission, montant_total, statut_paiement, " +
                "mode_paiement, rendezvous_id) VALUES (?, NOW(), ?, 'En attente', ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroField.getText().trim());
            stmt.setDouble(2, Double.parseDouble(montantField.getText()));
            stmt.setString(3, (String) modePaiementCombo.getSelectedItem());
            stmt.setInt(4, appointmentId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Facture créée avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la création de la facture",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de création: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean isInvoiceNumberExists(String numero) {
        String sql = "SELECT COUNT(*) FROM Facture WHERE numero = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de vérification: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        return false;
    }
}