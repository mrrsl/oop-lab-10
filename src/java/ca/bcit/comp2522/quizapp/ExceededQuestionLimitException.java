package ca.bcit.comp2522.quizapp;

/**
 * Exception thrown if a Quiz App caller tries to have the QuizApp instance produce more questions when it has already asked a maximum number of questions.
 *
 * @author Morris Li
 * @author Raphael Berthaud
 *
 * @version 0.1
 */
public class ExceededQuestionLimitException
    extends RuntimeException
{
    private static final String DEFAULT_ERROR_PREFIX = "Quiz must be reset before asking more questions. Current question limit is ";

    /**
     * Produce an exception that communicates the question number limit.
     *
     * @param messageLimit Maximum number of questions in a quiz round.
     */
    public ExceededQuestionLimitException(final int messageLimit)
    {
        super(DEFAULT_ERROR_PREFIX + messageLimit);
    }

}
