package com.dryt.quoridor.utils;

public final class GameConstants {
    // Dimensions du plateau
    public static final int BOARD_SIZE = 9;
    public static final int CELL_SIZE = 70;
    public static final int WALL_SIZE = 8;
    public static final double OFFSET_X = 40;
    public static final double OFFSET_Y = 40;

    // Nombre de murs par joueur
    public static final int WALLS_PER_PLAYER = 10;

    // Délais pour l'IA
    public static final int AI_THINKING_DELAY = 500;
    public static final int RENDER_DELAY = 50;

    // Messages d'erreur
    public static final String ERROR_NO_IA = "Erreur: Pas d'IA trouvée pour le joueur %d";
    public static final String ERROR_INVALID_MOVE = "Erreur: L'IA a proposé un mouvement invalide!";
    public static final String ERROR_INVALID_WALL = "Erreur: L'IA a proposé un placement de mur invalide!";
    public static final String ERROR_NO_SKIN = "Erreur: Skin non sélectionné pour le joueur ID: %d";
    public static final String ERROR_NO_DIFFICULTE = "Erreur : Pas de difficulté trouvée pour l'IA %d";

    // Messages de jeu
    public static final String MSG_WALL_CROSSING = "Croisement de mur interdit.";
    public static final String MSG_WALL_BLOCKING = "Ce mur bloquerait un joueur complètement.";
    public static final String MSG_WALL_OVERLAP = "Chevauchement de mur interdit.";
    public static final String MSG_WALL_ALREADY = "Un mur est déjà présent ici.";
    public static final String MSG_GAME_OVER = "Partie terminée";
    public static final String MSG_PLAYER_WINS = "Le joueur %d a gagné !";
    public static final String MSG_WALLS_REMAINING = "Murs restants : %d";

    private GameConstants() {
        // Empêche l'instanciation
    }
} 