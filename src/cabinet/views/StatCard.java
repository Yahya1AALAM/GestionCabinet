package cabinet.views;


import javax.swing.*;
import java.awt.*;

public class StatCard extends JPanel {
    private String title;
    private String value;
    private Color color;
    private JLabel valueLabel;

    public StatCard(String title, String value, Color color) {
        this.title = title;
        this.value = value;
        this.color = color;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(color);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        setPreferredSize(new Dimension(200, 120));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);

        // Ajouter un effet d'ombre
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}