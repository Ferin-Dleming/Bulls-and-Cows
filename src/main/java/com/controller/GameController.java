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

// @RestController is needed to make class injectable.
// Lets Spring MVC scan for methods that can handle HTTP requests.
// Tells Spring MVC to convert method results to JSON.
// @RequestMapping builds url.
// Methods in class will default to the given url.
@RestController
@RequestMapping("/assessment")
public class GameController {

    private final GameDaoDb gameDb;
    private final RoundDaoDb roundDb;

//    Autowired is optional here. The constructor will take in the
//    dependencies, and Spring satisfies the dependency with the class
//    annotated with @Repository.
    @Autowired
    public GameController(GameDaoDb gameDaoDb, RoundDaoDb roundDaoDb) {
        this.gameDb = gameDaoDb;
        this.roundDb = roundDaoDb;
    }

//    @PostMapping enables our method to accept POST requests at url /assessment/begin.
//    Returns 201 CREATED if successful. Otherwise, 404. Ask Ronnie.
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
//    @PostMapping enables our method to accept POST requests at url /assessment/guess.
//    Returns 201 CREATED if successful.
//    @RequestBody annotation allows Spring to inject request body as argument.
    @PostMapping("/guess")
    public ResponseEntity<Round> guess(@RequestBody String jsonStr) throws JSONException {

//      @RequestBody can only be used once so we must use a jsonObject
//      to get all of the arguments from the request body. We can unpack
//      the jsonObject using its .get method and casting.

        JSONObject jsonObject = new JSONObject(jsonStr);
        String gameId = (String) jsonObject.get("gameId");
        String guess = (String) jsonObject.get("guess");
        Game game = gameDb.getGameById(Integer.parseInt(gameId));
        if (game == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

//      Can't have a valid number starting with 0.
        if (guess.charAt(0) == '0') {
            return new ResponseEntity(null, HttpStatus.METHOD_NOT_ALLOWED);
        }

        if (!checkValidGuess(Integer.parseInt(guess))) {
            return new ResponseEntity(null, HttpStatus.METHOD_NOT_ALLOWED);
        }

//      Create round corresponding to guess.
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

//      If guess is correct, update dao accordingly.
        if (isCorrect(answer, Integer.parseInt(guess))) {
            game.setStatus(false);
            gameDb.updateGame(game);
        }
         else {
//          Create game with answer set to 0.

            Game temp = new Game();
            temp.setGameId(game.getGameId());
            temp.setStatus(true);
            round.setGame(temp);
        }
        return new ResponseEntity(round, HttpStatus.CREATED);
    }

//    @GetMapping enables our method to accept GET requests at url /assessment/game.
//    Returns 200 OK if successful.
    @GetMapping("/game")
    public ResponseEntity<List<Game>> getGames() {
        List<Game> games = gameDb.getAllGames();
        if (games == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

//      Creating return list populated with games.
//      Change game answers to reflect status.

        List<Game> retGames = new ArrayList<>();
        for (Game game : games) {
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

//  @GetMapping enables our method to accept GET requests at url /assessment/game/{gameId}
//  Return 200 OK if successful.
//  @PathVariable annotation instructs Spring to inject argument from the url path.
    @GetMapping("/game/{gameId}")
    public ResponseEntity<Game> getGameById(@PathVariable String gameId) {
        int id = Integer.parseInt(gameId);
        Game game = gameDb.getGameById(id);
        if (game == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

//      Change status if necessary.
//      Not a permanent operation since game is not being updated or set by dao.
        if (game.getStatus()) {
            game.setAnswer(0);
        }
        return ResponseEntity.ok(game);
    }
//  @GetMapping enables our method to accept GET requests at url /assessment/rounds/{gameId}
//  Return 200 OK if successful.
//  @PathVariable annotation instructs Spring to inject argument from the url path.
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

//      Change rounds if necessary to reflect status.

        if (game.getStatus()) {
            Game temp = new Game();
            temp.setAnswer(0);
            temp.setGameId(game.getGameId());
            temp.setStatus(game.getStatus());
            for (Round round : rounds) {
                round.setGame(temp);
            }
        }

//      Sort rounds based on LocalDateTime using custom comparator.

        Collections.sort(rounds, new CustomComparator());
        return ResponseEntity.ok(rounds);
    }

//  Helper method to check if user guess is valid.
//  Int is built in reverse but it won't affect anything
//  because we are only checking length and uniqueness.
    private boolean checkValidGuess(int guess) {
        int temp = guess;
        HashSet<Integer> set = new HashSet<>();
        while (guess > 0) {
            if (set.contains(guess % 10)) {
                return false;
            }
            set.add(guess % 10);
            guess = guess / 10;
        }
        return String.valueOf(temp).length() == 4;
    }

    private boolean isCorrect(int answer, int guess) {
        return answer == guess;
    }

//  Returns String formatted with exact and partials.
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
        for (int i = 0; i < 10 ; i++) {
            nums.add(i);
        }

//      Avoiding massive headache

        Collections.shuffle(nums);
        if (nums.get(0) == 0) {
            Collections.shuffle(nums);
        }
        int i = 0;
        for (int index = 0; index < 4; index++) {
            i = i + nums.get(index);
            if (index != 3) {
                i = i * 10;
            }
        }
        return i;
    }

//  Custom comparator used to sort list based on time.
    private static class CustomComparator implements Comparator<Round> {
        @Override
        public int compare(Round o1, Round o2) {
            return o1.getTime().compareTo(o2.getTime());
        }
    }
}
