package ca.bcit.comp2522.quizapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Handles the quiz presentation logic.
 *
 * @author Morris Li
 *
 * @version 0.1
 */
public class QuizApp
        extends Application
{
    /* Internal class that tracks the player's individual question record for the current round. */
    private record AskedQuestion(Question questionAsked, boolean wasCorrect) {}

    private static final int WINDOW_WIDTH_DEFAULT = 400;
    private static final int WINDOW_HEIGHT_DEFAULT = 200;
    private static final String WINDOW_TITLE = "Quick Quiz";
    private static final String SCENE_FILE_NAME = "quizwindow.fxml";

    private static final String QUESTION_FILE_NAME = "quiz.txt";
    private static final String QUESTION_FILE_SEP = "\\|";
    private static final int QUESTION_POS = 0;
    private static final int ANSWER_POS = 1;

    private static final int POINTS_CORRECT = 3;
    private static final int POINTS_WRONG = 0;
    private static final int QUESTIONS_PER_ROUND = 10;
    private static final int SCORE_INITIAL = 0;
    private static final int QUESTION_NUMBER_INITIAL = 0;
    private static final int QUESTION_BANK_LBOUND = 0;

    private final List<Question> questionList;
    private final List<AskedQuestion> answeredList;
    private Question presentedQuestion;
    private final RandomGenerator rgen;

    private Scene activeScene;
    private Stage currentStage;
    private QuizController vc;

    private int questionNumber;
    private int playerScore;

    /**
     * Initialize a quiz round with predefined questions.
     */
    public QuizApp()
    {
        super();

        this.questionList = parseQuestions();
        this.answeredList = new ArrayList<>();
        this.rgen = RandomGenerator.getDefault();

        this.reset();
    }

    /**
     * Initializes the quiz window.
     *
     * @param stage Main application window
     *
     * @throws IOException Thrown if the intended stage FXML is not found by the loader.
     */
    @Override
    public void start(final Stage stage) throws IOException
    {
        final FXMLLoader loader;

        loader = new FXMLLoader(QuizApp.class.getResource(SCENE_FILE_NAME));
        this.activeScene = new Scene(loader.load(), WINDOW_WIDTH_DEFAULT, WINDOW_HEIGHT_DEFAULT);
        this.currentStage = stage;
        this.vc = loader.getController();
        this.vc.setAppRef(this);

        stage.setTitle(WINDOW_TITLE);
        stage.setScene(this.activeScene);
        stage.show();
    }

    /*
     * Helper for getting the quiz file path to parse questions.
     *
     * @return Path to the quiz file.
     */
    private static Path getQuizFilePath()
    {
        final URL quizUrl;
        final URI quizUri;
        final Path quizPath;

        quizUrl = QuizApp.class.getResource(QUESTION_FILE_NAME);

        if (quizUrl == null)
        {
            throw new IllegalArgumentException("Resource not found: " + QUESTION_FILE_NAME);
        }

        try
        {
            quizUri = quizUrl.toURI();
            quizPath = Paths.get(quizUri);

            return quizPath;
        }
        catch (final URISyntaxException synEx)
        {
            throw new IllegalArgumentException("URI conversion failed: " + quizUrl);
        }
    }

    /*
     * Loads questions from text file into memory.
     *
     * @return List containing question and answer text.
     */
    private static List<Question> parseQuestions()
    {
        final Path quizFilePath;
        final List<String> questionText;
        final List<Question> questionList;

        quizFilePath = getQuizFilePath();
        questionList = new ArrayList<>();

        try
        {
            questionText = Files.readAllLines(quizFilePath);

            for (final String line : questionText)
            {
                final String[] qAndA;
                final Question parsed;

                qAndA = line.split(QUESTION_FILE_SEP);
                parsed = new Question(
                        qAndA[QUESTION_POS],
                        qAndA[ANSWER_POS]
                );

                questionList.add(parsed);
            }
        }
        catch (final IOException ioe)
        {
            throw new RuntimeException("Failed to read from quiz file");
        }

        return questionList;
    }

    /*
     * Starts a new round with QUESTION_PER_ROUND questions.
     */
    private void reset()
    {
        this.answeredList.clear();
        this.playerScore = SCORE_INITIAL;
        this.questionNumber = QUESTION_NUMBER_INITIAL;
    }

    /**
     * Advance quiz state to the next question
     */
    public String advanceQuestion()
    {
        final int nextQuestionIndex;

        nextQuestionIndex = rgen.nextInt(
                QUESTION_BANK_LBOUND,
                this.questionList.size()
        );

        this.presentedQuestion = this.questionList.get(nextQuestionIndex);
        this.questionNumber++;

        return this.presentedQuestion.question();
    }

    /**
     * Getter for the question the quiz should currently be presenting.
     *
     * @return Question object that contains both question and answer.
     */
    public Question getPresentedQuestion()
    {
        return this.presentedQuestion;
    }

    /**
     * Check the user's guess and return the result.
     *
     * @param guess User's inputted answer.
     *
     * @return True if the user's input is accepted as a correct answer.
     */
    public boolean checkAnswer(final String guess)
    {
        final boolean result;
        final AskedQuestion recordEntry;

        result = this.presentedQuestion.checkAnswer(guess);
        recordEntry = new AskedQuestion(this.presentedQuestion, result);

        if (result)
        {
            playerScore += POINTS_CORRECT;
        }
        else
        {
            playerScore += POINTS_WRONG;
        }

        this.answeredList.add(recordEntry);

        return result;
    }

    public int getQuestionNumber()
    {
        return this.questionNumber;
    }

    /**
     * Program entry point.
     *
     * @param args Unused
     */
    public static void main(final String[] args)
    {
        Application.launch(QuizApp.class, args);
    }
}
