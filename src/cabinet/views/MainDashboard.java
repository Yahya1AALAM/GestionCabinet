package cabinet.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainDashboard extends JFrame{
    private final String role;
    private final int userId;
    private JLabel statusBar = new JLabel("Prêt");

    // Classe interne DashboardPanel professionnelle et sobre
    private class DashboardPanel extends JPanel {
        public DashboardPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(250, 250, 250));
            setBorder(new EmptyBorder(20, 20, 20, 20));

            createProfessionalDashboard();
        }

        private void createProfessionalDashboard() {
            // Header simple
            JPanel headerPanel = createHeaderPanel();
            add(headerPanel, BorderLayout.NORTH);

            // Statistiques essentielles
            JPanel statsPanel = createStatsPanel();
            add(statsPanel, BorderLayout.CENTER);
        }

        private JPanel createHeaderPanel() {
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(250, 250, 250));
            headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

            JLabel welcomeLabel = new JLabel(
                    "<html><div style='color: #2c3e50;'>"
                            + "<span style='font-size:24px; font-weight:bold;'>Tableau de Bord - Cabinet Médical</span><br>"
                            + "<span style='font-size:14px; color: #7f8c8d;'>Connecté en tant que " + role + "</span>"
                            + "</div></html>"
            );

            headerPanel.add(welcomeLabel, BorderLayout.WEST);
            return headerPanel;
        }

        private JPanel createStatsPanel() {
            JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
            statsPanel.setBackground(new Color(250, 250, 250));

            // Seulement les statistiques essentielles pour la gestion
            statsPanel.add(createStatCard("Patients Aujourd'hui", "12", new Color(41, 128, 185)));
            statsPanel.add(createStatCard("Rendez-vous Aujourd'hui", "8", new Color(39, 174, 96)));
            statsPanel.add(createStatCard("Revenue Mensuel", "131,111.00 DH", new Color(142, 68, 173)));
            statsPanel.add(createStatCard("Médecins Disponibles", "3", new Color(243, 156, 18)));

            return statsPanel;
        }

        private JPanel createStatCard(String title, String value, Color color) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    new EmptyBorder(20, 20, 20, 20)
            ));

            // Titre
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(new Color(80, 80, 80));

            // Valeur
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
            valueLabel.setForeground(color);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(valueLabel);

            card.add(contentPanel, BorderLayout.CENTER);
            return card;
        }
    }

    private JPanel createDashboardPanel() {
        return new DashboardPanel();
    }

    public MainDashboard(String role, int userId) {
        this.role = role;
        this.userId = userId;
        initComponents();
        getContentPane().add(new DashboardPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void initComponents() {
        setTitle("Tableau de Bord - Cabinet Médical [" + role + "]");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();

        // Menu Fichier
        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Menu Accueil
        JMenu homeMenu = new JMenu("Accueil");
        JMenuItem dashboardItem = new JMenuItem("Tableau de Bord");
        dashboardItem.addActionListener(e -> {
            getContentPane().removeAll();
            add(new DashboardPanel(), BorderLayout.CENTER);
            revalidate();
            repaint();
            setStatus("Tableau de bord");
        });
        homeMenu.add(dashboardItem);
        menuBar.add(homeMenu);

        // Menu Patients (commun)
        JMenu patientsMenu = new JMenu("Patients");
        JMenuItem listPatientsItem = new JMenuItem("Liste des patients");
        listPatientsItem.addActionListener(e -> {
            getContentPane().removeAll();
            add(new PatientManagementView(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
        patientsMenu.add(listPatientsItem);

        menuBar.add(patientsMenu);

        // Menu Médecins (uniquement pour les administrateurs)
        if ("Medecin".equals(role) || "Secretaire".equals(role)) {
            JMenu doctorsMenu = new JMenu("Médecins");
            JMenuItem listDoctorsItem = new JMenuItem("Gestion des médecins");
            listDoctorsItem.addActionListener(e -> {
                getContentPane().removeAll();
                add(new DoctorManagementView(), BorderLayout.CENTER);
                revalidate();
                repaint();
            });
            doctorsMenu.add(listDoctorsItem);
            menuBar.add(doctorsMenu);
        }

        // Menu Rendez-vous
        JMenu rdvMenu = new JMenu("Rendez-vous");
        JMenuItem listRdvItem = new JMenuItem("Gestion des rendez-vous");
        listRdvItem.addActionListener(e -> {
            getContentPane().removeAll();
            add(new AppointmentManagementView(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
        rdvMenu.add(listRdvItem);
        menuBar.add(rdvMenu);

        // Ajouter la gestion des patients au menu
        JMenuItem patientsItem = new JMenuItem("Gestion des patients");
        patientsItem.addActionListener(e -> {
            getContentPane().removeAll();
            add(new PatientManagementView(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
        patientsMenu.add(patientsItem);

        // Menu Facturation (uniquement pour les médecins et secrétaires)
        if ("Medecin".equals(role) || "Secretaire".equals(role)) {
            JMenu billingMenu = new JMenu("Facturation");
            JMenuItem billingItem = new JMenuItem("Gestion des factures");
            billingItem.addActionListener(e -> {
                getContentPane().removeAll();
                add(new BillingManagementView(), BorderLayout.CENTER);
                revalidate();
                repaint();
            });
            billingMenu.add(billingItem);
            menuBar.add(billingMenu);
        }

        // Menu Paiements (uniquement pour les médecins et secrétaires)
        if ("Medecin".equals(role) || "Secretaire".equals(role)) {
            JMenu paymentsMenu = new JMenu("Paiements");
            JMenuItem paymentsItem = new JMenuItem("Gestion des paiements");
            paymentsItem.addActionListener(e -> {
                getContentPane().removeAll();
                add(new PaymentManagementView(), BorderLayout.CENTER);
                revalidate();
                repaint();
            });
            paymentsMenu.add(paymentsItem);
            menuBar.add(paymentsMenu);
        }

        // Menu Administration (uniquement pour les médecins)
        if ("Medecin".equals(role)) {
            JMenu adminMenu = new JMenu("Administration");

            JMenuItem createSecretaryItem = new JMenuItem("Créer un secrétaire");
            createSecretaryItem.addActionListener(this::openCreateSecretary);
            adminMenu.add(createSecretaryItem);

            menuBar.add(adminMenu);
        }
        add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(menuBar);

        // Contenu principal - Dashboard par défaut
        add(new DashboardPanel(), BorderLayout.CENTER);
    }

    private void openCreateSecretary(ActionEvent e) {
        CreateSecretaryDialog dialog = new CreateSecretaryDialog(this, userId);
        dialog.setVisible(true);
    }

    public void setStatus(String message) {
        statusBar.setText(message);
    }
}