package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableCellRenderer;



public class PatientManagementView extends JPanel {
    private JTable patientsTable;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchComboBox;

    public PatientManagementView() {
        initComponents();
        loadPatients();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher par:"));

        searchComboBox = new JComboBox<>(new String[]{"Nom", "Prénom", "CIN", "Téléphone"});
        searchPanel.add(searchComboBox);

        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Rechercher");
        searchButton.addActionListener(e -> searchPatients());
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // Barre d'outils avec icônes
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        ImageIcon addIcon = loadAndResizeIcon("/icons/add.png",16,16);
        ImageIcon editIcon = loadAndResizeIcon("/icons/edit.png",16,16);
        ImageIcon deleteIcon = loadAndResizeIcon("/icons/delete.png",16,16);
        ImageIcon refreshIcon = loadAndResizeIcon("/icons/refresh.png",16,16);

// Icône Ajouter
        addButton = new JButton("Ajouter", addIcon);
        addButton.setToolTipText("Ajouter un patient");
        addButton.addActionListener(this::openAddPatientDialog);

// Icône Modifier
        editButton = new JButton("Modifier", editIcon);
        editButton.setToolTipText("Modifier le patient sélectionné");
        editButton.addActionListener(this::openEditPatientDialog);

// Icône Supprimer
        deleteButton = new JButton("Supprimer", deleteIcon);
        deleteButton.setToolTipText("Supprimer le patient sélectionné");
        deleteButton.addActionListener(this::deletePatient);

// Icône Actualiser
        refreshButton = new JButton("Actualiser", refreshIcon);
        refreshButton.setToolTipText("Actualiser la liste");
        refreshButton.addActionListener(e -> loadPatients());

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        // Tableau des patients avec la colonne Date de naissance
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

        // Centrer les données dans les colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < patientsTable.getColumnCount(); i++) {
            patientsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Ajuster la largeur des colonnes
        patientsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        patientsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Nom
        patientsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Prénom
        patientsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // CIN
        patientsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Date de naissance
        patientsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Téléphone
        patientsTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Email

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de statut
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Double-cliquez sur un patient pour le modifier"));
        add(statusPanel, BorderLayout.SOUTH);

        // Ajouter un écouteur de double-clic pour modifier
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

    private void loadPatients() {
        tableModel.setRowCount(0);

        String sql = "SELECT * FROM Patient ORDER BY nom, prenom";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                java.sql.Date dateNaiss = rs.getDate("date_naissance");
                String dateStr = (dateNaiss != null) ? dateFormat.format(dateNaiss) : "";

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
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void searchPatients() {
        String searchBy = (String) searchComboBox.getSelectedItem();
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            loadPatients();
            return;
        }

        tableModel.setRowCount(0);
        String sql = "";

        switch (searchBy) {
            case "Nom":
                sql = "SELECT * FROM Patient WHERE nom LIKE ? ORDER BY nom, prenom";
                break;
            case "Prénom":
                sql = "SELECT * FROM Patient WHERE prenom LIKE ? ORDER BY nom, prenom";
                break;
            case "CIN":
                sql = "SELECT * FROM Patient WHERE cin LIKE ? ORDER BY nom, prenom";
                break;
            case "Téléphone":
                sql = "SELECT * FROM Patient WHERE telephone LIKE ? ORDER BY nom, prenom";
                break;
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    java.sql.Date dateNaiss = rs.getDate("date_naissance");
                    String dateStr = (dateNaiss != null) ? dateFormat.format(dateNaiss) : "";

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
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de recherche: " + ex.getMessage(),
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