package ca.bcit.comp2522.quizapp;

/**
 * Holds information for a quiz question.
 *
 * @author Morris Li
 *
 * @version 0.1
 *
 * @param question The quiz question.
 * @param answer The expected quiz answer.
 */
public record Question(String question,
                       String answer)
{
    /**
     * Check if a given answer is corrected.
     *
     * @param guess User's guessed answer.
     *
     * @return True if the given answer contains the expected answer
     */
    public boolean checkAnswer(final String guess)
    {
        final String uncasedGuess;
        final String uncasedAnswer;

        uncasedAnswer = guess.toLowerCase();
        uncasedGuess = this.answer.toLowerCase();

        return uncasedGuess.contains(uncasedAnswer);
    }
}
