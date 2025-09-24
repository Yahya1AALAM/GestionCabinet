package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.sql.*;

public class DoctorManagementView extends JPanel {
    private JTable doctorsTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    public DoctorManagementView() {
        initComponents();
        loadDoctors();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Barre d'outils avec icônes
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Chargement des icônes
        ImageIcon addIcon = loadAndResizeIcon("/icons/add.png", 16, 16);
        ImageIcon editIcon = loadAndResizeIcon("/icons/edit.png", 16, 16);
        ImageIcon deleteIcon = loadAndResizeIcon("/icons/delete.png", 16, 16);
        ImageIcon refreshIcon = loadAndResizeIcon("/icons/refresh.png", 16, 16);

        // Icône Ajouter
        addButton = new JButton("Ajouter ", addIcon);
        addButton.setToolTipText("Ajouter un médecin");
        addButton.addActionListener(this::openAddDoctorDialog);

        // Icône Modifier
        editButton = new JButton("Modifier", editIcon);
        editButton.setToolTipText("Modifier le médecin sélectionné");
        editButton.addActionListener(this::openEditDoctorDialog);

        // Icône Supprimer
        deleteButton = new JButton("Supprimer", deleteIcon);
        deleteButton.setToolTipText("Supprimer le médecin sélectionné");
        deleteButton.addActionListener(this::deleteDoctor);

        // Icône Actualiser
        refreshButton = new JButton("Actualiser", refreshIcon);
        refreshButton.setToolTipText("Actualiser la liste");
        refreshButton.addActionListener(e -> loadDoctors());

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);

        // Tableau des médecins
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Prénom", "Spécialité", "Téléphone", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorsTable = new JTable(tableModel);
        doctorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorsTable.getTableHeader().setReorderingAllowed(false);

        // Ajout de l'écouteur de souris pour le double-clic
        doctorsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openEditDoctorDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        add(scrollPane, BorderLayout.CENTER);
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

    private void loadDoctors() {
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Medecin ORDER BY nom, prenom")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_medecin"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("specialite"),
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

    private void openAddDoctorDialog(ActionEvent e) {
        AddDoctorDialog dialog = new AddDoctorDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadDoctors();
    }

    private void openEditDoctorDialog(ActionEvent e) {
        editSelectedDoctor();
    }

    // Surcharge pour permettre l'appel sans ActionEvent
    private void openEditDoctorDialog() {
        editSelectedDoctor();
    }

    private void editSelectedDoctor() {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médecin",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int doctorId = (int) doctorsTable.getValueAt(selectedRow, 0);
        EditDoctorDialog dialog = new EditDoctorDialog((JFrame)SwingUtilities.getWindowAncestor(this), doctorId);
        dialog.setVisible(true);
        loadDoctors();
    }

    private void deleteDoctor(ActionEvent e) {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médecin",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int doctorId = (int) doctorsTable.getValueAt(selectedRow, 0);
        String doctorName = (String) doctorsTable.getValueAt(selectedRow, 1) + " " +
                (String) doctorsTable.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer le médecin " + doctorName + "?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM Medecin WHERE id_medecin = ?")) {

                stmt.setInt(1, doctorId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Médecin supprimé avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadDoctors();
                } else {
                    JOptionPane.showMessageDialog(this, "Échec de la suppression du médecin",
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