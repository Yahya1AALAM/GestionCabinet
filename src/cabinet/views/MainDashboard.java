package cabinet.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainDashboard extends JFrame{
    private final String role;
    private final int userId; // Stocker l'ID de l'utilisateur connecté
    private JLabel statusBar = new JLabel("Prêt");


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

            // Option pour créer un secrétaire
            JMenuItem createSecretaryItem = new JMenuItem("Créer un secrétaire");
            createSecretaryItem.addActionListener(this::openCreateSecretary);
            adminMenu.add(createSecretaryItem);

            menuBar.add(adminMenu);
        }
        add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(menuBar);

        // Contenu principal
        JLabel welcomeLabel = new JLabel(
                "<html><div style='text-align:center; font-size:24px;'>"
                        + "Bienvenue dans le système de gestion de cabinet médical<br>"
                        + "Vous êtes connecté en tant que <b>" + role + "</b>"
                        + "</div></html>",
                SwingConstants.CENTER
        );

        add(welcomeLabel, BorderLayout.CENTER);
    }

    private void openCreateSecretary(ActionEvent e) {
        CreateSecretaryDialog dialog = new CreateSecretaryDialog(this, userId);
        dialog.setVisible(true);
    }

    public void setStatus(String message) {
        statusBar.setText(message);
    }
}
