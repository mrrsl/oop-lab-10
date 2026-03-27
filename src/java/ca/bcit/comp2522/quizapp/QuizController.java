package ca.bcit.comp2522.quizapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import javafx.event.Event;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Operates the quiz
 */
public class QuizController
    implements Initializable
{
    private static final String SUBMIT_BUTTON_TEXT = "Submit";
    private static final String START_BUTTON_TEXT = "Start";
    private static final String NEXT_BUTTON_TEXT = "Next Question";
    private static final String CORRECT_GUESS_TEXT = "CORRECT";
    private static final String WRONG_GUESS_TEXT = "WRONG, Correct answer is: ";
    private static final String WELCOME_MESSAGE = "Welcome to Quick Quiz";
    private static final String WELCOME_CAPTION = "Press " + START_BUTTON_TEXT+ " to get the first question!";
    private static final String TEXT_INPUT_PROMPT = "Answer here";

    private static final int QUESTION_STARTING_NUMBER = 1;

    /* Set to true if the window is accepting answer submissions. */
    private boolean acceptingGuesses;

    private int questionNumber;

    /* Reference to the application instance. Should not be null and will cause a run-time error if any events are caught while null. */
    private QuizApp appInstance;
    {
        questionNumber = QUESTION_STARTING_NUMBER;
    }

    /* Holds control buttons. */
    @FXML
    private BorderPane mainContainer;

    /* For rendering question number to user. */
    @FXML
    private Label questionNumberLabel;

    /* Display the welcome message and question text. */
    @FXML
    private Label titleText;

    /* Label that presents a smaller welcome message and is later used to indicate right/wrong answer. */
    @FXML
    private Label titleCaption;

    /* Starts the quiz or submits an answer depending on interface state. */
    @FXML
    private Button multiButton;

    /* Advance scene to next question. */
    @FXML
    private Button nextButton;

    @FXML
    private TextField answerInput;

    /**
     * Hide unneeded UI elements from the welcome screen when controller is loaded.
     *
     * @param location Unused.
     * @param resources Unused.
     */
    @Override
    @FXML
    public void initialize(final URL location,
                           final ResourceBundle resources)
    {
        this.answerInput.setVisible(false);
        this.nextButton.setVisible(false);
        this.titleText.setVisible(true);
    }

    /**
     * Set a reference to the main application so the controller can pass events
     *
     * @param appInstance The application instance that controls the app stage.
     */
    void setAppRef(final QuizApp appInstance)
    {
        this.appInstance = appInstance;
    }

    /**
     * Initiates a quiz, adds a submit and next question button, then presents the first question.
     */
    @FXML
    void onStartButtonClick(final Event event)
    {
        // Update button to submit functionality
        this.multiButton.setText(SUBMIT_BUTTON_TEXT);
        this.multiButton.setOnAction(this::onSubmitButtonClick);

        this.titleCaption.setVisible(false);
        this.nextButton.setVisible(true);
        this.answerInput.setVisible(true);

        onNextButtonClick(event);
    }

    @FXML
    void onNextButtonClick(final Event event)
    {
        final String nextQuestionText;
        final int nextQuestionNumber;

        nextQuestionText = appInstance.advanceQuestion();
        nextQuestionNumber = appInstance.getQuestionNumber();

        // Set labels
        this.titleText.setText(nextQuestionText);
        this.titleCaption.setVisible(false);
        setQuestionNumber(nextQuestionNumber);

        // Set buttons
        this.multiButton.setDisable(false);
        this.nextButton.setDisable(true);

        this.answerInput.setText(null);
        this.answerInput.setDisable(false);
        this.answerInput.setPromptText(TEXT_INPUT_PROMPT);
    }

    /**
     * Submit answer and grey out the button to emphasize the next question button.
     *
     * @param event Unused.
     */
    void onSubmitButtonClick(final Event event)
    {
        sendAnswer(answerInput.getText());
        this.multiButton.setDisable(true);
        this.nextButton.setDisable(false);
        this.answerInput.setDisable(true);
    }

    /**
     * Prints the given text to the centered label component.
     *
     * @param question Quiz question to present to the user.
     */
    void presentQuestionText(final String question)
    {
        this.titleText.setText(question);
    }

    /**
     * Sets the question number to be presented to the user.
     *
     * @param num A question number
     */
    void setQuestionNumber(final int num)
    {
        final String questionText;

        questionText = getFormattedQuestionNumber((num));

        if (questionText != null)
        {
            this.questionNumberLabel.setVisible(true);
            this.questionNumberLabel.setText(questionText);
        }
        else
        {
            this.questionNumberLabel.setVisible(false);
        }

    }

    /**
     * Sends the user's answer to the main Quiz app for evaluation.
     *
     * @param answer
     */
    void sendAnswer(final String answer)
    {
        final boolean result;

        result = appInstance.checkAnswer(answer);

        this.titleCaption.setVisible(true);

        if (result)
        {
            this.titleCaption.setText(CORRECT_GUESS_TEXT);
        }
        else
        {
            this.titleCaption.setText(WRONG_GUESS_TEXT +
                    appInstance.getPresentedQuestion().answer()
            );
        }
    }

    /**
     * Prepares the window scene to let the user initiate a new quiz.
     */
    void setNewQuizScene()
    {
        // Reset question number display
        this.questionNumberLabel.setText(null);
        this.questionNumberLabel.setVisible(false);

        // Reset answer input
        this.answerInput.setVisible(false);
        this.answerInput.setText(null);

        // Set welcome messages
        this.titleText.setText(WELCOME_MESSAGE);
        this.titleCaption.setText(WELCOME_CAPTION);

        // Set up the start button
        this.multiButton.setOnAction(this::onStartButtonClick);
        this.multiButton.setText(START_BUTTON_TEXT);
        this.multiButton.setDisable(false);
    }

    /*
     * Generates a string with the question number in the correct rendering format.
     *
     * @param number Question number.
     *
     * @return String to be rendered on the UI.
     */
    private static String getFormattedQuestionNumber(final int number)
    {
        if (number < QUESTION_STARTING_NUMBER) {
            return null;
        }
        return "Question " + number;
    }
}
