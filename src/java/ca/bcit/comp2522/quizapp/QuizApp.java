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
 * Handles these quiz responsibilities:
 * <ul>
 * <li>Tracks questions asked and user's answers</li>
 * <li>Determines if quiz has been completed</li>
 * <li>Manages the question bank and determines the questions that will be asked</li>
 * </ul>
 * Important methods for the scene quiz controller:
 * <ul>
 * <li>reset(): Resets the number of questions asked</li>
 * <li>advanceQuestion(): Move on the next question and get the text for it</li>
 * <li>shouldReset(): Controller should call this to see if quiz is complete</li>
 * </ul>
 *
 * @author Morris Li
 * @author Raphael Berthaud
 *
 * @version 0.1
 */
public class QuizApp
        extends Application
{
    /**
     * Data for tracking the player's individual question record for the current round.
     *
     * @param questionAsked {@link Question} instance representing a question that was displayed to the player.
     * @param wasCorrect Set to true if it was answered correctly, falsed otherwise.
     */
    public record AskedQuestion(Question questionAsked, boolean wasCorrect) {}

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
        if (this.answeredList.size() >= QUESTIONS_PER_ROUND)
        {
            throw new ExceededQuestionLimitException(QUESTIONS_PER_ROUND);
        }

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
        recordEntry = new AskedQuestion(
                this.presentedQuestion,
                result
        );

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

    /**
     * Get the number of the currently displayed question.
     *
     * @return 1-indexed number of the current question.
     */
    public int getQuestionNumber()
    {
        return this.questionNumber;
    }

    /**
     * Determine if the quiz should be reset due to reaching the maximum number of questions
     *
     * @return True if quiz is finished.
     */
    public boolean shouldReset()
    {
        return this.answeredList.size() >= QUESTIONS_PER_ROUND;
    }

    /**
     * Gets a list of questions that have been answered by the user in the current round.
     *
     * @return List containing a copy of the questions presented and whether the user answered them correctly.
     */
    public List<AskedQuestion> getQuestionHistory()
    {
        return this.answeredList;
    }

    /**
     * Getter for points scored by the player.
     *
     * @return Points scored on current quiz.
     */
    public int getPlayerScore()
    {
        return this.playerScore;
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
