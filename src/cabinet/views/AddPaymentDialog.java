package cabinet.views;

import cabinet.model.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPaymentDialog extends JDialog {
    private JTextField montantField;
    private JComboBox<String> factureCombo, modePaiementCombo, statutCombo;
    private JTextArea notesArea;
    private Map<String, Integer> factureMap; // Texte affiché -> ID Facture

    public AddPaymentDialog(JFrame parent) {
        super(parent, "Nouveau Paiement", true);
        factureMap = new HashMap<>();
        initComponents();
        loadInvoices();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        factureCombo = new JComboBox<>();
        modePaiementCombo = new JComboBox<>(new String[]{"Espece", "Carte", "Autre"});
        statutCombo = new JComboBox<>(new String[]{"Complet", "Partiel"});
        montantField = new JTextField();
        notesArea = new JTextArea(3, 20);

        formPanel.add(new JLabel("Facture :"));
        formPanel.add(factureCombo);
        formPanel.add(new JLabel("Montant :"));
        formPanel.add(montantField);
        formPanel.add(new JLabel("Mode de paiement :"));
        formPanel.add(modePaiementCombo);
        formPanel.add(new JLabel("Statut :"));
        formPanel.add(statutCombo);
        formPanel.add(new JLabel("Notes :"));
        formPanel.add(new JScrollPane(notesArea));

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(this::savePayment);
        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /** Charger les factures non payées dans la combo */
    private void loadInvoices() {
        factureCombo.removeAllItems();
        factureMap.clear();

        String sql = "SELECT f.id_facture, f.numero, p.nom, p.prenom, f.montant_total, f.statut_paiement " +
                "FROM Facture f " +
                "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "WHERE f.statut_paiement != 'Paye' " +
                "ORDER BY f.date_emission DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);

            while (rs.next()) {
                String displayText = rs.getString("numero") + " - " +
                        rs.getString("nom") + " " + rs.getString("prenom") + " - " +
                        nf.format(rs.getDouble("montant_total")) + " - " +
                        rs.getString("statut_paiement");

                int factureId = rs.getInt("id_facture");

                factureCombo.addItem(displayText);
                factureMap.put(displayText, factureId);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des factures: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /** Vérifie que les champs sont valides */
    private boolean validateForm() {
        if (factureCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une facture",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            double montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                JOptionPane.showMessageDialog(this, "Le montant doit être positif",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Montant invalide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /** Enregistrer le paiement */
    private void savePayment(ActionEvent e) {
        if (!validateForm()) return;

        String selectedText = (String) factureCombo.getSelectedItem();
        Integer factureId = factureMap.get(selectedText);
        if (factureId == null) {
            JOptionPane.showMessageDialog(this, "Erreur: impossible de trouver l'ID de la facture",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double montant = Double.parseDouble(montantField.getText());
        String modePaiement = (String) modePaiementCombo.getSelectedItem();
        String notes = notesArea.getText();

        // Vérifier que le montant ne dépasse pas le montant restant de la facture
        double montantTotalFacture = getFactureMontant(factureId);
        double totalDejaPaye = getTotalPaid(factureId);
        double montantRestant = montantTotalFacture - totalDejaPaye;

        if (montant > montantRestant) {
            JOptionPane.showMessageDialog(this,
                    "Le montant saisi (" + montant + " DH) dépasse le montant restant à payer: " + montantRestant + " DH",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Déterminer automatiquement le statut
        String statutPaiement;
        double nouveauTotalPaye = totalDejaPaye + montant;

        if (Math.abs(nouveauTotalPaye - montantTotalFacture) < 0.01) { // Tolérance pour les calculs flottants
            statutPaiement = "Complet";
        } else {
            statutPaiement = "Partiel";
        }

        if (insertPayment(factureId, montant, modePaiement, statutPaiement, notes)) {
            // Mettre à jour le statut de la facture
            if ("Complet".equals(statutPaiement)) {
                updateInvoiceStatus(factureId, "Paye");
            } else {
                updateInvoiceStatus(factureId, "Partiel");
            }

            JOptionPane.showMessageDialog(this, "Paiement enregistré avec succès",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    /** Insère le paiement en base */
    private boolean insertPayment(int factureId, double montant, String mode, String statut, String notes) {
        String sql = "INSERT INTO Paiement (facture_id, montant, date_paiement, mode_paiement, statut, notes) " +
                "VALUES (?, ?, NOW(), ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, factureId);
            stmt.setDouble(2, montant);
            stmt.setString(3, mode);
            stmt.setString(4, statut);
            stmt.setString(5, notes);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de sauvegarde: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }

    /** Récupère le montant total payé pour une facture */
    private double getTotalPaid(int factureId) {
        String sql = "SELECT COALESCE(SUM(montant), 0) as total FROM Paiement WHERE facture_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, factureId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de calcul du total payé: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return 0.0;
    }

    /** Récupère le montant total d'une facture */
    private double getFactureMontant(int factureId) {
        String sql = "SELECT montant_total FROM Facture WHERE id_facture = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, factureId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("montant_total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    /** Met à jour le statut d'une facture */
    private void updateInvoiceStatus(int factureId, String statut) {
        String sql = "UPDATE Facture SET statut_paiement = ? WHERE id_facture = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, factureId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}