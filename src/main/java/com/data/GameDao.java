package com.data;

import com.model.Game;
import java.util.List;

public interface GameDao {
    List<Game> getAllGames();
    Game getGameById(int id);
    Game addGame(Game game);
    void updateGame(Game game);
    void deleteGameById(int id);
}
