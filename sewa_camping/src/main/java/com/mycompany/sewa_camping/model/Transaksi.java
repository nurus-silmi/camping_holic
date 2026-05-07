package com.mycompany.sewa_camping.model;

public abstract class Transaksi implements HitungTotal {

    protected String idTransaksi;

    public abstract void proses();

    @Override
    public int hitungTotal(int jumlah, int harga, int hari) {
        return jumlah * harga * hari;
    }
}