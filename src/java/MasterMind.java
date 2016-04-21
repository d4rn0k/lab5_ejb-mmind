import pl.jrj.game.*;

import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 123
 * @version 0.7
 */
@Stateful
public class MasterMind implements IMasterMind {

    private MasterClass masterMInner;

    //@Inject
    private IGameMonitor gameMonitor;

    @Override
    public void initialize(int colorsCount, int pegsCount, long seed) {
        InitialContext ic = null;
        int maxGueses = 15;
        try {
            ic = new InitialContext();
            gameMonitor = (IGameMonitor) ic.lookup("java:global/ejb-project/GameMonitor!pl.jrj.game.IGameMonitor");
        } catch (NamingException ex) {
            Logger.getLogger(MasterMind.class.getName()).log(Level.SEVERE, null, ex);
        }
        gameMonitor.register(5, "92123");
        gameMonitor.initGame(colorsCount, pegsCount, seed);
        masterMInner = new MasterClass(maxGueses, colorsCount, pegsCount);
    }

    @Override
    public int play() {
        masterMInner.solve();
        return masterMInner.guessCount;
    }

    /**
     *
     * @param input alphabet string
     * @return Evaulation with correct positions and colors.
     */
    private Evaluation stringToEval(String input) {
        int correctColorsPositions = 0;
        int correctColors = 0;
        try {
            correctColorsPositions = Integer.parseInt(input.substring(0, 1));
            correctColors = Integer.parseInt(input.substring(1, 2));
        } catch (NumberFormatException ignored) {
        }
        return new Evaluation(correctColorsPositions, correctColors);
    }

    /**
     * @param combination
     * @return
     */
    private String guessToString(int[] combination) {
        StringBuilder string = new StringBuilder();
        String alphabet = "ABCDEFGHIJKLMNOQ";
        for (int aCombination : combination) {
            string.append(alphabet.charAt(aCombination));
        }
        return string.toString();
    }

    /**
     * @param combination
     * @return
     */
    private Evaluation verifyFromBean(int[] combination) {
        String answer = gameMonitor.verify(guessToString(combination));
        return stringToEval(answer);
    }

    private class MasterClass {

        MyDictionary dictionary;
        int guessCount = 0;
        int colorsNumber;
        int pegsNumber;
        private Guess[] guesses;

        MasterClass(int paramSize, int paramColors, int paramPegs) {
            this.colorsNumber = paramColors;
            this.pegsNumber = paramPegs;
            guesses = new Guess[paramSize];
            dictionary = new MyDictionary(paramPegs, paramColors);
        }

        boolean solve() {
            boolean solved = false;
            Guess guess = new Guess();

            guess.combination = dictionary.firstGuess();
            guess.evaluation = verifyFromBean(guess.combination);

            System.out.format("----> Sprawdzamy: [%s] wynik: %d pozycji %d kolorków \n ",
                    guessToString(guess.combination), guess.evaluation.correctColorInCorrectPosition,
                    guess.evaluation.correctColorInWrongPosition);

            if (guess.evaluation.getCorrectColorInCorrectPosition() == pegsNumber) {
                solved = true;
            }
            guesses[guessCount] = guess;
            guessCount++;
            dictionary.processEvaluation(guess);

            while (!solved && guessCount < guesses.length) {
                guess = new Guess();
                if (dictionary.possibilities.size() == 1) {
                    guess.combination = dictionary.possibilities.iterator().next();
                } else {
                    guess.combination = minMaxNextStep();
                }
                guess.evaluation = verifyFromBean(guess.combination);

                System.out.format("----> Sprawdzamy: [%s] wynik: %d pozycji %d kolorków \n ",
                        guessToString(guess.combination), guess.evaluation.correctColorInCorrectPosition,
                        guess.evaluation.correctColorInWrongPosition);

                if (guess.evaluation.getCorrectColorInCorrectPosition() == pegsNumber) {
                    solved = true;
                }
                guesses[guessCount] = guess;
                dictionary.processEvaluation(guess);
                guessCount++;
            }
            return solved;
        }

        int[] minMaxNextStep() {
            int[] min = new int[dictionary.allPossibilities.size()];
            int[] max = new int[dictionary.allPossibilities.size()];
            Guess[] guess = new Guess[dictionary.allPossibilities.size()];

            for (int i = 0; i < dictionary.allPossibilities.size(); i++) {
                min[i] = Integer.MAX_VALUE;
                max[i] = Integer.MIN_VALUE;
            }

            int index = 0;
            for (int[] combination : dictionary.allPossibilities) {
                for (Evaluation evaluation : dictionary.allEvaluations) {
                    Guess g = new Guess();
                    g.combination = combination;
                    g.evaluation = evaluation;
                    if (!guessUsed(g)) {
                        int count = dictionary.simulateCountProcessEvaluation(g);
                        if (count < min[index]) {
                            min[index] = count;
                        }
                        if (count > max[index]) {
                            max[index] = count;
                        }
                        guess[index] = g;
                    }
                }
            }
            int myMin = Integer.MIN_VALUE;
            int myMax = Integer.MIN_VALUE;
            int myIndex = -1;
            for (int i = 0; i < dictionary.allPossibilities.size(); i++) {
                if (min[index] > myMin) {
                    myMin = min[index];
                    myMax = max[index];
                    myIndex = index;
                } else if (min[index] == myMin) {
                    if (max[index] > myMax) {
                        myMax = max[index];
                        myIndex = index;
                    }
                }
            }
            return guess[myIndex].combination;
        }

        private boolean guessUsed(Guess g) {
            for (int i = 0; i < guessCount; i++) {
                Guess temp = guesses[i];
                if (temp.equals(g)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class Evaluator {

        Evaluation evaluate(int[] suggested, int[] solution) {
            Evaluation evaluation = new Evaluation();
            boolean[] counted = new boolean[suggested.length];
            for (int i = 0; i < solution.length; i++) {
                if (suggested[i] == solution[i]) {
                    counted[i] = true;
                    evaluation.incrementCorrectColorInCorrectPosition();
                }
            }
            boolean[] counted2 = new boolean[suggested.length];
            for (int i = 0; i < solution.length; i++) {
                if (!counted[i]) {
                    int indexContains = contains(solution, suggested[i], counted, counted2);
                    if (indexContains != -1) {
                        counted2[indexContains] = true;
                        evaluation.incrementCorrectColorInWrongPosition();
                    }
                }
            }
            return evaluation;
        }

        private int contains(int[] solution, int key, boolean[] counted, boolean[] counted2) {
            int position = -1;
            for (int index = 0; index < solution.length; index++) {
                if (solution[index] == key && !counted2[index] && !counted[index]) {
                    position = index;
                    return position;
                }
            }
            return position;
        }
    }

    private class MyDictionary {

        int size;
        ArrayHelper arrayHelper = new ArrayHelper();
        Evaluator evaluator = new Evaluator();
        Set<int[]> allPossibilities;
        Set<Evaluation> allEvaluations;
        Set<int[]> possibilities;
        Set<Integer> colors = new HashSet<>();

        MyDictionary(int size, int colorsNumber) {
            for (int i = 0; i < colorsNumber; i++) {
                colors.add(i);
            }
            this.size = size;

            generateAllPossibilities();
            generateAllEvaluations();
            possibilities = new HashSet<>(allPossibilities);
        }

        private void generateAllEvaluations() {
            allEvaluations = new HashSet<>();
            for (int i = 0; i <= size; i++) {
                for (int j = 0; j <= size - i; j++) {
                    Evaluation e = new Evaluation();
                    e.setCorrectColorInCorrectPosition(i);
                    e.setCorrectColorInWrongPosition(j);
                    allEvaluations.add(e);
                }
            }
        }

        private void generateAllPossibilities() {
            allPossibilities = new HashSet<>();
            int index = 0;
            int[] possibility = new int[size];
            generateAllPossibilities(possibility, index);
        }

        private void generateAllPossibilities(int[] possibility, int index) {
            if (index == size) {
                allPossibilities.add(possibility);
            } else {
                for (Integer i : colors) {
                    int[] copy = arrayHelper.clone(possibility);
                    copy[index] = i;
                    generateAllPossibilities(copy, index + 1);
                }
            }
        }

        void processEvaluation(Guess g) {
            processEvaluation(g.combination, g.evaluation);
        }

        void processEvaluation(int[] guess, Evaluation evaluation) {
            Iterator possibilitiesItr = possibilities.iterator();
            while (possibilitiesItr.hasNext()) {
                int[] a = (int[]) possibilitiesItr.next();
                Evaluation e = evaluator.evaluate(a, guess);
                if (!e.equals(evaluation)) {
                    possibilitiesItr.remove();
                }
            }
        }

        int simulateCountProcessEvaluation(Guess g) {
            return simulateCountProcessEvaluation(g.combination, g.evaluation);
        }

        int simulateCountProcessEvaluation(int[] guess, Evaluation evaluation) {
            Iterator possibilitiesItr = possibilities.iterator();
            int count = 0;
            while (possibilitiesItr.hasNext()) {
                int[] a = (int[]) possibilitiesItr.next();
                Evaluation e = evaluator.evaluate(a, guess);
                if (!e.equals(evaluation)) {
                    count++;
                }
            }
            return count;
        }

        int[] firstGuess() {
            int[] guess = new int[size];
            int tempColor = 0;
            for (int i = 0; i < guess.length - 1; i = i + 2) {
                guess[i] = tempColor;
                guess[i + 1] = tempColor;
                tempColor++;
            }
            return guess;
        }

    }

    private class Guess {

        int[] combination;
        Evaluation evaluation;

        boolean equals(Guess g) {
            boolean same = g.combination.length == this.combination.length;
            if (same) {
                for (int i = 0; i < this.combination.length; i++) {
                    if (g.combination[i] != this.combination[i]) {
                        same = false;
                    }
                }
            }
            return same;
        }
    }

    private class ArrayHelper {

        int[] clone(int[] array) {
            int[] copy = new int[array.length];
            System.arraycopy(array, 0, copy, 0, array.length);
            return copy;
        }

        public <T> T[] clone_array(T[] array, Class<T> cls) throws Exception {
            T[] copy = (T[]) Array.newInstance(cls, array.length);
            System.arraycopy(array, 0, copy, 0, array.length);
            return copy;
        }

        boolean contains(int[] array, int key) {
            for (int anArray : array) {
                if (anArray == key) {
                    return true;
                }
            }
            return false;
        }

        public int getMax(int[] array) {
            int max = -1;
            for (int anArray : array) {
                if (anArray > max) {
                    max = anArray;
                }
            }
            return max;
        }

        public void revert(int[] array) {

            int[] temp = clone(array);
            for (int i = 0; i < array.length; i++) {
                array[i] = temp[array.length - 1 - i];
            }
        }

    }

    private class Evaluation {

        private int correctColorInCorrectPosition;
        private int correctColorInWrongPosition;

        Evaluation(int var1, int var2) {
            correctColorInCorrectPosition = var1;
            correctColorInWrongPosition = var2;
        }

        Evaluation() {
            correctColorInCorrectPosition = 0;
            correctColorInWrongPosition = 0;
        }

        void incrementCorrectColorInCorrectPosition() {
            this.correctColorInCorrectPosition = this.correctColorInCorrectPosition + 1;
        }

        void incrementCorrectColorInWrongPosition() {
            this.correctColorInWrongPosition = this.correctColorInWrongPosition + 1;
        }

        int getCorrectColorInCorrectPosition() {
            return correctColorInCorrectPosition;
        }

        void setCorrectColorInCorrectPosition(int correctColorInCorrectPosition) {
            this.correctColorInCorrectPosition = correctColorInCorrectPosition;
        }

        int getCorrectColorInWrongPosition() {
            return correctColorInWrongPosition;
        }

        void setCorrectColorInWrongPosition(int correctColorInWrongPosition) {
            this.correctColorInWrongPosition = correctColorInWrongPosition;
        }

        boolean equals(Evaluation evaluation) {
            return this.getCorrectColorInCorrectPosition() == evaluation.getCorrectColorInCorrectPosition()
                    && this.getCorrectColorInWrongPosition() == evaluation.getCorrectColorInWrongPosition();
        }

    }
}
