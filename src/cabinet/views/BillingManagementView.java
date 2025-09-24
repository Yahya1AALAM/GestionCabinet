package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;

public class BillingManagementView extends JPanel {
    private JTable invoicesTable;
    private DefaultTableModel tableModel;
    private JButton createInvoiceButton, printInvoiceButton, markPaidButton, editInvoiceButton;
    private JComboBox<String> statusFilterCombo;
    private JDateChooser dateFilterChooser;
    private JTextField searchField;

    public BillingManagementView() {
        initComponents();
        loadInvoices();
    }

    private void initComponents() {
        // Utiliser un BorderLayout comme layout principal
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Créer un panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel de filtres
        JPanel filterPanel = createFilterPanel();

        // Barre d’outils
        JToolBar toolBar = createToolBar();

        // Panel supérieur (filtres + toolbar alignée à gauche)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(filterPanel);

        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolBarPanel.add(toolBar);
        topPanel.add(toolBarPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tableau des factures
        JScrollPane tableScrollPane = createTableScrollPane();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Ajouter le panel principal
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        // Filtre par date
        filterPanel.add(new JLabel("Date:"));
        dateFilterChooser = new JDateChooser();
        dateFilterChooser.setDateFormatString("dd/MM/yy");
        dateFilterChooser.setPreferredSize(new Dimension(100, 25));
        filterPanel.add(dateFilterChooser);

        // Filtre par statut
        filterPanel.add(new JLabel("Statut:"));
        statusFilterCombo = new JComboBox<>(new String[]{"Tous", "Payé", "En attente", "Annulé"});
        statusFilterCombo.setPreferredSize(new Dimension(120, 25));
        statusFilterCombo.addActionListener(e -> filterInvoices());
        filterPanel.add(statusFilterCombo);

        // Recherche
        filterPanel.add(new JLabel("Recherche:"));
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.addActionListener(e -> filterInvoices());
        filterPanel.add(searchField);

        // Bouton Filtrer avec icône
        ImageIcon filterIcon = loadAndResizeIcon("/icons/filter.png", 16, 16);
        JButton filterButton = new JButton("Filtrer", filterIcon);
        filterButton.addActionListener(e -> filterInvoices());
        filterPanel.add(filterButton);

        // Bouton Réinitialiser avec icône
        ImageIcon resetIcon = loadAndResizeIcon("/icons/clean.png", 16, 16);
        JButton clearFilterButton = new JButton("Réinitialiser", resetIcon);
        clearFilterButton.addActionListener(e -> {
            dateFilterChooser.setDate(null);
            statusFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            loadInvoices();
        });
        filterPanel.add(clearFilterButton);

        return filterPanel;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Charger les icônes
        ImageIcon createIcon = loadAndResizeIcon("/icons/add.png", 16, 16);
        ImageIcon editIcon = loadAndResizeIcon("/icons/edit.png", 16, 16);
        ImageIcon printIcon = loadAndResizeIcon("/icons/print.png", 16, 16);
        ImageIcon paidIcon = loadAndResizeIcon("/icons/check.png", 16, 16);

        createInvoiceButton = new JButton("Créer facture", createIcon);
        createInvoiceButton.addActionListener(this::openCreateInvoiceDialog);

        editInvoiceButton = new JButton("Modifier", editIcon);
        editInvoiceButton.addActionListener(this::openEditInvoiceDialog);

        printInvoiceButton = new JButton("Imprimer", printIcon);
        printInvoiceButton.addActionListener(this::printInvoice);

        markPaidButton = new JButton("Marquer payé", paidIcon);
        markPaidButton.addActionListener(this::markAsPaid);

        toolBar.add(createInvoiceButton);
        toolBar.add(editInvoiceButton);
        toolBar.add(printInvoiceButton);
        toolBar.add(markPaidButton);

        return toolBar;
    }

    private JScrollPane createTableScrollPane() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Numéro", "Date", "Patient", "Montant", "Statut", "RDV Associé"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        invoicesTable = new JTable(tableModel);
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoicesTable.getTableHeader().setReorderingAllowed(false);

        // Ajuster les largeurs de colonnes
        invoicesTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        invoicesTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Numéro
        invoicesTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Date
        invoicesTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Patient
        invoicesTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Montant
        invoicesTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Statut
        invoicesTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // RDV Associé

        return new JScrollPane(invoicesTable);
    }

    private ImageIcon loadAndResizeIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image image = originalIcon.getImage();
                Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(resizedImage);
            } else {
                System.err.println("Fichier introuvable: " + path);
                return createTextIcon("[!]");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône: " + path);
            e.printStackTrace();
            return createTextIcon("[ERR]");
        }
    }

    private ImageIcon createTextIcon(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.BLACK);
        label.setFont(label.getFont().deriveFont(8f));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        label.setSize(16, 16);
        label.paint(g2d);
        g2d.dispose();

        return new ImageIcon(image);
    }

    private void loadInvoices() {
        tableModel.setRowCount(0);
        String sql = "SELECT f.*, p.nom, p.prenom, r.id_rdv " +
                "FROM Facture f " +
                "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "ORDER BY f.date_emission DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_facture"),
                        rs.getString("numero"),
                        dateFormat.format(rs.getDate("date_emission")),
                        rs.getString("nom") + " " + rs.getString("prenom"),
                        String.format("%.2f DH", rs.getDouble("montant_total")),
                        rs.getString("statut_paiement"),
                        rs.getInt("id_rdv")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des factures: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void filterInvoices() {
        tableModel.setRowCount(0);

        java.util.Date selectedDate = dateFilterChooser.getDate();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String searchText = searchField.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT f.*, p.nom, p.prenom, r.id_rdv " +
                "FROM Facture f " +
                "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "WHERE 1=1");

        if (selectedDate != null) {
            sql.append(" AND DATE(f.date_emission) = ?");
        }

        if (!"Tous".equals(selectedStatus)) {
            sql.append(" AND f.statut_paiement = ?");
        }

        if (!searchText.isEmpty()) {
            sql.append(" AND (p.nom LIKE ? OR p.prenom LIKE ? OR f.numero LIKE ?)");
        }

        sql.append(" ORDER BY f.date_emission DESC");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (selectedDate != null) {
                stmt.setDate(paramIndex++, new java.sql.Date(selectedDate.getTime()));
            }

            if (!"Tous".equals(selectedStatus)) {
                stmt.setString(paramIndex++, selectedStatus);
            }

            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id_facture"),
                            rs.getString("numero"),
                            dateFormat.format(rs.getDate("date_emission")),
                            rs.getString("nom") + " " + rs.getString("prenom"),
                            String.format("%.2f DH", rs.getDouble("montant_total")),
                            rs.getString("statut_paiement"),
                            rs.getInt("id_rdv")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de filtrage: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openCreateInvoiceDialog(ActionEvent e) {
        CreateInvoiceDialog dialog = new CreateInvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadInvoices();
    }

    private void openEditInvoiceDialog(ActionEvent e) {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une facture",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int invoiceId = (int) invoicesTable.getValueAt(selectedRow, 0);
        EditInvoiceDialog dialog = new EditInvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this), invoiceId);
        dialog.setVisible(true);
        loadInvoices();
    }

    private void printInvoice(ActionEvent e) {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une facture",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int invoiceId = (int) invoicesTable.getValueAt(selectedRow, 0);

        try {
            // Récupérer les détails complets de la facture
            String sql = "SELECT f.*, p.nom, p.prenom, p.cin, p.telephone, " +
                    "m.nom as medecin_nom, m.prenom as medecin_prenom, " +
                    "r.date_heure_debut, t.nom as type_test " +
                    "FROM Facture f " +
                    "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                    "JOIN Patient p ON r.patient_id = p.id_patient " +
                    "JOIN Medecin m ON r.medecin_id = m.id_medecin " +
                    "LEFT JOIN TypeTest t ON r.type_test_id = t.id_type_test " +
                    "WHERE f.id_facture = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, invoiceId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Générer le contenu de la facture
                        String invoiceContent = generateInvoiceContent(rs);

                        // Afficher un dialogue d'impression
                        showPrintDialog(invoiceContent, "Facture #" + rs.getString("numero"));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de récupération de la facture: " + ex.getMessage(),
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

        // Convertir le code de paiement en libellé complet
        String modePaiementCode = rs.getString("mode_paiement");
        String modePaiementLibelle = convertPaymentModeToLabel(modePaiementCode);
        content.append("Mode de paiement: ").append(modePaiementLibelle).append("\n");

        content.append("Statut: ").append(rs.getString("statut_paiement")).append("\n\n");

        // Pied de page
        content.append("========================================\n");
        content.append("Merci pour votre confiance!\n");
        content.append("========================================\n");

        return content.toString();
    }

    private String convertPaymentModeToLabel(String code) {
        if (code == null) return "Non spécifié";

        switch (code) {
            case "Espece": return "Espèces";
            case "Carte": return "Carte";
            case "Autre": return "Autre";
            default: return code;
        }
    }

    private void showPrintDialog(String content, String title) {
        // Créer une zone de texte avec le contenu de la facture
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Afficher dans une boîte de dialogue avec option d'impression
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        // Panel de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton previewButton = new JButton("Prévisualisation");
        previewButton.addActionListener(evt -> {
            JDialog previewDialog = new JDialog((Frame) null, "Prévisualisation de la facture", true);
            previewDialog.setSize(600, 500);
            previewDialog.setLocationRelativeTo(null);

            JTextArea previewArea = new JTextArea(content);
            previewArea.setEditable(false);
            previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            previewDialog.add(new JScrollPane(previewArea));
            previewDialog.setVisible(true);
        });

        JButton printButton = new JButton("Imprimer");
        printButton.addActionListener(evt -> {
            InvoicePrinter.printInvoice(content);
        });

        buttonPanel.add(previewButton);
        buttonPanel.add(printButton);

        JOptionPane.showMessageDialog(this,
                new Object[]{scrollPane, buttonPanel},
                title,
                JOptionPane.PLAIN_MESSAGE);
    }

    private void markAsPaid(ActionEvent e) {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une facture",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int invoiceId = (int) invoicesTable.getValueAt(selectedRow, 0);

        // Correction: Récupérer le montant comme String et le convertir en double
        String montantString = (String) invoicesTable.getValueAt(selectedRow, 4);
        double montantTotal;

        try {
            // Supprimer " DH" et convertir en double
            montantString = montantString.replace(" DH", "").trim();
            montantTotal = Double.parseDouble(montantString);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de format du montant: " + montantString,
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si un paiement complet existe déjà
        double totalPaye = getTotalPaidForInvoice(invoiceId);
        if (Math.abs(totalPaye - montantTotal) < 0.01) {
            JOptionPane.showMessageDialog(this, "Cette facture est déjà payée en totalité",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Créer un paiement complet
        if (createFullPayment(invoiceId, montantTotal)) {
            JOptionPane.showMessageDialog(this, "Facture marquée comme payée et paiement enregistré");
            loadInvoices(); // Recharger les factures
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement du paiement");
        }
    }

    private double getTotalPaidForInvoice(int invoiceId) {
        String sql = "SELECT COALESCE(SUM(montant), 0) as total FROM Paiement WHERE facture_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
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

    private boolean createFullPayment(int factureId, double montant) {
        String sql = "INSERT INTO Paiement (facture_id, montant, date_paiement, mode_paiement, statut, notes) " +
                "VALUES (?, ?, NOW(), 'Espece', 'Complet', 'Paiement complet via bouton Marquer payé')";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, factureId);
            stmt.setDouble(2, montant);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Mettre à jour le statut de la facture
                String updateSql = "UPDATE Facture SET statut_paiement = 'Paye' WHERE id_facture = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, factureId);
                    updateStmt.executeUpdate();
                }
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur base de données: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return false;
    }
}
