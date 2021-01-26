package com.controller;

import com.data.GameDaoDb;
import com.data.RoundDaoDb;
import com.model.Game;
import com.model.Round;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/assessment")
public class GameController {

    private final GameDaoDb gameDb;
    private final RoundDaoDb roundDb;

    @Autowired
    public GameController(GameDaoDb gameDaoDb, RoundDaoDb roundDaoDb) {
        this.gameDb = gameDaoDb;
        this.roundDb = roundDaoDb;
    }

    @PostMapping("/begin")
    public ResponseEntity<Integer> begin() {
        Game game = new Game();
        game.setStatus(true);
        game.setAnswer(generateNumber());
        game = gameDb.addGame(game);
        if (game == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(game.getGameId(), HttpStatus.CREATED);
    }

    @PostMapping("/guess")
    public ResponseEntity<Round> guess(@RequestBody String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        String gameId = (String) jsonObject.get("gameId");
        String guess = (String) jsonObject.get("guess");
        Game game = gameDb.getGameById(Integer.parseInt(gameId));
        if (game == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }
        if (!checkValidGuess(Integer.parseInt(guess))) {
            return new ResponseEntity(null, HttpStatus.METHOD_NOT_ALLOWED);
        }

        int answer = game.getAnswer();
        Round round = new Round();
        round.setGuess(guess);
        round.setGuessResult(getGuessResult(answer, Integer.parseInt(guess)));
        round.setTime(LocalDateTime.now());
        round.setGame(game);
        round = roundDb.addRound(round);
        if (round == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

        if (isCorrect(answer, Integer.parseInt(guess))) {
            game.setStatus(false);
            gameDb.updateGame(game);
        }
         else {
            Game temp = new Game();
            temp.setGameId(game.getGameId());
            round.setGame(temp);
        }
        return new ResponseEntity(round, HttpStatus.CREATED);
    }

    @GetMapping("/game")
    public ResponseEntity<List<Game>> getGames() {
        List<Game> games = gameDb.getAllGames();
        if (games == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }
        List<Game> retGames = new ArrayList<>();
        for(Game game : games) {
            if (!game.getStatus()) {
                retGames.add(game);
            } else {
                Game temp = new Game();
                temp.setGameId(game.getGameId());
                temp.setAnswer(0);
                temp.setStatus(game.getStatus());
                retGames.add(temp);
            }
        }
        return ResponseEntity.ok(retGames);
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<Game> getGameById(@PathVariable String gameId) {
        int id = Integer.parseInt(gameId);
        Game game = gameDb.getGameById(id);
        if (game == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }
        if (game.getStatus()) {
            game.setAnswer(0);
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/rounds/{gameId}")
    public ResponseEntity<List<Round>> getRounds(@PathVariable String gameId) {
        int id = Integer.parseInt(gameId);
        Game game = gameDb.getGameById(id);
        if (game == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }
        List<Round> rounds = roundDb.getRoundsForGame(game);
        if (rounds == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

        if (!game.getStatus()) {
            Collections.sort(rounds, new CustomComparator());
        } else {
            Game temp = new Game();
            temp.setAnswer(0);
            temp.setGameId(game.getGameId());
            temp.setStatus(game.getStatus());
            for (Round round : rounds) {
                round.setGame(temp);
            }
            Collections.sort(rounds, new CustomComparator());
        }
        return ResponseEntity.ok(rounds);
    }

    private boolean checkValidGuess(int guess) {
        HashSet<Integer> set = new HashSet<>();
        while (guess > 0) {
            if (set.contains(guess % 10)) {
                return false;
            }
            set.add(guess % 10);
            guess = guess / 10;
        }
        return true;
    }

    private boolean isCorrect(int answer, int guess) {
        return answer == guess;
    }

    private String getGuessResult(int answer, int guess) {
        int exact = 0;
        int partial = 0;
        ArrayList<Integer> ans = new ArrayList<>();
        ArrayList<Integer> gus = new ArrayList<>();
        while (answer > 0) {
            ans.add(answer % 10);
            answer = answer / 10;
            gus.add(guess % 10);
            guess = guess / 10;
        }
        for (int i = 0; i < gus.size(); i++) {
            if (ans.get(i).equals(gus.get(i))) {
                exact++;
            } else if (ans.contains(gus.get(i))) {
                partial++;
            }
        }
        return String.format("e:%1$d:p%2$d", exact, partial);
    }

    private int generateNumber() {
        ArrayList<Integer> nums = new ArrayList<>();
        for(int i = 0; i<10 ; i++) {
            nums.add(i);
        }
        Collections.shuffle(nums);
        int i = 0;
        for (int index = 0; index < 4; index++) {
            i = i + nums.get(index);
            if (index != 3) {
                i = i * 10;
            }
        }
        return i;
    }

    private class CustomComparator implements Comparator<Round> {
        @Override
        public int compare(Round o1, Round o2) {
            return o1.getTime().compareTo(o2.getTime());
        }
    }
}
