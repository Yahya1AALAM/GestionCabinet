package cabinet.views;

import cabinet.model.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardPanel extends JPanel {
    private StatCard patientsTodayCard, appointmentsTodayCard, monthlyRevenueCard, availableDoctorsCard;

    public DashboardPanel() {
        initComponents();
        loadStatistics();
    }

    private void initComponents() {
        setLayout(new GridLayout(2, 2, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 240, 240));

        patientsTodayCard = new StatCard("Patients aujourd'hui", "0", new Color(65, 105, 225)); // RoyalBlue
        appointmentsTodayCard = new StatCard("Rendez-vous aujourd'hui", "0", new Color(46, 139, 87)); // SeaGreen
        monthlyRevenueCard = new StatCard("Revenue mensuel", "0 DH", new Color(255, 140, 0)); // DarkOrange
        availableDoctorsCard = new StatCard("Médecins disponibles", "0", new Color(178, 34, 34)); // FireBrick

        add(patientsTodayCard);
        add(appointmentsTodayCard);
        add(monthlyRevenueCard);
        add(availableDoctorsCard);
    }

    private void loadStatistics() {
        try {
            // Patients aujourd'hui
            int patientsToday = getTodayPatientsCount();
            patientsTodayCard.setValue(String.valueOf(patientsToday));

            // Rendez-vous aujourd'hui
            int appointmentsToday = getTodayAppointmentsCount();
            appointmentsTodayCard.setValue(String.valueOf(appointmentsToday));

            // Revenue mensuel
            double monthlyRevenue = getMonthlyRevenue();
            monthlyRevenueCard.setValue(String.format("%.2f DH", monthlyRevenue));

            // Médecins disponibles
            int availableDoctors = getAvailableDoctorsCount();
            availableDoctorsCard.setValue(String.valueOf(availableDoctors));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des statistiques: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private int getTodayPatientsCount() throws SQLException {
        // Utilisez une colonne existante ou modifiez selon votre schéma
        String sql = "SELECT COUNT(*) FROM Patient WHERE DATE(date_creation) = CURDATE()";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private int getTodayAppointmentsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM RendezVous WHERE DATE(date_heure_debut) = CURDATE() AND statut != 'Annulé'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private double getMonthlyRevenue() {
        try {
            // Vérifier si la table Facture existe
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'cabinet' AND table_name = 'Facture'";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkTableSql);
                 ResultSet rs = checkStmt.executeQuery()) {

                if (rs.next() && rs.getInt(1) > 0) {
                    // La table existe, on peut exécuter la requête
                    String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM Facture WHERE MONTH(date_emission) = MONTH(CURDATE()) AND YEAR(date_emission) = YEAR(CURDATE())";
                    try (PreparedStatement stmt = conn.prepareStatement(sql);
                         ResultSet resultSet = stmt.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getDouble(1);
                        }
                    }
                } else {
                    // La table n'existe pas encore, retourner 0
                    return 0;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération du revenue mensuel: " + ex.getMessage());
            ex.printStackTrace();
        }
        return 0;
    }

    private int getAvailableDoctorsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Medecin";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
