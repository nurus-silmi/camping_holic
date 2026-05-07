package com.mycompany.sewa_camping.ui;

import com.mycompany.sewa_camping.config.Database;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FormLogin extends JFrame {

    JTextField user;
    JPasswordField pass;
    JButton show, login;

    public FormLogin() {

        setTitle("Login");
        setSize(900,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== FONT =====
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        // ===== BACKGROUND =====
        ImageIcon icon = new ImageIcon(
                new ImageIcon(getClass().getResource("/bg.png"))
                        .getImage()
                        .getScaledInstance(900, 500, Image.SCALE_SMOOTH)
        );

        JLabel background = new JLabel(icon);
        background.setLayout(new GridBagLayout());

        // ===== PANEL =====
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(320,260));
        panel.setBackground(new Color(0,0,0,80));
        panel.setLayout(new GridLayout(3,1,15,15));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));

        // ===== USERNAME =====
        user = new JTextField();
        user.setFont(inputFont);

        TitledBorder userBorder = BorderFactory.createTitledBorder("Username");
        userBorder.setTitleFont(labelFont);
        user.setBorder(userBorder);

        // ===== PASSWORD =====
        pass = new JPasswordField();
        pass.setEchoChar('•');
        pass.setFont(inputFont);

        TitledBorder passBorder = BorderFactory.createTitledBorder("Password");
        passBorder.setTitleFont(labelFont);
        pass.setBorder(passBorder);

        // ===== BUTTON MATA =====
        show = new JButton("👁");
        show.setFocusPainted(false);

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setOpaque(false);
        passPanel.add(pass, BorderLayout.CENTER);
        passPanel.add(show, BorderLayout.EAST);

        // ===== BUTTON LOGIN =====
        login = new JButton("Login");
        login.setFont(new Font("Segoe UI", Font.BOLD, 15));

        // ===== TAMBAH KE PANEL =====
        panel.add(user);
        panel.add(passPanel);
        panel.add(login);

        background.add(panel);
        add(background);

        // ===== EVENT SHOW PASSWORD =====
        show.addActionListener(e -> {
            if (pass.getEchoChar() == (char)0) {
                pass.setEchoChar('•');
            } else {
                pass.setEchoChar((char)0);
            }
        });

        // ===== ENTER NAVIGATION =====
        user.addActionListener(e -> pass.requestFocus());   // Enter → ke password
        pass.addActionListener(e -> login());               // Enter → login

        // Enter global (opsional, tapi mantap)
        getRootPane().setDefaultButton(login);

        // ===== BUTTON LOGIN =====
        login.addActionListener(e -> login());

        setVisible(true);
    }

    // ===== LOGIN =====
    void login() {
        try (Connection c = Database.getConnection()) {

            PreparedStatement p = c.prepareStatement(
                    "SELECT * FROM user WHERE username=? AND password=?"
            );

            p.setString(1, user.getText());
            p.setString(2, new String(pass.getPassword()));

            ResultSet r = p.executeQuery();

            if (r.next()) {
                JOptionPane.showMessageDialog(this, "Login berhasil");
                new MenuUtama();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username / Password salah");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}