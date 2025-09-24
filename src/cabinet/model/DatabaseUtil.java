package cabinet.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cabinet";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trouvé", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur de hachage SHA-256", e);
        }
    }

    public static boolean savePatient(String nom, String prenom, String cin, java.sql.Date dateNaissance, String tel,
                                      String email, String sexe, String adresse) throws SQLException {
        String sql = "INSERT INTO Patient (nom, prenom, cin, date_naissance, telephone, email, sexe, adresse) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, cin);
                if (dateNaissance != null) {
                    stmt.setDate(4, dateNaissance);
                } else {
                    stmt.setNull(4, Types.DATE);
                }
                stmt.setString(5, tel);
                stmt.setString(6, email);
                stmt.setString(7, sexe);
                stmt.setString(8, adresse);

                int rowsAffected = stmt.executeUpdate();
                conn.commit();
                return rowsAffected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public static boolean updatePatient(int id, String nom, String prenom, String cin, java.sql.Date dateNaissance,
                                        String tel, String email, String sexe, String adresse) throws SQLException {
        String sql = "UPDATE Patient SET nom=?, prenom=?, cin=?, date_naissance=?, telephone=?, email=?, sexe=?, adresse=? "
                + "WHERE id_patient=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, cin);

            if (dateNaissance != null) {
                stmt.setDate(4, dateNaissance);
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, tel);
            stmt.setString(6, email);
            stmt.setString(7, sexe);
            stmt.setString(8, adresse);
            stmt.setInt(9, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean saveAppointment(int patientId, int doctorId, Timestamp dateTime,
                                          String status, Integer testTypeId) throws SQLException {
        String sql = "INSERT INTO RendezVous (patient_id, medecin_id, date_heure_debut, statut, type_test_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setTimestamp(3, dateTime);
            stmt.setString(4, status);

            if (testTypeId != null) {
                stmt.setInt(5, testTypeId);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean updateAppointment(int appointmentId, int patientId, int doctorId,
                                            Timestamp dateTime, String status, Integer testTypeId) throws SQLException {
        String sql = "UPDATE RendezVous SET patient_id=?, medecin_id=?, date_heure_debut=?, "
                + "statut=?, type_test_id=? WHERE id_rdv=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setTimestamp(3, dateTime);
            stmt.setString(4, status);

            if (testTypeId != null) {
                stmt.setInt(5, testTypeId);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setInt(6, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean updateDoctor(int id, String nom, String prenom, String specialite,
                                       String telephone, String email) throws SQLException {
        String sql = "UPDATE Medecin SET nom=?, prenom=?, specialite=?, telephone=?, email=? WHERE id_medecin=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, specialite);
            stmt.setString(4, telephone);
            stmt.setString(5, email);
            stmt.setInt(6, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
