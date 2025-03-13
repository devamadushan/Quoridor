import com.dryt.quoridor.gameLogic.GameInstance;
import com.dryt.quoridor.model.Player;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class gameInstanceTest {

    @Test
    void testGameInstanceCreation() {
        GameInstance j1 = new GameInstance(9, 2, 0, 10);
        assertNotNull(j1);
    }

    @Test
    void testGetPlayer() {
        GameInstance game = new GameInstance(9, 2, 0, 10);
        Player player0 = game.getPlayer(0);
        Player player1 = game.getPlayer(1);

        assertNotNull(player0);
        assertEquals("Player0", player0.getNom());

        assertNotNull(player1);
        assertEquals("Player1", player1.getNom());

        assertEquals(9, player0.getX());
        assertEquals(1, player0.getY());
        assertEquals(1, player1.getX());
        assertEquals(9, player1.getY());
        ///System.out.println(player0.getNom());
    }

    @Test
    void testTurnSwitch() {
        String simulatedInput = "1\nZ\n1\nD\n2\n4 4 H\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        GameInstance game = new GameInstance(9, 2, 0, 10);


        Player firstPlayer = game.getCurrentPlayer();

        boolean gameRunning = game.playTurn();
        assertTrue(gameRunning);

        Player currentPlayer = game.getCurrentPlayer();
        assertNotEquals(firstPlayer, currentPlayer);

    }

    @Test
    void testPlayerMovements() {
        String simulatedInput = "1\nS\n1\nQ\n2\n4 4 H\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        GameInstance game = new GameInstance(9, 2, 0, 10);


        Player firstPlayer = game.getCurrentPlayer();

        boolean gameRunning = game.playTurn();
        assertTrue(gameRunning);

        Player currentPlayer = game.getCurrentPlayer();
        System.out.println(currentPlayer.getX()+" "+currentPlayer.getY());

        game.playTurn();
        Player currentPlayer2 = game.getCurrentPlayer();
        System.out.println(currentPlayer2.getX()+" "+currentPlayer2.getY());





    }
}

