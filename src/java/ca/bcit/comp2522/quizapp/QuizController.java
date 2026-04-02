package ca.bcit.comp2522.quizapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import javafx.event.Event;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controls rendering for the quiz. For every new Quiz session, the stage will:
 * - Display a welcome message
 * - Hide the answer input
 * - Hide the question number label
 * - Hide the next question button
 * - Turn the submit button into a start button
 *
 *
 * @author Morris Li
 * @author Raphael Berthaud
 *
 * @version 0.1
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

    private static final String RESULT_BUTTON_PREVIEW_TEXT = "See results";
    private static final String RESULT_BUTTON_NEW_ROUND = "Start a new quiz";

    private static final String RESULT_CORRECT_MARKER = "O";
    private static final String RESULT_WRONG_MARKER = "X";
    private static final int RESULT_FOOTER_SIZE = 1;
    private static final int RESULT_HEADER_SIZE = 0;
    private static final int RESULT_MIN_CHILD_COUNT = 0;

    private static final int QUESTION_STARTING_NUMBER = 1;
    private static final int STARTING_COUNT = 0;

    /* Reference to the application instance. Should not be null and will cause a run-time error if any events are caught while null. */
    private QuizApp appInstance;

    /* Main scene for rendering quiz questions. */
    private Scene quizScene;

    // Non-attached containers for displaying quiz results:

    /* Contains the layout for the results display. */
    final private VBox resultsContainer;
    /* Contains the scene to be swapped in for result display. */
    final private Scene resultsScene;

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

    /* Advance scene to next question. Disabled while user is working on current question. */
    @FXML
    private Button nextButton;

    /* Text field for inputting answer. */
    @FXML
    private TextField answerInput;

    /**
     * Initializes hidden elements that will be swapped in depending on the mode of the display.
     */
    public QuizController()
    {
        final Button newQuizButton;
        final List<Node> resultChildren;

        this.resultsContainer = new VBox();
        this.resultsScene = new Scene(this.resultsContainer);

        resultChildren = this.resultsContainer.getChildren();
        newQuizButton = new Button("New Quiz");

        newQuizButton.setOnAction(this::setNewQuizScene);
        resultChildren.addLast(newQuizButton);
    }

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
        this.answerInput.setOnKeyPressed(this::onInputEnterKey);

        this.nextButton.setVisible(false);
        this.titleText.setVisible(true);
    }

    /**
     * Set a reference to the main application so the controller can pass events.
     * Call this when initially setting up the Stage from the calling Application.
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

    /**
     * Set the stage to a new question, clearing previous text input, re-enabling buttons for submitting answers, and disabling the next question button.
     *
     * @param event Unused.
     */
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

        // Set up answer input
        this.answerInput.setText(null);
        this.answerInput.setDisable(false);
        this.answerInput.setPromptText(TEXT_INPUT_PROMPT);
    }

    /**
     * Submit answer and gray out the button to emphasize the next question button.
     * Additionally, the controller will check with app if the user has answered all questions in the current quiz round.
     * If the quiz is 'completed' the next button should render and result screen with a restart button instead of a new question.
     *
     * @param event Used to get the current Stage.
     */
    void onSubmitButtonClick(final Event event)
    {
        final boolean quizFinished;

        sendAnswer(answerInput.getText());

        quizFinished = appInstance.shouldReset();

        // Button should move on to results screen or go to next question
        if (quizFinished)
        {
            this.prepResultScreen();
        }
        else
        {
            this.multiButton.setDisable(true);
            this.nextButton.setDisable(false);
            this.answerInput.setDisable(true);
        }
    }

    /*
     * Adjust elements within the regular quiz scene to prompt the user to advance to a results page:
     * - Hide and disable answer input
     * - Hide and disable the next question button
     * - Change the multibutton to advance to the result screne
     */
    private void prepResultScreen()
    {
        // Change element visibility here
        this.answerInput.setDisable(true);
        this.answerInput.setVisible(false);
        this.nextButton.setDisable(true);
        this.nextButton.setVisible(false);

        // Change button functionality here.
        this.multiButton.setText(RESULT_BUTTON_PREVIEW_TEXT);
        this.multiButton.setOnAction(this::setResultScene);
    }

    /**
     * Event handler for detecting the enter key on the text input field.
     *
     * @param event Event info passed by the JFX component.
     */
    void onInputEnterKey(final Event event)
    {
        if (!(event instanceof KeyEvent))
        {
            return;
        }

        final KeyEvent keyEv;
        final KeyCode keyPressed;

        keyEv = (KeyEvent) event;
        keyPressed = keyEv.getCode();

        if (keyPressed == KeyCode.ENTER)
        {
            this.onSubmitButtonClick(event);
        }
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
     * Sends the user's answer to the main Quiz app for evaluation and present the result other user on the screen.
     *
     * @param answer Answer present on the input field of the app.
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
     *
     * @param event Used to retrieve current Stage instance.
     */
    void setNewQuizScene(final Event event)
    {
        final Stage currentStage;

        currentStage = getStageFromEvent(event);

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

    /**
     * Hides question-asking UI elements and presents the quiz results with a "New Quiz" button.
     *
     * @param event UI event used to get the current stage.
     */
    void setResultScene(final Event event)
    {
        final List<QuizApp.AskedQuestion> results;
        final List<Node> resultChildren;
        final Stage currentStage;

        final HBox resultCount;
        final List<Node> resultCountChildren;
        final Label resultLabel;

        final int totalCount;
        int correctCount;

        clearResults();

        results = appInstance.getQuestionHistory();
        resultChildren = resultsContainer.getChildren();
        currentStage = getStageFromEvent(event);

        resultCount = new HBox();
        resultCountChildren = resultCount.getChildren();

        totalCount = results.size();
        correctCount = STARTING_COUNT;

        currentStage.setScene(this.resultsScene);

        // Generate a HBox row to display a result in the format of "[O|X] Question" and append it to a VBox
        for (final QuizApp.AskedQuestion q : results)
        {
            final Question questionInfo;
            final Label question;
            final Label answer;
            final HBox row;
            final List<Node> rowChildren;

            questionInfo = q.questionAsked();
            question = new Label(questionInfo.question());

            if (q.wasCorrect())
            {
                answer = new Label(RESULT_CORRECT_MARKER);
            }
            else
            {
                answer = new Label(RESULT_WRONG_MARKER);
            }

            row = new HBox();
            rowChildren = row.getChildren();

            rowChildren.add(answer);
            rowChildren.add(question);
            resultChildren.add(row);

            correctCount++;
        }

        resultLabel = new Label("You got: " + correctCount + '/' + totalCount);
        resultCountChildren.add(resultLabel);
        resultChildren.add(resultCount);
    }

    /*
     * Utility function for getting the Stage that sent an Event.
     *
     * @param event Event fired from some UI element.
     */
    private static Stage getStageFromEvent(final Event event)
    {
        final Stage stage;
        final Node node;
        final Scene scene;

        node = (Node) event.getSource();
        scene = node.getScene();
        stage = (Stage) scene.getWindow();

        return stage;
    }

    /*
     * Helper to remove all but the last child from the result container.
     * This preserves the button element that got added during construction so we don't have to manually re-add
     * everytime we call setResultScene.
     */
    private void clearResults()
    {
        final List<Node> resultChildren;
        final Node last;
        final int minusOneSize;

        resultChildren = this.resultsContainer.getChildren();
        last = resultChildren.getLast();
        minusOneSize = resultChildren.size() - RESULT_FOOTER_SIZE;

        if (last instanceof Button && minusOneSize > RESULT_MIN_CHILD_COUNT)
        {
            resultChildren.subList(RESULT_HEADER_SIZE, minusOneSize).clear();
        }
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
