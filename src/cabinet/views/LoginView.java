package cabinet.views;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import cabinet.model.DatabaseUtil;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;

    public LoginView() {
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Connexion - Cabinet Médical");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 500);
        setResizable(false);

        // Panel principal avec fond professionnel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(41, 128, 185), 0, getHeight(), new Color(52, 152, 219));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Panel de contenu avec ombre
        JPanel contentPanel = new JPanel(new BorderLayout(10, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // En-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Cabinet Médical", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));

        JLabel subtitleLabel = new JLabel("Connexion au système", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Icône médicale avec l'image doctor.png
        JLabel iconLabel = createIconLabel();
        contentPanel.add(iconLabel, BorderLayout.NORTH);

        // Formulaire
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Champ nom d'utilisateur
        JPanel usernamePanel = new JPanel(new BorderLayout(10, 5));
        usernamePanel.setBackground(Color.WHITE);
        JLabel usernameLabel = new JLabel("Nom d'utilisateur:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        usernameLabel.setForeground(new Color(52, 73, 94));

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        usernameField.setMaximumSize(new Dimension(300, 35));

        usernamePanel.add(usernameLabel, BorderLayout.NORTH);
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        usernamePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Champ mot de passe
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 5));
        passwordPanel.setBackground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordLabel.setForeground(new Color(52, 73, 94));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setMaximumSize(new Dimension(300, 35));

        passwordPanel.add(passwordLabel, BorderLayout.NORTH);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        formPanel.add(usernamePanel);
        formPanel.add(passwordPanel);
        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        cancelButton = new JButton("Annuler");
        loginButton = new JButton("Se connecter");

        // Style des boutons
        styleButton(cancelButton, new Color(231, 76, 60));
        styleButton(loginButton, new Color(39, 174, 96));

        cancelButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir quitter l'application ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        loginButton.addActionListener(e -> authenticate());

        buttonPanel.add(cancelButton);
        buttonPanel.add(loginButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Pied de page
        JLabel footerLabel = new JLabel("© 2025 Cabinet Médical - Tous droits réservés", JLabel.CENTER);
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // Enter key pour soumettre le formulaire
        getRootPane().setDefaultButton(loginButton);

        // Focus sur le champ username au démarrage
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }

    private JLabel createIconLabel() {
        try {
            // Chargement de l'icône doctor.png
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/cabinet/resources/icons/doctor.png"));

            // Redimensionner l'image pour qu'elle soit adaptée à l'interface
            Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            JLabel iconLabel = new JLabel(scaledIcon, JLabel.CENTER);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            return iconLabel;

        } catch (Exception e) {
            // Fallback vers un emoji si l'image n'est pas trouvée
            System.err.println("Icône non trouvée, utilisation de l'emoji de secours: " + e.getMessage());
            JLabel iconLabel = new JLabel("🏥", JLabel.CENTER);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            return iconLabel;
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Effet hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void authenticate() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty()) {
            showError("Veuillez saisir votre nom d'utilisateur", "Champ obligatoire");
            usernameField.requestFocus();
            return;
        }

        if (password.length == 0) {
            showError("Veuillez saisir votre mot de passe", "Champ obligatoire");
            passwordField.requestFocus();
            return;
        }

        // Désactiver le bouton pendant l'authentification
        loginButton.setEnabled(false);
        loginButton.setText("Connexion...");

        // Simuler un délai pour l'authentification
        new Thread(() -> {
            try {
                Thread.sleep(500); // Petit délai pour l'effet visuel

                SwingUtilities.invokeLater(() -> {
                    try (Connection conn = DatabaseUtil.getConnection()) {
                        String sql = "SELECT u.*, m.nom, m.prenom " +
                                "FROM utilisateur u " +
                                "LEFT JOIN medecin m ON u.medecin_id = m.id_medecin " +
                                "WHERE u.nom_utilisateur = ? AND u.is_active = 1";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, username);

                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    String storedHash = rs.getString("mot_de_passe");
                                    String inputHash = DatabaseUtil.sha256(new String(password));

                                    if (inputHash.equals(storedHash)) {
                                        String role = rs.getString("role");
                                        int userId = rs.getInt("id_utilisateur");

                                        // Récupérer le nom complet de manière intelligente
                                        String nomComplet = getNomComplet(rs, username);

                                        openDashboard(role, userId, nomComplet);
                                    } else {
                                        showError("Le mot de passe saisi est incorrect", "Erreur d'authentification");
                                        passwordField.setText("");
                                        passwordField.requestFocus();
                                    }
                                } else {
                                    showError("Aucun utilisateur trouvé avec ce nom d'utilisateur", "Utilisateur introuvable");
                                    usernameField.requestFocus();
                                }
                            }
                        }
                    } catch (Exception e) {
                        showError("Erreur de connexion à la base de données: " + e.getMessage(), "Erreur système");
                        e.printStackTrace();
                    } finally {
                        Arrays.fill(password, '0');
                        loginButton.setEnabled(true);
                        loginButton.setText("Se connecter");
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                loginButton.setEnabled(true);
                loginButton.setText("Se connecter");
            }
        }).start();
    }

    private String getNomComplet(ResultSet rs, String username) {
        try {
            // Essayer d'abord de récupérer le nom du médecin si disponible
            String nomMedecin = rs.getString("nom");
            String prenomMedecin = rs.getString("prenom");

            if (nomMedecin != null && prenomMedecin != null &&
                    !nomMedecin.trim().isEmpty() && !prenomMedecin.trim().isEmpty()) {
                return nomMedecin + " " + prenomMedecin;
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser le nom d'utilisateur
            System.err.println("Erreur lors de la récupération du nom complet: " + e.getMessage());
        }

        // Fallback: utiliser le nom d'utilisateur formaté
        return username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this,
                "<html><center>" + message + "</center></html>",
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message, String title) {
        JOptionPane.showMessageDialog(this,
                "<html><center>" + message + "</center></html>",
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openDashboard(String role, int userId, String nomComplet) {
        // Animation de fermeture
        setVisible(false);

        // Message de bienvenue personnalisé selon le rôle
        String roleDisplay = "";
        switch(role) {
            case "Medecin":
                roleDisplay = "Docteur";
                break;
            case "Secretaire":
                roleDisplay = "Secrétaire médicale";
                break;
            default:
                roleDisplay = role;
        }

        showSuccess(
                "✅ <b>Authentification réussie</b><br><br>" +
                        "Bienvenue <b>" + nomComplet + "</b><br>" +
                        "Rôle : <b>" + roleDisplay + "</b><br><br>" +
                        "Redirection vers le tableau de bord...",
                "Connexion établie"
        );

        dispose();

        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard(role, userId);
            dashboard.setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}