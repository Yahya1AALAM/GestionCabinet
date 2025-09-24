package cabinet.views;

import cabinet.model.DatabaseUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.text.SimpleDateFormat;


public class PaymentManagementView extends JPanel {
    private JTable paymentsTable;
    private DefaultTableModel tableModel;
    private JButton addPaymentButton, viewInvoiceButton, printReceiptButton;
    private JComboBox<String> statusFilterCombo;
    private JDateChooser dateFilterChooser;
    private JTextField searchField;

    public PaymentManagementView() {
        initComponents();
        loadPayments();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel filtres
        JPanel filterPanel = createFilterPanel();

        // Barre d’outils
        JToolBar toolBar = createToolBar();

        // Panel haut (filtres + toolbar alignée à gauche)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(filterPanel);

        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolBarPanel.add(toolBar);
        topPanel.add(toolBarPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tableau paiements
        JScrollPane tableScrollPane = createTableScrollPane();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

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
        statusFilterCombo.addActionListener(e -> filterPayments());
        filterPanel.add(statusFilterCombo);

        // Recherche
        filterPanel.add(new JLabel("Recherche:"));
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.addActionListener(e -> filterPayments());
        filterPanel.add(searchField);

        // Bouton Filtrer avec icône
        ImageIcon filterIcon = loadAndResizeIcon("/icons/filter.png", 16, 16);
        JButton filterButton = new JButton("Filtrer", filterIcon);
        filterButton.addActionListener(e -> filterPayments());
        filterPanel.add(filterButton);

        // Bouton Réinitialiser avec icône
        ImageIcon resetIcon = loadAndResizeIcon("/icons/clean.png", 16, 16);
        JButton clearFilterButton = new JButton("Réinitialiser", resetIcon);
        clearFilterButton.addActionListener(e -> {
            dateFilterChooser.setDate(null);
            statusFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            loadPayments();
        });
        filterPanel.add(clearFilterButton);

        return filterPanel;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Charger les icônes
        ImageIcon addIcon = loadAndResizeIcon("/icons/add.png", 16, 16);
        ImageIcon viewIcon = loadAndResizeIcon("/icons/view.png", 16, 16);
        ImageIcon printIcon = loadAndResizeIcon("/icons/print.png", 16, 16);

        addPaymentButton = new JButton("Nouveau paiement", addIcon);
        addPaymentButton.addActionListener(this::openAddPaymentDialog);

        viewInvoiceButton = new JButton("Voir facture", viewIcon);
        viewInvoiceButton.addActionListener(this::viewInvoice);

        printReceiptButton = new JButton("Imprimer reçu", printIcon);
        printReceiptButton.addActionListener(this::printReceipt);

        toolBar.add(addPaymentButton);
        toolBar.add(viewInvoiceButton);
        toolBar.add(printReceiptButton);

        return toolBar;
    }

    private JScrollPane createTableScrollPane() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Facture", "Patient", "Montant", "Date", "Mode", "Statut"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentsTable = new JTable(tableModel);
        paymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentsTable.getTableHeader().setReorderingAllowed(false);

        // Ajuster les largeurs de colonnes
        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Facture
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Patient
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Montant
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Date
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Mode
        paymentsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Statut

        return new JScrollPane(paymentsTable);
    }

    private void loadPayments() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.id_paiement, f.numero as facture_numero, " +
                "pat.nom as patient_nom, pat.prenom as patient_prenom, " +
                "p.montant, p.date_paiement, p.mode_paiement, p.statut " +
                "FROM Paiement p " +
                "JOIN Facture f ON p.facture_id = f.id_facture " +
                "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                "JOIN Patient pat ON r.patient_id = pat.id_patient " +
                "ORDER BY p.date_paiement DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_paiement"),
                        rs.getString("facture_numero"),
                        rs.getString("patient_nom") + " " + rs.getString("patient_prenom"),
                        String.format("%.2f DH", rs.getDouble("montant")),
                        dateFormat.format(rs.getDate("date_paiement")),
                        rs.getString("mode_paiement"),
                        rs.getString("statut")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des paiements: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void filterPayments() {
        tableModel.setRowCount(0);

        java.util.Date selectedDate = dateFilterChooser.getDate();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String searchText = searchField.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT p.id_paiement, f.numero as facture_numero, " +
                "pat.nom as patient_nom, pat.prenom as patient_prenom, " +
                "p.montant, p.date_paiement, p.mode_paiement, p.statut " +
                "FROM Paiement p " +
                "JOIN Facture f ON p.facture_id = f.id_facture " +
                "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                "JOIN Patient pat ON r.patient_id = pat.id_patient " +
                "WHERE 1=1");

        if (selectedDate != null) {
            sql.append(" AND DATE(p.date_paiement) = ?");
        }

        if (!"Tous".equals(selectedStatus)) {
            sql.append(" AND p.statut = ?");
        }

        if (!searchText.isEmpty()) {
            sql.append(" AND (pat.nom LIKE ? OR pat.prenom LIKE ? OR f.numero LIKE ?)");
        }

        sql.append(" ORDER BY p.date_paiement DESC");

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
                            rs.getInt("id_paiement"),
                            rs.getString("facture_numero"),
                            rs.getString("patient_nom") + " " + rs.getString("patient_prenom"),
                            String.format("%.2f DH", rs.getDouble("montant")),
                            dateFormat.format(rs.getDate("date_paiement")),
                            rs.getString("mode_paiement"),
                            rs.getString("statut")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de filtrage: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openAddPaymentDialog(ActionEvent e) {
        AddPaymentDialog dialog = new AddPaymentDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadPayments();
    }

    private void viewInvoice(ActionEvent e) {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un paiement",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int paymentId = (int) paymentsTable.getValueAt(selectedRow, 0);
        String invoiceNumber = (String) paymentsTable.getValueAt(selectedRow, 1);

        // Ouvrir la vue de la facture correspondante
        ViewInvoiceDialog dialog = new ViewInvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this), invoiceNumber);
        dialog.setVisible(true);
    }

    private void printReceipt(ActionEvent e) {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un paiement",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int paymentId = (int) paymentsTable.getValueAt(selectedRow, 0);

        try {
            // Récupérer les détails du paiement
            String sql = "SELECT p.*, f.numero as facture_numero, " +
                    "pat.nom as patient_nom, pat.prenom as patient_prenom, pat.cin, " +
                    "med.nom as medecin_nom, med.prenom as medecin_prenom " +
                    "FROM Paiement p " +
                    "JOIN Facture f ON p.facture_id = f.id_facture " +
                    "JOIN RendezVous r ON f.rendezvous_id = r.id_rdv " +
                    "JOIN Patient pat ON r.patient_id = pat.id_patient " +
                    "JOIN Medecin med ON r.medecin_id = med.id_medecin " +
                    "WHERE p.id_paiement = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, paymentId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Générer le contenu du reçu
                        String receiptContent = generateReceiptContent(rs);

                        // Afficher un dialogue d'impression
                        showPrintDialog(receiptContent, "Reçu de Paiement #" + paymentId);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de récupération du paiement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String generateReceiptContent(ResultSet rs) throws SQLException {
        StringBuilder content = new StringBuilder();

        // En-tête du reçu
        content.append("========================================\n");
        content.append("           CABINET MÉDICAL\n");
        content.append("             REÇU DE PAIEMENT\n");
        content.append("========================================\n\n");

        // Informations du reçu
        content.append("Reçu N°: ").append(rs.getInt("id_paiement")).append("\n");
        content.append("Date: ").append(rs.getDate("date_paiement")).append("\n");
        content.append("Facture N°: ").append(rs.getString("facture_numero")).append("\n\n");

        // Informations du patient
        content.append("Patient: ").append(rs.getString("patient_nom")).append(" ").append(rs.getString("patient_prenom")).append("\n");
        content.append("CIN: ").append(rs.getString("cin")).append("\n\n");

        // Informations du médecin
        content.append("Médecin: ").append(rs.getString("medecin_nom")).append(" ").append(rs.getString("medecin_prenom")).append("\n\n");

        // Détails du paiement
        content.append("Montant: ").append(String.format("%.2f DH", rs.getDouble("montant"))).append("\n");
        content.append("Mode de paiement: ").append(rs.getString("mode_paiement")).append("\n");
        content.append("Statut: ").append(rs.getString("statut")).append("\n\n");

        // Pied de page
        content.append("========================================\n");
        content.append("Merci pour votre confiance!\n");
        content.append("========================================\n");

        return content.toString();
    }

    private void showPrintDialog(String content, String title) {
        // Créer une zone de texte avec le contenu du reçu
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Afficher dans une boîte de dialogue avec option d'impression
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        // Panel de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton printButton = new JButton("Imprimer");
        printButton.addActionListener(evt -> {
            // Logique d'impression (à implémenter)
            JOptionPane.showMessageDialog(this, "Fonction d'impression à implémenter");
        });

        buttonPanel.add(printButton);

        JOptionPane.showMessageDialog(this,
                new Object[]{scrollPane, buttonPanel},
                title,
                JOptionPane.PLAIN_MESSAGE);
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
}