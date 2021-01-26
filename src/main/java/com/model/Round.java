package com.model;


import java.time.LocalDateTime;

public class Round {

    int roundId;
    String guess;
    LocalDateTime time;
    String guessResult;
    Game game;

    public int getRoundId() {
        return roundId;
    }

    public void setRoundId(int id) {
        this.roundId = id;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getGuessResult() {
        return guessResult;
    }

    public void setGuessResult(String guessResult) {
        this.guessResult = guessResult;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    // Override default equals for testing purposes
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
            if (!(obj instanceof Round)) {
                return false;
            }
            Round round = (Round) obj;
            return (round.roundId == this.roundId
                    && round.guess.equals(this.guess)
                    && round.time.isEqual(this.time)
                    && round.guessResult.equals(this.guessResult)
                    && round.game.equals(this.game));
    }

    // Override default hashCode for testing purposes
    @Override
    public int hashCode() {
        return this.roundId;
    }
}
