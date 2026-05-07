package com.mycompany.sewa_camping.ui;

import com.mycompany.sewa_camping.config.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormRiwayat extends JFrame {

    JTable table, tableDetail;
    DefaultTableModel model, modelDetail;

    public FormRiwayat() {

        setTitle("Riwayat Transaksi");
        setSize(900,500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== TABLE ATAS =====
        model = new DefaultTableModel(
                new String[]{"ID","Nama","No HP","Tanggal Sewa","Status"},0);

        table = new JTable(model);

        // ===== TABLE BAWAH =====
        modelDetail = new DefaultTableModel(
                new String[]{
                        "Barang","Jumlah","Tanggal Sewa",
                        "Harus Kembali","Kembali Real",
                        "Denda","Total"
                },0);

        tableDetail = new JTable(modelDetail);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table),
                new JScrollPane(tableDetail)
        );

        split.setDividerLocation(250);

        add(split, BorderLayout.CENTER);

        // ===== BUTTON =====
        JPanel btn = new JPanel();

        JButton kembali = new JButton("Kembalikan");

        btn.add(kembali);
        add(btn, BorderLayout.SOUTH);

        // ===== EVENT =====
        table.getSelectionModel().addListSelectionListener(e -> loadDetail());
        kembali.addActionListener(e -> kembalikan());

        tampil();

        setVisible(true);
    }

    // =========================
    // LOAD DATA ATAS
    // =========================
    void tampil() {
        model.setRowCount(0);

        try (Connection c = Database.getConnection()) {

            ResultSet r = c.createStatement().executeQuery(
                    "SELECT t.id_transaksi, p.nama, p.no_hp, t.tanggal_sewa, t.status " +
                    "FROM transaksi t JOIN penyewa p ON t.id_penyewa=p.id_penyewa");

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getString(1),
                        r.getString(2),
                        r.getString(3),
                        r.getDate(4),
                        r.getString(5)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // LOAD DETAIL
    // =========================
    void loadDetail() {

        int row = table.getSelectedRow();

        if (row < 0) return;

        String idT = model.getValueAt(row,0).toString();

        modelDetail.setRowCount(0);

        try (Connection c = Database.getConnection()) {

            PreparedStatement p = c.prepareStatement(
                    "SELECT a.nama_alat, d.jumlah, t.tanggal_sewa, " +
                    "t.tanggal_kembali, t.tanggal_kembali_real, t.denda, a.harga " +
                    "FROM detail_transaksi d " +
                    "JOIN alat a ON d.id_alat=a.id_alat " +
                    "JOIN transaksi t ON d.id_transaksi=t.id_transaksi " +
                    "WHERE d.id_transaksi=?"
            );

            p.setString(1, idT);

            ResultSet r = p.executeQuery();

            while (r.next()) {

                int jumlah = r.getInt("jumlah");
                int harga = r.getInt("harga");
                int total = jumlah * harga;

                modelDetail.addRow(new Object[]{
                        r.getString("nama_alat"),
                        jumlah,
                        r.getDate("tanggal_sewa"),
                        r.getDate("tanggal_kembali"),
                        r.getDate("tanggal_kembali_real"),
                        r.getInt("denda"),
                        total
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // KEMBALIKAN + STRUK
    // =========================
    void kembalikan() {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this,"Pilih transaksi!");
            return;
        }

        String idT = model.getValueAt(row,0).toString();

        try (Connection c = Database.getConnection()) {

            PreparedStatement cek = c.prepareStatement(
                    "SELECT status, tanggal_kembali FROM transaksi WHERE id_transaksi=?");
            cek.setString(1, idT);

            ResultSet r = cek.executeQuery();

            if (r.next()) {

                if (r.getString("status").equals("Kembali")) {
                    JOptionPane.showMessageDialog(this,"Sudah dikembalikan!");
                    return;
                }

                Date tglHarus = r.getDate("tanggal_kembali");
                Date now = new Date(System.currentTimeMillis());

                int denda = 0;

                if (tglHarus != null) {
                    long selisih = (now.getTime() - tglHarus.getTime()) / (1000*60*60*24);
                    if (selisih > 0) denda = (int) selisih * 10000;
                }

                PreparedStatement p = c.prepareStatement(
                        "UPDATE transaksi SET tanggal_kembali_real=?, denda=?, status='Kembali' WHERE id_transaksi=?");

                p.setDate(1, now);
                p.setInt(2, denda);
                p.setString(3, idT);
                p.executeUpdate();

                JOptionPane.showMessageDialog(this,"Berhasil dikembalikan! Denda: "+denda);

                // 🔥 CETAK STRUK
                cetakStrukKembali(idT);
            }

            tampil();
            loadDetail();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Error: "+e.getMessage());
        }
    }

    // =========================
    // STRUK PENGEMBALIAN
    // =========================
    void cetakStrukKembali(String idT) {

        try (Connection c = Database.getConnection()) {

            StringBuilder s = new StringBuilder();

            s.append("===== CAMPING HOLIC =====\n");
            s.append("STRUK PENGEMBALIAN\n");
            s.append("-------------------------\n");

            PreparedStatement p = c.prepareStatement(
                    "SELECT p.nama, t.tanggal_kembali, t.tanggal_kembali_real, t.denda " +
                    "FROM transaksi t JOIN penyewa p ON t.id_penyewa=p.id_penyewa " +
                    "WHERE t.id_transaksi=?"
            );

            p.setString(1, idT);
            ResultSet r = p.executeQuery();

            if (r.next()) {
                s.append("ID        : ").append(idT).append("\n");
                s.append("Nama      : ").append(r.getString("nama")).append("\n");
                s.append("Harus Kmb : ").append(r.getDate("tanggal_kembali")).append("\n");
                s.append("Kembali   : ").append(r.getDate("tanggal_kembali_real")).append("\n");
                s.append("Denda     : ").append(r.getInt("denda")).append("\n");
            }

            s.append("-------------------------\n");

            PreparedStatement d = c.prepareStatement(
                    "SELECT a.nama_alat, d.jumlah " +
                    "FROM detail_transaksi d " +
                    "JOIN alat a ON d.id_alat=a.id_alat " +
                    "WHERE d.id_transaksi=?"
            );

            d.setString(1, idT);
            ResultSet rd = d.executeQuery();

            while (rd.next()) {
                s.append(rd.getString("nama_alat"))
                 .append(" (").append(rd.getInt("jumlah")).append(")\n");
            }

            s.append("-------------------------\n");
            s.append("Terima kasih 🙏\n");

            tampilStruk(s.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }

    // =========================
    // TAMPILKAN STRUK
    // =========================
    void tampilStruk(String isi) {
        JTextArea area = new JTextArea(isi);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Struk",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}