package com.mycompany.sewa_camping.service;

import com.mycompany.sewa_camping.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TransaksiService {

    private Connection conn;

    public TransaksiService() {
        conn = Database.getConnection();
    }

    // ===== TAMBAH PENYEWA (FIX no_hp + aman) =====
    public void tambahPenyewa(String id, String nama, String no_hp) {
        try {
            PreparedStatement p = conn.prepareStatement(
                "INSERT INTO penyewa (id_penyewa, nama, no_hp) VALUES (?,?,?)"
            );

            p.setString(1, id);
            p.setString(2, nama);
            p.setString(3, no_hp);

            p.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}