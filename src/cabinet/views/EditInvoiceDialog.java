package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class EditInvoiceDialog extends JDialog {
    private JTextField numeroField, montantField;
    private JComboBox<String> statutCombo, modePaiementCombo;
    private int factureId;

    public EditInvoiceDialog(JFrame parent, int factureId) {
        super(parent, "Modifier Facture", true);
        this.factureId = factureId;
        initComponents();
        loadFactureData();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Numéro de facture
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Numéro:"), gbc);

        gbc.gridx = 1;
        numeroField = new JTextField(20);
        numeroField.setEditable(false);
        add(numeroField, gbc);

        // Montant
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Montant (DH):"), gbc);

        gbc.gridx = 1;
        montantField = new JTextField(20);
        add(montantField, gbc);

        // Statut - Utiliser les valeurs de l'ENUM
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Statut:"), gbc);

        gbc.gridx = 1;
        statutCombo = new JComboBox<>(new String[]{"Paye", "En attente", "Partiel"});
        add(statutCombo, gbc);

        // Mode de paiement - Utiliser les valeurs de l'ENUM
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Mode de paiement:"), gbc);

        gbc.gridx = 1;
        modePaiementCombo = new JComboBox<>(new String[]{"Espece", "Carte", "Autre"});
        add(modePaiementCombo, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(this::saveFacture);
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, gbc);
    }

    private void loadFactureData() {
        String sql = "SELECT * FROM Facture WHERE id_facture = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, factureId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    numeroField.setText(rs.getString("numero"));
                    montantField.setText(String.valueOf(rs.getDouble("montant_total")));
                    statutCombo.setSelectedItem(rs.getString("statut_paiement"));
                    modePaiementCombo.setSelectedItem(rs.getString("mode_paiement"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveFacture(ActionEvent e) {
        // Validation des données
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

        String sql = "UPDATE Facture SET montant_total = ?, statut_paiement = ?, " +
                "mode_paiement = ? WHERE id_facture = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, Double.parseDouble(montantField.getText()));
            stmt.setString(2, (String) statutCombo.getSelectedItem());
            stmt.setString(3, (String) modePaiementCombo.getSelectedItem());
            stmt.setInt(4, factureId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Facture modifiée avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Échec de la modification",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de sauvegarde: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}