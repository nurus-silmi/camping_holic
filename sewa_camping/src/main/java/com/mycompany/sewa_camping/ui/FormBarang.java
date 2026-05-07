package com.mycompany.sewa_camping.ui;

import com.mycompany.sewa_camping.config.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormBarang extends JFrame {

    JTextField id, nama, stok, harga;
    JTable table;
    DefaultTableModel model;

    public FormBarang() {

        setTitle("Data Barang");
        setSize(850,400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== INPUT =====
        JPanel input = new JPanel(new GridLayout(4,2,10,10));

        id = new JTextField();
        id.setEditable(false); // auto ID

        nama = new JTextField();
        stok = new JTextField();
        harga = new JTextField();

        input.add(new JLabel("ID"));
        input.add(id);
        input.add(new JLabel("Nama"));
        input.add(nama);
        input.add(new JLabel("Stok"));
        input.add(stok);
        input.add(new JLabel("Harga"));
        input.add(harga);

        add(input, BorderLayout.WEST);

        // ===== TABEL =====
        model = new DefaultTableModel(
                new String[]{"ID","Nama","Stok","Harga"},0);

        table = new JTable(model);

        // ===== WARNA STOK =====
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable table,Object value,
                    boolean isSelected, boolean hasFocus,int row,int column){

                Component c = super.getTableCellRendererComponent(
                        table,value,isSelected,hasFocus,row,column);

                if (!isSelected) {
                    int s = Integer.parseInt(table.getValueAt(row,2).toString());

                    if (s == 0) c.setBackground(new Color(255,102,102));
                    else if (s < 5) c.setBackground(new Color(255,204,102));
                    else c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON =====
        JPanel btn = new JPanel();

        JButton tambah = new JButton("Tambah");
        JButton update = new JButton("Update");
        JButton hapus = new JButton("Hapus");

        btn.add(tambah);
        btn.add(update);
        btn.add(hapus);

        add(btn, BorderLayout.SOUTH);

        // ===== EVENT =====
        tambah.addActionListener(e -> tambah());
        update.addActionListener(e -> update());
        hapus.addActionListener(e -> hapus());

        table.getSelectionModel().addListSelectionListener(e -> isiField());

        tampil();
        generateId(); // auto ID awal

        setVisible(true);
    }

    // ================= AUTO ID =================
    void generateId() {
        try (Connection c = Database.getConnection()) {

            String idBaru = "ALT001";

            ResultSet r = c.createStatement().executeQuery(
                "SELECT id_alat FROM alat ORDER BY id_alat DESC LIMIT 1"
            );

            if (r.next()) {
                String last = r.getString(1).substring(3);
                int num = Integer.parseInt(last) + 1;
                idBaru = String.format("ALT%03d", num);
            }

            id.setText(idBaru);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= VALIDASI =================
    boolean validasi() {
        if (nama.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,"Nama wajib diisi!");
            return false;
        }

        try {
            Integer.parseInt(stok.getText());
            Integer.parseInt(harga.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Stok & Harga harus angka!");
            return false;
        }

        return true;
    }

    // ================= TAMPIL =================
    void tampil() {
        model.setRowCount(0);

        try (Connection c = Database.getConnection()) {
            ResultSet r = c.createStatement().executeQuery("SELECT * FROM alat");

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getString("id_alat"),
                        r.getString("nama_alat"),
                        r.getInt("stok"),
                        r.getInt("harga")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ISI FIELD =================
    void isiField() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            id.setText(model.getValueAt(row,0).toString());
            nama.setText(model.getValueAt(row,1).toString());
            stok.setText(model.getValueAt(row,2).toString());
            harga.setText(model.getValueAt(row,3).toString());
        }
    }

    // ================= TAMBAH =================
    void tambah() {

        if (!validasi()) return;

        try (Connection c = Database.getConnection()) {

            PreparedStatement p = c.prepareStatement(
                    "INSERT INTO alat VALUES (?,?,?,?)"
            );

            p.setString(1, id.getText());
            p.setString(2, nama.getText());
            p.setInt(3, Integer.parseInt(stok.getText()));
            p.setInt(4, Integer.parseInt(harga.getText()));

            p.executeUpdate();

            tampil();
            generateId(); // ID baru

            JOptionPane.showMessageDialog(this,"Data berhasil ditambah!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UPDATE =================
    void update() {

        if (!validasi()) return;

        try (Connection c = Database.getConnection()) {

            PreparedStatement p = c.prepareStatement(
                    "UPDATE alat SET nama_alat=?, stok=?, harga=? WHERE id_alat=?"
            );

            p.setString(1, nama.getText());
            p.setInt(2, Integer.parseInt(stok.getText()));
            p.setInt(3, Integer.parseInt(harga.getText()));
            p.setString(4, id.getText());

            p.executeUpdate();

            tampil();

            JOptionPane.showMessageDialog(this,"Data berhasil diupdate!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= HAPUS =================
    void hapus() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus data ini?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection c = Database.getConnection()) {

            PreparedStatement p = c.prepareStatement(
                    "DELETE FROM alat WHERE id_alat=?"
            );

            p.setString(1, id.getText());
            p.executeUpdate();

            tampil();
            generateId();

            JOptionPane.showMessageDialog(this,"Data berhasil dihapus!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}