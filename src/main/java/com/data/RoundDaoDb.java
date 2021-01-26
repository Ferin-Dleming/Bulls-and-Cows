package com.data;

import com.model.Game;
import com.model.Round;
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
public class RoundDaoDb implements RoundDao{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RoundDaoDb(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Round getRoundById(int id) {
        try {
            final String SELECT_ROUND_BY_ID = "SELECT * FROM Round WHERE RoundId = ?";
            Round round = jdbcTemplate.queryForObject(SELECT_ROUND_BY_ID, new RoundMapper(), id);
            round.setGame(getGameForRound(round));
            return round;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private Game getGameForRound(Round round) {
        final String SELECT_GAME_FOR_ROUND = "SELECT g.* FROM Game AS g "
                + "JOIN Round AS r ON g.GameId = r.GameId WHERE r.RoundId = ?";
        return jdbcTemplate.queryForObject(SELECT_GAME_FOR_ROUND, new GameDaoDb.GameMapper(), round.getRoundId());
    }


    @Override
    public List<Round> getAllRounds() {
        final String SELECT_ALL_ROUNDS = "SELECT * FROM Round";
        List<Round> rounds = jdbcTemplate.query(SELECT_ALL_ROUNDS, new RoundMapper());
        addGameToRounds(rounds);
        return rounds;
    }

    private void addGameToRounds(List<Round> rounds) {
        for(Round round : rounds) {
            round.setGame(getGameForRound(round));
        }
    }

    @Override
    public List<Round> getRoundsForGame(Game game) {
        final String SELECT_ROUNDS_FOR_GAME = "SELECT * FROM Round WHERE GameId = ?";
        List<Round> rounds = jdbcTemplate.query(SELECT_ROUNDS_FOR_GAME, new RoundMapper(), game.getGameId());
        addGameToRounds(rounds);
        return rounds;
    }

    @Override
    public Round addRound(Round round) {
        final String INSERT_ROOM = "INSERT INTO Round(Guess, Time, GuessResult, GameId) VALUES(?,?,?,?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((Connection conn) -> {
            PreparedStatement statement = conn.prepareStatement(
                    INSERT_ROOM,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, round.getGuess());
            statement.setTimestamp(2, Timestamp.valueOf(round.getTime()));
            statement.setString(3, round.getGuessResult());
            statement.setInt(4, round.getGame().getGameId());
            return statement;
        }, keyHolder);

        round.setRoundId(keyHolder.getKey().intValue());
        return round;
    }

    @Override
    public void updateRound(Round round) {
        final String UPDATE_ROUND = "UPDATE Round " +
                "SET Guess = ?, Time = ?, GuessResult = ?, GameId = ? WHERE RoundId = ?";
                jdbcTemplate.update(UPDATE_ROUND,
                        round.getGuess(),
                        round.getTime(),
                        round.getGuessResult(),
                        round.getGame().getGameId(),
                        round.getRoundId());
    }

    @Override
    @Transactional
    public void deleteRoundById(int id) {
        final String DELETE_ROUND = "DELETE FROM Round WHERE RoundId = ?";
        jdbcTemplate.update(DELETE_ROUND, id);
    }

    public static final class RoundMapper implements RowMapper<Round> {

        @Override
        public Round mapRow(ResultSet rs, int index) throws SQLException {
            Round rd = new Round();
            rd.setGuess(rs.getString("Guess"));
            rd.setRoundId((rs.getInt("RoundId")));
            rd.setTime(rs.getTimestamp("Time").toLocalDateTime());
            rd.setGuessResult(rs.getString("GuessResult"));
            return rd;
        }
    }
}
