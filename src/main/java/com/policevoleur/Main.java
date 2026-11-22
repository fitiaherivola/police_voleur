package com.policevoleur;

import com.policevoleur.ui.PoliceVoleurApp;
import javafx.application.Application;

/**
 * Point d'entree. Classe distincte de l'Application JavaFX pour pouvoir aussi etre lancee
 * depuis un IDE sans erreur "JavaFX runtime components are missing".
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(PoliceVoleurApp.class, args);
    }
}
