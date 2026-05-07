package com.mycompany.sewa_camping.ui;

import javax.swing.*;
import java.awt.*;

public class MenuUtama extends JFrame {

    public MenuUtama() {

        setTitle("Menu Utama");
        setSize(400,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== PANEL =====
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,1,15,15));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        panel.setBackground(new Color(245,245,245));

        // ===== FONT =====
        Font font = new Font("Segoe UI", Font.BOLD, 14);

        // ===== BUTTON =====
        JButton transaksi = new JButton("Transaksi");
        JButton riwayat = new JButton("Riwayat");
        JButton barang = new JButton("Data Barang");

        JButton[] buttons = {transaksi, riwayat, barang};

        // ===== STYLE BUTTON =====
        for (JButton btn : buttons) {
            btn.setFont(font);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(33,150,243));
            btn.setForeground(Color.WHITE);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        // ===== TAMBAH KE PANEL =====
        panel.add(transaksi);
        panel.add(riwayat);
        panel.add(barang);

        add(panel);

        // ===== EVENT =====
        transaksi.addActionListener(e -> new FormTransaksi());
        riwayat.addActionListener(e -> new FormRiwayat());
        barang.addActionListener(e -> new FormBarang());

        setVisible(true);
    }
}