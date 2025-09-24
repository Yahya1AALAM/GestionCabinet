package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ViewInvoiceDialog extends JDialog {
    private JTextArea invoiceArea;

    public ViewInvoiceDialog(JFrame parent, String invoiceNumber) {
        super(parent, "Facture: " + invoiceNumber, true);
        initComponents();
        loadInvoiceData(invoiceNumber);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(600, 500);

        invoiceArea = new JTextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        add(new JScrollPane(invoiceArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadInvoiceData(String invoiceNumber) {
        String sql = "SELECT f.*, p.nom, p.prenom, p.cin, p.telephone, " +
                "m.nom as medecin_nom, m.prenom as medecin_prenom, " +
                "r.date_heure_debut, t.nom as type_test " +
                "FROM Facture f " +
                "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "JOIN Medecin m ON r.medecin_id = m.id_medecin " +
                "LEFT JOIN TypeTest t ON r.type_test_id = t.id_type_test " +
                "WHERE f.numero = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, invoiceNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String invoiceContent = generateInvoiceContent(rs);
                    invoiceArea.setText(invoiceContent);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement de la facture: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String generateInvoiceContent(ResultSet rs) throws SQLException {
        StringBuilder content = new StringBuilder();

        // En-tête de la facture
        content.append("========================================\n");
        content.append("           CABINET MÉDICAL\n");
        content.append("========================================\n\n");

        // Informations de la facture
        content.append("Facture N°: ").append(rs.getString("numero")).append("\n");
        content.append("Date: ").append(rs.getDate("date_emission")).append("\n");
        content.append("Médecin: ").append(rs.getString("medecin_nom")).append(" ").append(rs.getString("medecin_prenom")).append("\n\n");

        // Informations du patient
        content.append("Patient: ").append(rs.getString("nom")).append(" ").append(rs.getString("prenom")).append("\n");
        content.append("CIN: ").append(rs.getString("cin")).append("\n");
        content.append("Téléphone: ").append(rs.getString("telephone")).append("\n\n");

        // Détails du rendez-vous
        content.append("Date du rendez-vous: ").append(rs.getTimestamp("date_heure_debut")).append("\n");
        if (rs.getString("type_test") != null) {
            content.append("Type de test: ").append(rs.getString("type_test")).append("\n");
        }
        content.append("\n");

        // Montant et paiement
        content.append("Montant: ").append(String.format("%.2f DH", rs.getDouble("montant_total"))).append("\n");
        content.append("Mode de paiement: ").append(rs.getString("mode_paiement")).append("\n");
        content.append("Statut: ").append(rs.getString("statut_paiement")).append("\n\n");

        // Pied de page
        content.append("========================================\n");
        content.append("Merci pour votre confiance!\n");
        content.append("========================================\n");

        return content.toString();
    }
}
