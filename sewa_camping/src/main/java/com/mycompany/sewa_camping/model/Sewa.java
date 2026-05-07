package com.mycompany.sewa_camping.model;

public class Sewa extends Transaksi {

    public Sewa(String id) {
        this.idTransaksi = id;
    }

    @Override
    public void proses() {
        System.out.println("Proses transaksi sewa");
    }
}