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
    protected int[] bitTypes;    // 0 - poprawny bit danych, 1 - przekłamany bit danych, 2 - niepewny bit danych, 3 - poprawny bit redundantny, 4 - przekłamany bit redundantny, 5 - niepewny bit redundantny
    protected int errors = 0;
    protected int errorPosition = 0;
    private Random randomGenerator;

//    public void setData(int[] data) {
//        this.data = new int[data.length];
//        System.arraycopy(data, 0, this.data, 0, data.length);
//        errors = 0;
//    }

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

//    public void setCode(int[] code) {
//        this.code = new int[code.length];
//        this.bitTypes = new int[code.length];
//        System.arraycopy(code, 0, this.code, 0, code.length);
//    }

//    public void setCode(String str) {
//        int codeLength = str.length();
//        this.code = new int[codeLength];
//        this.bitTypes = new int[codeLength];
//
//        for (int i = 0; i < codeLength; i++) {
//            if (str.charAt(i) == '1') {
//                this.code[i] = 1;
//            } else {
//                this.code[i] = 0;
//            }
//        }
//        this.errors = 0;
//    }

    abstract int[] encode();

    abstract int[] decode();

//    public int getDataBitsNumber() {
//        return data.length;
//    }

//    public int getControlBitsNumber() {
//        return code.length - data.length;
//    }

//    public int getDetectedErrorsNumber() {
//        return errors;
//    }

//    public int getNumberOfFixedErrors() {
//        int numberOfFixedErrors = 0;
//        for (int bitType : bitTypes) {
//            if (bitType == 1 || bitType == 4) {
//                numberOfFixedErrors++;
//            }
//        }
//        return numberOfFixedErrors;
//    }

//    public int[] getBitTypes() {
//        return bitTypes;
//    }

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

//    public void negate(int position) {
//        if (position < code.length) {
//
//            if (code[position] == 1) {
//                code[position] = 0;
//            } else {
//                code[position] = 1;
//            }
//
//            if (bitTypes[position] == 1) {
//                bitTypes[position] = 0;
//            } else {
//                bitTypes[position] = 1;
//            }
//        } else {
//            //throw new IllegalStateException("Positon")
//            System.err.println("Position greater or equal then code lenght. Cannot negate.");
//        }
//    }

    public int getErrorPosition() {
        return errorPosition;
    }

    public void resetErrorPosition() {
        errorPosition = 0;
    }

//    public int[] getCode() {
//        return code;
//    }

}

