package app;

import java.util.Random;

abstract class AbstractErrorDetectionAlgorithm {

    protected static int CORRECT = 0;
    protected static int INCORRECT = 1;
    protected static int UNCERTAIN = 2;
    protected static int CORRECT_REDUNDANT = 3;
    protected static int INCORRECT_REDUNDANT = 4;
    protected static int UNCERTAIN_REDUNDANT = 5;

    protected int[] data;
    protected int[] code;
    protected int[] bitTypes;
    protected int errors = 0;
    protected int errorPosition = 0;
    private Random randomGenerator;

    public void setData(String str) {
        int bitStringLength = str.length();
        data = new int[bitStringLength];
        for (int i = 0; i < bitStringLength; i++) {
            if (str.charAt(i) == '1') {
                data[i] = 1;
            } else {
                data[i] = 0;
            }
        }
        errors = 0;
    }

    abstract int[] encode();

    abstract int[] decode();

    abstract void fixCorruptedBitString();

    public void interfereCodeWithRandomErrors(int numberOfErrors) {
        int codeLength = code.length;
        if (numberOfErrors > codeLength) {
            numberOfErrors = codeLength;
        }
        int corruptedBits = 0;
        clearBitTypesArray(codeLength);

        while (corruptedBits < numberOfErrors) {
            corruptRandomBitOfCode(codeLength);
            corruptedBits++;
        }
    }

    private void corruptRandomBitOfCode(int codeLength) {
        if (randomGenerator == null) {
            randomGenerator = new Random();
        }
        int positionOfBitToCorrupt = randomGenerator.nextInt(codeLength);

        if (code[positionOfBitToCorrupt] == 1) {
            code[positionOfBitToCorrupt] = 0;
        } else {
            code[positionOfBitToCorrupt] = 1;
        }
        bitTypes[positionOfBitToCorrupt] = 1;
    }

    private void clearBitTypesArray(int codeLength) {
        for (int i = 0; i < codeLength; i++) {
            bitTypes[i] = 0;
        }
    }

    public int getErrorPosition() {
        return errorPosition;
    }

    public void resetErrorPosition() {
        errorPosition = 0;
    }

}

