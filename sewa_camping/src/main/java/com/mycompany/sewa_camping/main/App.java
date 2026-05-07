package com.mycompany.sewa_camping.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.sewa_camping.ui.FormLogin;

public class App {

    public static void main(String[] args) {

        FlatLightLaf.setup(); // UI modern

        new FormLogin();
    }
}