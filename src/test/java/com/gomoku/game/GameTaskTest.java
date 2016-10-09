package com.gomoku.game;

import static com.gomoku.board.BoardFieldType.NONE;
import static com.gomoku.board.BoardFieldType.PLAYER_O;
import static com.gomoku.board.BoardFieldType.PLAYER_X;
import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.gomoku.board.Board;
import com.gomoku.board.BoardFieldType;
import com.gomoku.player.Player;

/**
 * Unit test for {@link GameTask}.
 *
 * @author zeldan
 *
 */
public class GameTaskTest {

    private static final Player FIRST_PLAYER = new Player("firstPlayer", "http://192.168.0.1:8080");
    private static final Player SECOND_PLAYER = new Player("secondPlayer", "http://192.168.0.2:9000");

    private static final int BOARD_LIMIT_TWO_TO_WIN = 2;
    private static final int BOARD_LIMIT_THREE_TO_WIN = 3;
    private static final int BOARD_WIDTH = 2;
    private static final int BOARD_HEIGHT = 2;

    @Mock
    private GameExecutorService gameService;

    private GameTask underTest;

    @Before
    public void setUp() {
        initMocks(this);
        underTest = new GameTask(BOARD_WIDTH, BOARD_HEIGHT, BOARD_LIMIT_THREE_TO_WIN, gameService);
    }

    @Test
    public void shouldChangeActualPlayerInEveryRound() {
        // GIVEN
        final Board notFullBoard = new Board(BOARD_WIDTH, BOARD_HEIGHT, BOARD_LIMIT_THREE_TO_WIN, new BoardFieldType[][] {
                { PLAYER_X, PLAYER_O },
                { PLAYER_O, NONE }
        });

        final Board fullBoard = new Board(BOARD_WIDTH, BOARD_HEIGHT, BOARD_LIMIT_THREE_TO_WIN, new BoardFieldType[][] {
                { PLAYER_X, PLAYER_O },
                { PLAYER_O, PLAYER_X }
        });

        final GameState gameStateWithNotFullBoard = new GameState(emptyMap(), notFullBoard);
        when(gameService.playOneRound(any(GameState.class), eq(PLAYER_O))).thenReturn(of(gameStateWithNotFullBoard));
        when(gameService.playOneRound(eq(gameStateWithNotFullBoard), eq(PLAYER_X))).thenReturn(of(gameStateWithNotFullBoard));
        when(gameService.playOneRound(eq(gameStateWithNotFullBoard), Mockito.eq(PLAYER_O))).thenReturn(of(new GameState(emptyMap(), fullBoard)));

        // WHEN
        final GameTaskResult gameTaskResult = underTest.matchAgainstEachOther(FIRST_PLAYER, SECOND_PLAYER);

        // THEN
        assertFalse(gameTaskResult.getWinner().isPresent());
        final InOrder inOrder = inOrder(gameService);
        inOrder.verify(gameService).playOneRound(any(GameState.class), eq(PLAYER_O));
        inOrder.verify(gameService).playOneRound(gameStateWithNotFullBoard, PLAYER_X);
        inOrder.verify(gameService).playOneRound(gameStateWithNotFullBoard, PLAYER_O);
    }

    @Test
    public void shouldPlayerXBeTheWinner() {
        // GIVEN
        final Board fullBoardWithWinnerPlayerX = new Board(BOARD_WIDTH, BOARD_HEIGHT, BOARD_LIMIT_TWO_TO_WIN, new BoardFieldType[][] {
                { PLAYER_X, PLAYER_X },
                { PLAYER_O, PLAYER_X }
        });
        when(gameService.playOneRound(any(GameState.class), eq(PLAYER_O))).thenReturn(of(new GameState(emptyMap(), fullBoardWithWinnerPlayerX)));

        // WHEN
        final GameTaskResult gameTaskResult = underTest.matchAgainstEachOther(FIRST_PLAYER, SECOND_PLAYER);

        // THEN
        final Optional<BoardFieldType> winner = gameTaskResult.getWinner();
        assertTrue(winner.isPresent());
        assertEquals(PLAYER_X, winner.get());

    }

}
