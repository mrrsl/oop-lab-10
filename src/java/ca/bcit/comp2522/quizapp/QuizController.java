package ca.bcit.comp2522.quizapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Operates the quiz
 */
public class QuizController
{
    /* Holds control buttons. */
    @FXML
    private HBox buttonContainer;

    /* Display the welcome message and question text. */
    @FXML
    private Label titleText;

    @FXML
    private Label titleCaption;

    @FXML
    private Button startButton;

    @FXML
    private Button submitButton;

    @FXML
    private TextField answerInput;

    /**
     * Initiates a quiz and presents the first question.
     */
    @FXML
    void onStartButtonClick() {

        submitButton.setVisible(true);

        buttonContainer.getChildren().remove(startButton);
        startButton = null;

        titleCaption.setVisible(false);
    }

    @FXML
    void onSubmitButtonClick()
    {

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
}
