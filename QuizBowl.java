import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

class Player {
    final private String playerName;
    private int points;

    public Player(String name) {
        this.playerName = name;
        this.points = 0;
    }

    public void addPoints(int point) {
        this.points += point;
    }

    public void subtractPoints(int point) {
        this.points -= point;
    }

    public int getPoints() {
        return this.points;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}

abstract class Question {
    protected String question;
    protected int points;
    protected String validAns;

    public Question(String question, int pts, String ans) {
        this.question = question;
        this.points = pts;
        this.validAns = ans;
    }

    public int getPoints() {
        return points;
    }

    boolean validAnswer(String ans) {
        System.out.println(validAns);
        return ans.trim().equalsIgnoreCase(String.valueOf(validAns).trim());
    }

    abstract void displayQuestion();
}

class QuestionTF extends Question {
    public QuestionTF(String question, int pts, boolean ans) {
        super(question, pts, String.valueOf(ans));
    }

    @Override
    void displayQuestion() {
        System.out.println("Question: " + question + " (true/false)");
    }
}

class QuestionMC extends Question {
    final private ArrayList<String> choices;

    public QuestionMC(String question, int pts, ArrayList<String> choices,
            String ans) {
        super(question, pts, ans);
        this.choices = choices;
    }

    @Override
    void displayQuestion() {
        System.out.println("Question: " + question);
        IntStream.range(0, choices.size()).forEach(i -> System.out.println((char) ('A' + i) + ". " + choices.get(i)));
    }
}

class QuestionSA extends Question {
    public QuestionSA(String question, int pts, String ans) {
        super(question, pts, ans);
    }

    @Override
    void displayQuestion() {
        System.out.println("Question: " + question);
    }
}

public class QuizBowl {
    final String fileName = "./questions";
    final int NUM_CHOICES = 4;
    final int PENALTY_POINTS = 1;
    private Player player;
    final private ArrayList<Question> questions;
    final private Scanner scanner;

    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";

    public QuizBowl() {
        questions = new ArrayList<>();
        scanner = new Scanner(System.in);
    }

    private void loadQuestion() throws FileNotFoundException {
        Scanner fileContents = new Scanner(new File(fileName));
        loadQuestionsRec(fileContents);
        fileContents.close();
    }

    private void loadQuestionsRec(Scanner fileContents) {
        if (!fileContents.hasNextLine()) {
            return;
        }

        String quesType = fileContents.nextLine().trim();
        if (quesType.isEmpty()) {
            loadQuestionsRec(fileContents);
            return;
        }

        int pts = Integer.parseInt(fileContents.nextLine().trim());
        String question = fileContents.nextLine().trim();

        switch (quesType) {
            case "TF" -> {
                boolean ans = Boolean.parseBoolean(fileContents.nextLine().trim());
                questions.add(new QuestionTF(question, pts, ans));
            }
            case "MC" -> {
                ArrayList<String> choices = new ArrayList<>();
                IntStream.range(0, NUM_CHOICES).forEach(i -> choices.add(fileContents.nextLine().trim()));
                String ans = fileContents.nextLine().trim().charAt(0) + "";
                questions.add(new QuestionMC(question, pts, choices, ans));
            }
            case "SA" -> {
                String ans = fileContents.nextLine().trim();
                questions.add(new QuestionSA(question, pts, ans));
            }
        }

        if (fileContents.hasNextLine()) {
            fileContents.nextLine();
        }
        loadQuestionsRec(fileContents);
    }

    int getTotalQuestions() {
        return questions.size();
    }

    int handleValidQuestion(int total) {
        int val = 0;
        if (scanner.hasNextInt()) {
            val = scanner.nextInt();
            scanner.nextLine();
            if (val < 1 || val > total) {
                System.out.println("Please enter a number between 1 and " + total + ": ");
                return handleValidQuestion(total);
            }
        } else {
            System.out.println("Please enter a valid integer value: ");
            scanner.next();
            return handleValidQuestion(total);
        }
        return val;
    }

    public void runGame() {
        System.out.println("What is your name: ");
        String playerName = scanner.nextLine();
        player = new Player(playerName);

        try {
            loadQuestion();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            return;
        }

        final int totalQuestions = getTotalQuestions();

        System.out.println("How many questions do you want to answer: " +
                "(max: 10)");
        int questionToAnswer = handleValidQuestion(totalQuestions);

        Collections.shuffle(questions);
        IntStream.range(0, questionToAnswer).forEach(i -> {
            Question question = questions.get(i);

            System.out.println(CYAN + "Points: " + player.getPoints() + RESET);
            question.displayQuestion();
            String ans = scanner.nextLine();

            if (!ans.equalsIgnoreCase("SKIP")) {
                if (question.validAnswer(ans)) {
                    player.addPoints(question.getPoints());
                    System.out.println(GREEN + "Correct! You get " + question.getPoints() + " points.\n" + RESET);
                } else {
                    player.subtractPoints(PENALTY_POINTS);
                    System.out.println(RED + "Incorrect! You loose " + PENALTY_POINTS +
                            " " +
                            "points\n" + RESET);
                }
            } else {
                System.out.println(YELLOW + "Skipped Question. You get 0 points.\n" + RESET);
            }
        });

        System.out.println("Game Over");
        System.out.printf("%s. Your final score is %d\n",
                player.getPlayerName(), player.getPoints());
        if (player.getPoints() < 0) {
            System.out.println("Better luck next time");
        }
    }

    public static void main(String[] args) {
        QuizBowl game = new QuizBowl();
        game.runGame();
    }
}
