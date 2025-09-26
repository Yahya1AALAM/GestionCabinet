package cabinet.views;

import cabinet.model.DatabaseUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;



public class PatientManagementView extends JPanel {
    private JTable patientsTable;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchComboBox;
    private JButton filterButton, clearFilterButton;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public PatientManagementView() {
        initComponents();
        loadPatients();
    }
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres"));

        // Combo pour choisir la colonne à rechercher
        filterPanel.add(new JLabel("Rechercher par:"));
        searchComboBox = new JComboBox<>(new String[]{"Nom", "Prénom", "CIN", "Téléphone"});
        filterPanel.add(searchComboBox);

        // Champ texte
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 24));
        searchField.addActionListener(e -> filterPatients()); // Enter = rechercher
        filterPanel.add(searchField);

        // Icônes
        ImageIcon filterIcon = loadAndResizeIcon("/icons/filter.png", 16, 16);
        ImageIcon resetIcon = loadAndResizeIcon("/icons/refresh.png", 16, 16);

        // Bouton Filtrer avec icône
        filterButton = new JButton("Filtrer", filterIcon);
        filterButton.setToolTipText("Appliquer le filtre");
        filterButton.addActionListener(e -> filterPatients());
        filterPanel.add(filterButton);

        // Bouton Réinitialiser avec icône
        clearFilterButton = new JButton("Réinitialiser", resetIcon);
        clearFilterButton.setToolTipText("Réinitialiser les filtres");
        clearFilterButton.addActionListener(e -> {
            searchField.setText("");
            searchComboBox.setSelectedIndex(0);
            loadPatients();
        });
        filterPanel.add(clearFilterButton);

        return filterPanel;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // MAIN PANEL qui contient topPanel + table
        JPanel mainPanel = new JPanel(new BorderLayout());

        // TOP PANEL : filtres (createFilterPanel) + toolbar (sous les filtres)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // add filter panel
        topPanel.add(createFilterPanel());

        // toolbar (sous le filterPanel)
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        ImageIcon addIcon = loadIconSafe("/icons/add.png", 16, 16);
        ImageIcon editIcon = loadIconSafe("/icons/edit.png", 16, 16);
        ImageIcon deleteIcon = loadIconSafe("/icons/delete.png", 16, 16);
        ImageIcon refreshIcon = loadIconSafe("/icons/refresh.png", 16, 16);

        addButton = new JButton("Ajouter", addIcon);
        addButton.setToolTipText("Ajouter un patient");
        addButton.addActionListener(this::openAddPatientDialog);

        editButton = new JButton("Modifier", editIcon);
        editButton.setToolTipText("Modifier le patient sélectionné");
        editButton.addActionListener(this::openEditPatientDialog);

        deleteButton = new JButton("Supprimer", deleteIcon);
        deleteButton.setToolTipText("Supprimer le patient sélectionné");
        deleteButton.addActionListener(this::deletePatient);

        refreshButton = new JButton("Actualiser", refreshIcon);
        refreshButton.setToolTipText("Actualiser la liste");
        refreshButton.addActionListener(e -> loadPatients());

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);

        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolBarPanel.add(toolBar);
        topPanel.add(toolBarPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // TABLEAU
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Prénom", "CIN", "Date de naissance", "Téléphone", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientsTable = new JTable(tableModel);
        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientsTable.getTableHeader().setReorderingAllowed(false);

        // Centrer les cellules
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        // appliquer après création des colonnes
        patientsTable.setDefaultRenderer(Object.class, centerRenderer);

        // Ajuster largeur colonne si possible
        patientsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        patientsTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        patientsTable.getColumnModel().getColumn(1).setPreferredWidth(140);  // Nom
        patientsTable.getColumnModel().getColumn(2).setPreferredWidth(140);  // Prénom
        patientsTable.getColumnModel().getColumn(3).setPreferredWidth(110);  // CIN
        patientsTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // Date naissance
        patientsTable.getColumnModel().getColumn(5).setPreferredWidth(120);  // Téléphone
        patientsTable.getColumnModel().getColumn(6).setPreferredWidth(170);  // Email

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // status footer
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Double-cliquez sur un patient pour le modifier"));
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // double-clic (une seule écoute)
        patientsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openEditPatientDialog(null);
                }
            }
        });
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
                return new ImageIcon();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône: " + path);
            e.printStackTrace();
            return new ImageIcon();
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

    private void filterPatients() {
        tableModel.setRowCount(0);

        String searchText = searchField.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT * FROM Patient WHERE 1=1");

        if (!searchText.isEmpty()) {
            sql.append(" AND (nom LIKE ? OR prenom LIKE ? OR cin LIKE ?)");
        }

        sql.append(" ORDER BY nom, prenom");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id_patient"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("cin"),
                            rs.getString("telephone"),
                            rs.getDate("date_naissance")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur filtrage patients: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPatients() {
        tableModel.setRowCount(0);
        String sql = "SELECT id_patient, nom, prenom, cin, date_naissance, telephone, email FROM Patient ORDER BY nom, prenom";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                java.sql.Date d = rs.getDate("date_naissance");
                String dateStr = (d == null) ? "" : dateFormat.format(d);
                tableModel.addRow(new Object[]{
                        rs.getInt("id_patient"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("cin"),
                        dateStr,
                        rs.getString("telephone"),
                        rs.getString("email")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement patients: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void searchPatients() {
        String searchText = searchField.getText().trim();
        String column = "nom"; // valeur par défaut

        String choice = (String) searchComboBox.getSelectedItem();
        if ("Nom".equals(choice)) column = "nom";
        else if ("Prénom".equals(choice)) column = "prenom";
        else if ("CIN".equals(choice)) column = "cin";
        else if ("Téléphone".equals(choice)) column = "telephone";

        tableModel.setRowCount(0);

        String sql = "SELECT id_patient, nom, prenom, cin, date_naissance, telephone, email FROM Patient WHERE " + column + " LIKE ? ORDER BY nom, prenom";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id_patient"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("cin"),
                            rs.getDate("date_naissance"),
                            rs.getString("telephone"),
                            rs.getString("email")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur filtrage patients: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openAddPatientDialog(ActionEvent e) {
        AddPatientDialog dialog = new AddPatientDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadPatients();
    }

    private void openEditPatientDialog(ActionEvent e) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un patient",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int patientId = (int) patientsTable.getValueAt(selectedRow, 0);
        // Ouvrir le dialogue d'édition avec les données du patient
        EditPatientDialog dialog = new EditPatientDialog((JFrame)SwingUtilities.getWindowAncestor(this), patientId);
        dialog.setVisible(true);
        loadPatients();
    }

    private ImageIcon loadIconSafe(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon original = new ImageIcon(imgURL);
                Image img = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return null; // IntelliJ affichera juste le texte du bouton si l'icône est null
    }

    private void deletePatient(ActionEvent e) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un patient",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int patientId = (int) patientsTable.getValueAt(selectedRow, 0);
        String patientName = (String) patientsTable.getValueAt(selectedRow, 1) + " " +
                (String) patientsTable.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer " + patientName + "?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM Patient WHERE id_patient = ?")) {

                stmt.setInt(1, patientId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Patient supprimé avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadPatients();
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la suppression du patient",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur de suppression: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}