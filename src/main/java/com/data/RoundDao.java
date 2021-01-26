package com.data;

import com.model.Game;
import com.model.Round;
import java.util.List;

public interface RoundDao {
    List<Round> getAllRounds();
    Round getRoundById(int id);
    Round addRound(Round round);
    void updateRound(Round round);
    void deleteRoundById(int id);

    List<Round> getRoundsForGame(Game game);
}
