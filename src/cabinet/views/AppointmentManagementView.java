package cabinet.views;

import cabinet.model.DatabaseUtil;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AppointmentManagementView extends JPanel {
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private JComboBox<String> statusFilterCombo;
    private JDateChooser dateFilterChooser;

    public AppointmentManagementView() {
        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        // Utiliser un BorderLayout comme layout principal
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Créer un panel principal pour organiser les composants
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel de filtres
        JPanel filterPanel = createFilterPanel();

        // Barre d'outils
        JToolBar toolBar = createToolBar();

        // Panel supérieur (filtres + toolbar alignée à gauche)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(filterPanel);

        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolBarPanel.add(toolBar);
        topPanel.add(toolBarPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tableau des rendez-vous
        JScrollPane tableScrollPane = createTableScrollPane();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Ajouter le panel principal à la vue
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        // Filtre par date
        filterPanel.add(new JLabel("Date:"));
        dateFilterChooser = new JDateChooser();
        dateFilterChooser.setDateFormatString("yyyy-MM-dd");
        dateFilterChooser.setPreferredSize(new Dimension(120, 25));
        filterPanel.add(dateFilterChooser);

        // Bouton Filtrer
        ImageIcon filterIcon = loadAndResizeIcon("/icons/filter.png", 16, 16);
        JButton filterButton = new JButton("Filtrer", filterIcon);
        filterButton.addActionListener(e -> filterAppointments());
        filterPanel.add(filterButton);

        // Bouton Tout afficher
        ImageIcon clearIcon = loadAndResizeIcon("/icons/clean.png", 16, 16);
        JButton clearFilterButton = new JButton("Réinitialiser", clearIcon);
        clearFilterButton.addActionListener(e -> loadAppointments());
        filterPanel.add(clearFilterButton);

        // Filtre par statut
        filterPanel.add(new JLabel("Statut:"));
        statusFilterCombo = new JComboBox<>(new String[]{"Tous", "Prévu", "Confirmé", "Annulé", "Passé"});
        statusFilterCombo.addActionListener(e -> filterAppointments());
        filterPanel.add(statusFilterCombo);

        return filterPanel;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Chargement des icônes
        ImageIcon addIcon = loadAndResizeIcon("/icons/add.png", 16, 16);
        ImageIcon editIcon = loadAndResizeIcon("/icons/edit.png", 16, 16);
        ImageIcon deleteIcon = loadAndResizeIcon("/icons/delete.png", 16, 16);
        ImageIcon refreshIcon = loadAndResizeIcon("/icons/refresh.png", 16, 16);
        ImageIcon invoiceIcon = loadAndResizeIcon("/icons/invoice.png", 16, 16);

        // Boutons avec icônes
        addButton = new JButton("Nouveau RDV", addIcon);
        addButton.addActionListener(this::openAddAppointmentDialog);

        editButton = new JButton("Modifier", editIcon);
        editButton.addActionListener(this::openEditAppointmentDialog);

        deleteButton = new JButton("Supprimer", deleteIcon);
        deleteButton.addActionListener(this::deleteAppointment);

        refreshButton = new JButton("Actualiser", refreshIcon);
        refreshButton.addActionListener(e -> loadAppointments());

        JButton createInvoiceButton = new JButton("Créer facture", invoiceIcon);
        createInvoiceButton.addActionListener(this::createInvoiceFromAppointment);

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);
        toolBar.add(createInvoiceButton);

        return toolBar;
    }

    private JScrollPane createTableScrollPane() {
        // Tableau des rendez-vous
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Patient", "Médecin", "Date/Heure", "Statut", "Type de test"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentsTable = new JTable(tableModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.getTableHeader().setReorderingAllowed(false);

        // Centrer les données dans les colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < appointmentsTable.getColumnCount(); i++) {
            appointmentsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Ajouter un écouteur de double-clic pour modifier
        appointmentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openEditAppointmentDialog(null);
                }
            }
        });

        return new JScrollPane(appointmentsTable);
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

    // Méthode pour créer une icône de texte de secours
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

    private void loadAppointments() {
        tableModel.setRowCount(0);

        String sql = "SELECT r.id_rdv, p.nom as patient_nom, p.prenom as patient_prenom, " +
                "m.nom as medecin_nom, m.prenom as medecin_prenom, " +
                "r.date_heure_debut, r.statut, t.nom as type_test " +
                "FROM RendezVous r " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "JOIN Medecin m ON r.medecin_id = m.id_medecin " +
                "LEFT JOIN TypeTest t ON r.type_test_id = t.id_type_test " +
                "ORDER BY r.date_heure_debut DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                Timestamp dateHeure = rs.getTimestamp("date_heure_debut");
                String dateStr = (dateHeure != null) ? dateFormat.format(dateHeure) : "";

                tableModel.addRow(new Object[]{
                        rs.getInt("id_rdv"),
                        rs.getString("patient_nom") + " " + rs.getString("patient_prenom"),
                        rs.getString("medecin_nom") + " " + rs.getString("medecin_prenom"),
                        dateStr,
                        rs.getString("statut"),
                        rs.getString("type_test")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void filterAppointments() {
        tableModel.setRowCount(0);

        java.util.Date selectedDate = dateFilterChooser.getDate();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();

        StringBuilder sql = new StringBuilder("SELECT r.id_rdv, p.nom as patient_nom, p.prenom as patient_prenom, " +
                "m.nom as medecin_nom, m.prenom as medecin_prenom, " +
                "r.date_heure_debut, r.statut, t.nom as type_test " +
                "FROM RendezVous r " +
                "JOIN Patient p ON r.patient_id = p.id_patient " +
                "JOIN Medecin m ON r.medecin_id = m.id_medecin " +
                "LEFT JOIN TypeTest t ON r.type_test_id = t.id_type_test " +
                "WHERE 1=1");

        if (selectedDate != null) {
            sql.append(" AND DATE(r.date_heure_debut) = ?");
        }

        if (!"Tous".equals(selectedStatus)) {
            sql.append(" AND r.statut = ?");
        }

        sql.append(" ORDER BY r.date_heure_debut DESC");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (selectedDate != null) {
                stmt.setDate(paramIndex++, new java.sql.Date(selectedDate.getTime()));
            }

            if (!"Tous".equals(selectedStatus)) {
                stmt.setString(paramIndex++, selectedStatus);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    Timestamp dateHeure = rs.getTimestamp("date_heure_debut");
                    String dateStr = (dateHeure != null) ? dateFormat.format(dateHeure) : "";

                    tableModel.addRow(new Object[]{
                            rs.getInt("id_rdv"),
                            rs.getString("patient_nom") + " " + rs.getString("patient_prenom"),
                            rs.getString("medecin_nom") + " " + rs.getString("medecin_prenom"),
                            dateStr,
                            rs.getString("statut"),
                            rs.getString("type_test")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de filtrage: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openAddAppointmentDialog(ActionEvent e) {
        AddAppointmentDialog dialog = new AddAppointmentDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadAppointments();
    }

    private void openEditAppointmentDialog(ActionEvent e) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un rendez-vous",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsTable.getValueAt(selectedRow, 0);
        EditAppointmentDialog dialog = new EditAppointmentDialog((JFrame)SwingUtilities.getWindowAncestor(this), appointmentId);
        dialog.setVisible(true);
        loadAppointments();
    }

    private void deleteAppointment(ActionEvent e) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un rendez-vous",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsTable.getValueAt(selectedRow, 0);
        String patientName = (String) appointmentsTable.getValueAt(selectedRow, 1);
        String dateTime = (String) appointmentsTable.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer le rendez-vous de " + patientName + " du " + dateTime + "?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM RendezVous WHERE id_rdv = ?")) {

                stmt.setInt(1, appointmentId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Rendez-vous supprimé avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la suppression du rendez-vous",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur de suppression: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void createInvoiceFromAppointment(ActionEvent e) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un rendez-vous",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsTable.getValueAt(selectedRow, 0);

        // Vérifier si une facture existe déjà pour ce rendez-vous
        if (isInvoiceExistsForAppointment(appointmentId)) {
            JOptionPane.showMessageDialog(this, "Une facture existe déjà pour ce rendez-vous",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CreateInvoiceFromAppointmentDialog dialog = new CreateInvoiceFromAppointmentDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), appointmentId);
        dialog.setVisible(true);
    }

    private boolean isInvoiceExistsForAppointment(int appointmentId) {
        String sql = "SELECT COUNT(*) FROM Facture WHERE rendezvous_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

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
