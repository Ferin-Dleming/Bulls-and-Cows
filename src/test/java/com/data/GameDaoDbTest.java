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
public class GameDaoDbTest{

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
    public void testGetAllGames() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        gameDao.addGame(game);
        Game game2 = new Game();
        game2.setGameId(2);
        game2.setStatus(true);
        game2.setAnswer(4321);
        gameDao.addGame(game2);
        Game game3 = new Game();
        game3.setGameId(3);
        game3.setStatus(true);
        game3.setAnswer(9876);
        gameDao.addGame(game3);
        List<Game> games = gameDao.getAllGames();
        assertEquals(3, games.size());
        assertTrue(games.contains(game));
        assertTrue(games.contains(game2));
        assertTrue(games.contains(game3));
    }

    @Test
    public void testAddGetGame() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);
        Game fromDao = gameDao.getGameById(game.getGameId());
        assertEquals(game, fromDao);
    }

    @Test
    public void testUpdateGame() {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);
        Game fromDao = gameDao.getGameById(game.getGameId());
        assertEquals(game, fromDao);
        game.setStatus(false);
        gameDao.updateGame(game);
        assertNotEquals(game, fromDao);
        fromDao = gameDao.getGameById(game.getGameId());
        assertEquals(game, fromDao);
    }

    @Test
    public void testDeleteGameById() throws ParseException {
        Game game = new Game();
        game.setGameId(1);
        game.setStatus(true);
        game.setAnswer(1234);
        game = gameDao.addGame(game);

        Round round = new Round();
        round.setRoundId(1);
        round.setGuess("1234");
        round.setTime(LocalDateTime.now());
        round.setGuessResult("e0:p:0");
        round.setGame(game);
        round = roundDao.addRound(round);

        gameDao.deleteGameById(game.getGameId());
        Game fromDao = gameDao.getGameById(game.getGameId());
        assertNull(fromDao);
    }
}