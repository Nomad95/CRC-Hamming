package app;

public class Hamming extends CodeBase {

    private static int CORRECT = 0;
    private static int INCORRECT = 1;
    private static int UNCERTAIN = 2;
    private static int CORRECT_REDUNDANT = 3;
    private static int INCORRECT_REDUNDANT = 4;
    private static int UNCERTAIN_REDUNDANT = 5;

    @Override
    int[] encode() {
        int dataLength = data.length;
        int i = 0, redundancy = 0, sum = 0;

        sum = computeHammingCodeLength(dataLength, i, redundancy, sum);

        code = new int[sum];
        bitTypes = new int[sum];

        long mask = 0;
        redundancy = 0;
        i = 0;
        sum = 0;

        // wylicza bity kontrolne
        while (i < dataLength) {
            if (Math.pow(2, redundancy) - 1 == sum) {
                redundancy++;
            } else {
                code[sum] = data[i];
                if (data[i] == 1)
                    mask ^= sum + 1;
                i++;
            }
            sum++;
        }

        redundancy = 0;
        saveControlBytes(redundancy, sum, mask);

        return code;
    }

    private void saveControlBytes(int redundancy, int sum, long mask) {
        int i;
        for (i = 0; i < sum; i++) {
            if (Math.pow(2, redundancy) - 1 == i) {
                if ((mask & ((long) 1 << redundancy)) == 0)
                    code[i] = 0;
                else
                    code[i] = 1;
                redundancy++;
            }
        }
    }

    private int computeHammingCodeLength(int dataLength, int i, int redundancy, int sum) {
        while (i < dataLength) {
            if (Math.pow(2, redundancy) - 1 == sum)
                redundancy++;    // potęga 2 - bit redundantny
            else
                i++;// bit danych

            sum++;
        }

        return sum;
    }

    @Override
    int[] decode() {
        int codedLength = code.length;
        int originalLength = 0;
        int redundancy = 0;

        originalLength = computeOriginalDataLength(codedLength, originalLength, redundancy);

        data = new int[originalLength];
        originalLength = 0;
        redundancy = 0;

        getOriginalData(codedLength, originalLength, redundancy);

        return data;
    }

    private void getOriginalData(int codeLength, int originalLength, int redundancy) {
        for (int i = 0; i < codeLength; i++) {
            if (Math.pow(2, redundancy) - 1 != i) {
                data[originalLength] = code[i];
                originalLength++;
            } else
                redundancy++;
        }
    }

    private int computeOriginalDataLength(int codeLength, int originalLength, int redundancy) {
        for (int i = 0; i < codeLength; i++) {
            if (Math.pow(2, redundancy) - 1 != i)
                originalLength++;
            else
                redundancy++;
        }
        return originalLength;
    }

    @Override
    void fixCorruptedBitString() {
        int codedLength = code.length;
        int originalLength = 0;
        int redundancy = 0;

        originalLength = computeOriginalDataLength(codedLength, originalLength, redundancy);

        data = new int[originalLength];

        int mask = 0;
        originalLength = 0;
        redundancy = 0;

        for (int i = 0; i < codedLength; i++) {
            // kontrola poprawności
            if (code[i] == INCORRECT)
                mask ^= i + 1;

            // określanie typu bitów
            if (Math.pow(2, redundancy) - 1 != i) {
                originalLength++;
                bitTypes[i] = CORRECT;
            } else {
                bitTypes[i] = CORRECT_REDUNDANT;
                redundancy++;
            }
        }

        if (errorWasDetected(mask)) {
            errors++;
            int number = mask - 1;            // numeracja bitów od 1, tablicy - od 0

            errorPosition = number;

            if (number < code.length) {
                if (bitTypes[number] == CORRECT)
                    bitTypes[number] = INCORRECT;              // korygujemy bit danych
                else if (bitTypes[number] == CORRECT_REDUNDANT)
                    bitTypes[number] = INCORRECT_REDUNDANT;    // korygujemy bit redundantny

                if (code[number] == INCORRECT)
                    code[number] = CORRECT;
                else code[number] = INCORRECT;
            }
        }
    }

    private boolean errorWasDetected(int mask) {
        return mask != 0;
    }
}

