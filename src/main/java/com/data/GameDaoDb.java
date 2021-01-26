package com.data;

import com.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;

@Repository
@Profile("database")
public class GameDaoDb implements GameDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GameDaoDb(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Game> getAllGames() {
        final String SELECT_ALL_GAMES = "SELECT * FROM Game";
        return jdbcTemplate.query(SELECT_ALL_GAMES, new GameMapper());
    }

    @Override
    public Game getGameById(int id) {
        try {
            final String SELECT_GAME_BY_ID = "SELECT * FROM Game WHERE GameId = ?";
            return jdbcTemplate.queryForObject(SELECT_GAME_BY_ID, new GameMapper(), id);
        } catch(DataAccessException ex) {
            return null;
        }
    }

    @Override
    public Game addGame(Game game) {
        final String INSERT_GAME = "INSERT INTO Game(Answer, Status) VALUES(?,?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((Connection conn) -> {

                    PreparedStatement statement = conn.prepareStatement(
                            INSERT_GAME,
                            Statement.RETURN_GENERATED_KEYS);

                    statement.setInt(1, game.getAnswer());
                    statement.setBoolean(2, game.getStatus());

                    return statement;
                }, keyHolder );
        game.setGameId(keyHolder.getKey().intValue());
        return game;
    }

    @Override
    public void updateGame(Game game) {
        final String UPDATE_GAME = "UPDATE Game SET Answer = ?, Status = ? WHERE GameId = ?";
        jdbcTemplate.update(UPDATE_GAME,
                game.getAnswer(),
                game.getStatus(),
                game.getGameId());
    }

    @Override
    @Transactional
    public void deleteGameById(int id) {
        final String DELETE_GAME_ROUND = "DELETE FROM Round WHERE GameId = ?";
        jdbcTemplate.update(DELETE_GAME_ROUND, id);
        final String DELETE_MEETING = "DELETE FROM Game WHERE GameId = ?";
        jdbcTemplate.update(DELETE_MEETING, id);
    }

    public static final class GameMapper implements RowMapper<Game> {

        @Override
        public Game mapRow(ResultSet rs, int index) throws SQLException {
            Game gm = new Game();
            gm.setGameId(rs.getInt("GameId"));
            gm.setAnswer(rs.getInt("Answer"));
            gm.setStatus((rs.getBoolean("Status")));

            return gm;
        }
    }
}
