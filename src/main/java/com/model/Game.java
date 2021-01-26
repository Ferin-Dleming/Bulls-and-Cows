package com.model;

public class Game {

    private int gameId;
    private int answer;
    private boolean status;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
        }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    // Override default equals for testing
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Game)) {
            return false;
        }

            Game game = (Game) obj;
            return (game.gameId == this.gameId &&
                    game.answer == this.answer &&
                    game.status == this.status);
    }

    // Override default hashCode for testing
    @Override
    public int hashCode() {
        return this.gameId;
    }
}
