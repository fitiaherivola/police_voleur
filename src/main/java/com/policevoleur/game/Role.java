package com.policevoleur.game;

/** Les deux camps. Sert aussi a designer a qui est le tour. */
public enum Role {
    POLICE("Policiers"),
    THIEF("Voleur");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public Role opponent() {
        return this == POLICE ? THIEF : POLICE;
    }
}
