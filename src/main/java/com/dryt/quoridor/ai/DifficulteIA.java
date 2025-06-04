package com.dryt.quoridor.ai;

public enum DifficulteIA {
    FACILE(2),
    MOYEN(4),
    DIFFICILE(6);

    private final int profondeur;

    DifficulteIA(int profondeur) {
        this.profondeur = profondeur;
    }

    public int getProfondeur() {
        return profondeur;
    }

    public static MinimaxAI createAI(DifficulteIA difficulte) {
        return new MinimaxAI(difficulte.getProfondeur());
    }
} 