package com.mycompany.sewa_camping.ui;

import com.mycompany.sewa_camping.config.Database;
import com.mycompany.sewa_camping.model.Sewa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class FormTransaksi extends JFrame {

    JTextField nama, hp, jumlah, hari, totalAll;
    JComboBox<String> barang;

    JTable table;
    DefaultTableModel model;

    public FormTransaksi() {

        setTitle("Transaksi");
        setSize(900,600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel input = new JPanel(new GridLayout(6,2,10,10));

        nama = new JTextField();
        hp = new JTextField();
        jumlah = new JTextField();
        hari = new JTextField();
        totalAll = new JTextField();
        totalAll.setEditable(false);

        barang = new JComboBox<>();
        loadBarang();

        input.add(new JLabel("Nama"));
        input.add(nama);

        input.add(new JLabel("No HP"));
        input.add(hp);

        input.add(new JLabel("Barang"));
        input.add(barang);

        input.add(new JLabel("Jumlah"));
        input.add(jumlah);

        input.add(new JLabel("Hari"));
        input.add(hari);

        input.add(new JLabel("Total Semua"));
        input.add(totalAll);

        add(input, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID Barang","Nama","Jumlah","Hari","Harga","Total"},0);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btn = new JPanel();

        JButton tambah = new JButton("Tambah Item");
        JButton hapus = new JButton("Hapus Item");
        JButton sewa = new JButton("Sewa");

        btn.add(tambah);
        btn.add(hapus);
        btn.add(sewa);

        add(btn, BorderLayout.SOUTH);

        tambah.addActionListener(e -> tambahItem());
        hapus.addActionListener(e -> hapusItem());
        sewa.addActionListener(e -> sewa());

        setVisible(true);
    }

    // =========================
    void loadBarang() {
        try (Connection c = Database.getConnection()) {
            ResultSet r = c.createStatement().executeQuery("SELECT * FROM alat");

            while (r.next()) {
                barang.addItem(
                        r.getString("id_alat")+" - "+
                        r.getString("nama_alat")+" - Rp"+r.getInt("harga"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    void tambahItem() {

        if (jumlah.getText().isEmpty() || hari.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,"Jumlah & Hari wajib diisi!");
            return;
        }

        try (Connection c = Database.getConnection()) {

            String selected = barang.getSelectedItem().toString();
            String[] split = selected.split(" - ");

            String id = split[0];
            String namaBarang = split[1];

            int jml = Integer.parseInt(jumlah.getText());
            int hr = Integer.parseInt(hari.getText());

            PreparedStatement p = c.prepareStatement(
                    "SELECT stok, harga FROM alat WHERE id_alat=?");
            p.setString(1, id);

            ResultSet r = p.executeQuery();

            if (r.next()) {

                int stok = r.getInt("stok");
                int harga = r.getInt("harga");

                if (jml > stok) {
                    JOptionPane.showMessageDialog(this,"Stok tidak cukup!");
                    return;
                }

                // 🔥 PAKAI ABSTRACT + INTERFACE
                Sewa s = new Sewa("TMP");
                int total = s.hitungTotal(jml, harga, hr);

                model.addRow(new Object[]{
                        id, namaBarang, jml, hr, harga, total
                });

                hitungTotal();

                jumlah.setText("");
                hari.setText("");
                jumlah.requestFocus();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,"Jumlah & Hari harus angka!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Error: "+e.getMessage());
        }
    }

    // =========================
    void hapusItem() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.removeRow(row);
            hitungTotal();
        }
    }

    // =========================
    void hitungTotal() {
        int total = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            total += Integer.parseInt(model.getValueAt(i,5).toString());
        }

        totalAll.setText(String.valueOf(total));
    }

    // =========================
    String generateIdTransaksi(Connection c) throws Exception {
        ResultSet r = c.createStatement().executeQuery(
                "SELECT MAX(id_transaksi) FROM transaksi");

        if (r.next() && r.getString(1) != null) {
            int num = Integer.parseInt(r.getString(1).substring(3)) + 1;
            return String.format("TRX%04d", num);
        }
        return "TRX0001";
    }

    String generateIdPenyewa(Connection c) throws Exception {
        ResultSet r = c.createStatement().executeQuery(
                "SELECT MAX(id_penyewa) FROM penyewa");

        if (r.next() && r.getString(1) != null) {
            int num = Integer.parseInt(r.getString(1).substring(2)) + 1;
            return String.format("SW%04d", num);
        }
        return "SW0001";
    }

    // =========================
    void sewa() {

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,"Belum ada item!");
            return;
        }

        try (Connection c = Database.getConnection()) {

            String idT = generateIdTransaksi(c);
            String idP = generateIdPenyewa(c);

            PreparedStatement p1 = c.prepareStatement(
                    "INSERT INTO penyewa VALUES (?,?,?)");
            p1.setString(1, idP);
            p1.setString(2, nama.getText());
            p1.setString(3, hp.getText());
            p1.executeUpdate();

            Date now = Date.valueOf(LocalDate.now());
            Date kembali = Date.valueOf(LocalDate.now().plusDays(1));

            PreparedStatement p2 = c.prepareStatement(
                    "INSERT INTO transaksi VALUES (?,?,?,?,?,?,?)");

            p2.setString(1, idT);
            p2.setString(2, idP);
            p2.setDate(3, now);
            p2.setDate(4, kembali);
            p2.setNull(5, Types.DATE);
            p2.setInt(6, 0);
            p2.setString(7, "Dipinjam");
            p2.executeUpdate();

            // 🔥 FIX UTAMA
            for (int i = 0; i < model.getRowCount(); i++) {

                PreparedStatement p3 = c.prepareStatement(
                        "INSERT INTO detail_transaksi (id_transaksi,id_alat,jumlah,hari) VALUES (?,?,?,?)");

                p3.setString(1, idT);
                p3.setString(2, model.getValueAt(i,0).toString());
                p3.setInt(3, Integer.parseInt(model.getValueAt(i,2).toString()));
                p3.setInt(4, Integer.parseInt(model.getValueAt(i,3).toString()));

                p3.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,"Transaksi berhasil!");

            cetakStrukSewa(idT);

            model.setRowCount(0);
            totalAll.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Error: "+e.getMessage());
        }
    }

    // =========================
    void cetakStrukSewa(String idT) {

        try (Connection c = Database.getConnection()) {

            StringBuilder s = new StringBuilder();

            s.append("===== CAMPING HOLIC =====\n");
            s.append("STRUK PENYEWAAN\n");
            s.append("-------------------------\n");

            PreparedStatement p = c.prepareStatement(
                    "SELECT p.nama, p.no_hp FROM transaksi t JOIN penyewa p ON t.id_penyewa=p.id_penyewa WHERE t.id_transaksi=?");

            p.setString(1, idT);
            ResultSet r = p.executeQuery();

            if (r.next()) {
                s.append("ID   : ").append(idT).append("\n");
                s.append("Nama : ").append(r.getString(1)).append("\n");
                s.append("HP   : ").append(r.getString(2)).append("\n");
            }

            s.append("-------------------------\n");

            PreparedStatement d = c.prepareStatement(
                    "SELECT a.nama_alat, d.jumlah, d.hari, a.harga FROM detail_transaksi d JOIN alat a ON d.id_alat=a.id_alat WHERE d.id_transaksi=?");

            d.setString(1, idT);
            ResultSet rd = d.executeQuery();

            int total = 0;

            while (rd.next()) {
                int t = rd.getInt("jumlah") * rd.getInt("hari") * rd.getInt("harga");
                total += t;

                s.append(rd.getString("nama_alat")).append("\n");
                s.append("  ").append(rd.getInt("jumlah"))
                 .append(" x ").append(rd.getInt("hari"))
                 .append(" x ").append(rd.getInt("harga"))
                 .append(" = ").append(t).append("\n");
            }

            s.append("-------------------------\n");
            s.append("TOTAL : ").append(total).append("\n");

            tampilStruk(s.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }

    void tampilStruk(String isi) {
        JTextArea area = new JTextArea(isi);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area));
    }
}