package com.data;

import com.model.Game;
import com.model.Round;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoundDaoDbTest {

    @Autowired
    RoundDaoDb roundDao;

    @Autowired
    GameDaoDb gameDao;

    @Before
    public void setUp() {
        List<Round> rounds = roundDao.getAllRounds();
        for(Round round : rounds) {
            roundDao.deleteRoundById(round.getRoundId());
        }

        List<Game> games = gameDao.getAllGames();
        for(Game game : games) {
            gameDao.deleteGameById(game.getGameId());
        }
    }

    @Test
    public void testGetAllRounds() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);
        Round round = new Round();
        round.setRoundId(1);
        round.setGuess("1234");
        round.setTime(LocalDateTime.now().withNano(0));
        round.setGuessResult("e0:p:0");
        round.setGame(game);
        round = roundDao.addRound(round);

        Round round2 = new Round();
        round2.setRoundId(2);
        round2.setGuess("4321");
        round2.setTime(LocalDateTime.now().withNano(0));
        round2.setGuessResult("e1:p:1");
        round2.setGame(game);
        round2 = roundDao.addRound(round2);

        Round round3 = new Round();
        round3.setRoundId(3);
        round3.setGuess("9876");
        round3.setTime(LocalDateTime.now().withNano(0));
        round3.setGuessResult("e2:p:2");
        round3.setGame(game);
        round3 = roundDao.addRound(round3);

        List<Round> rounds = roundDao.getAllRounds();
        assertEquals(3, rounds.size());
        assertTrue(rounds.contains(round));
        assertTrue(rounds.contains(round2));
        assertTrue(rounds.contains(round3));
    }

    @Test
    public void testGetRoundsForGame() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);

        Game game2 = new Game();
        game2.setGameId(2);
        game2.setStatus(true);
        game2.setAnswer(5678);
        game2 = gameDao.addGame(game2);

        Round round = new Round();
        round.setRoundId(1);
        round.setGuess("1234");
        round.setTime(LocalDateTime.now().withNano(0));
        round.setGuessResult("e0:p:0");
        round.setGame(game);
        round = roundDao.addRound(round);

        Round round2 = new Round();
        round2.setRoundId(2);
        round2.setGuess("5678");
        round2.setTime(LocalDateTime.now().withNano(0));
        round2.setGuessResult("e1:p:1");
        round2.setGame(game2);
        round2 = roundDao.addRound(round2);

        Round round3 = new Round();
        round3.setRoundId(3);
        round3.setGuess("9876");
        round3.setTime(LocalDateTime.now().withNano(0));
        round3.setGuessResult("e2:p:2");
        round3.setGame(game);
        round3 = roundDao.addRound(round3);

        List<Round> roundsForGame = roundDao.getRoundsForGame(game);

        assertEquals(2, roundsForGame.size());
        assertTrue(roundsForGame.contains(round));
        assertTrue(roundsForGame.contains(round3));
        assertFalse(roundsForGame.contains(round2));
    }

    @Test
    public void testAddGetRound() throws ParseException {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);
        Round round = new Round();
        round.setRoundId(1);
        round.setGuess("1234");
        round.setTime(LocalDateTime.now().withNano(0));
        round.setGuessResult("e0:p:0");
        round.setGame(game);
        round = roundDao.addRound(round);
        Round fromDao = roundDao.getRoundById(round.getRoundId());
        assertEquals(round, fromDao);
    }

    @Test
    public void testUpdateRound() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);

        Round round = new Round();
        round.setRoundId(1);
        round.setGuess("1234");
        round.setTime(LocalDateTime.now().withNano(0));
        round.setGuessResult("e0:p:0");
        round.setGame(game);
        round = roundDao.addRound(round);
        Round fromDao = roundDao.getRoundById(round.getRoundId());
        assertEquals(round, fromDao);

        round.setGuess("9876");
        roundDao.updateRound(round);
        assertNotEquals(round, fromDao);
        fromDao = roundDao.getRoundById(round.getRoundId());
        assertEquals(round, fromDao);
    }

    @Test
    public void testDeleteRoundById() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);

        Round round = new Round();
        round.setRoundId(1);
        round.setGuess("1234");
        round.setTime(LocalDateTime.now().withNano(0));
        round.setGuessResult("e0:p:0");
        round.setGame(game);
        round = roundDao.addRound(round);

        roundDao.deleteRoundById(round.getRoundId());
        Round fromDao = roundDao.getRoundById(round.getRoundId());
        assertNull(fromDao);
    }
}