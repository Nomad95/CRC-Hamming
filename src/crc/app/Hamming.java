package app;

public class Hamming extends CodeBase {
    @Override
    int[] encode() {
        int dataLength = data.length;
        int i = 0, redundancy = 0, sum = 0;

        while (i < dataLength) { // liczenie długości kodu
            if (Math.pow(2, redundancy) - 1 == sum)
                redundancy++;    // potęga 2 - bit redundantny
            else
                i++;// bit danych

            sum++;
        }

        code = new int[sum];
        bitTypes = new int[sum];

        long mask = 0;
        redundancy = 0;
        i = 0;
        sum = 0;
        while (i < dataLength) { // przepisz dane i wylicz bity kontrolne
            if (Math.pow(2, redundancy) - 1 == sum) redundancy++;
            else {
                code[sum] = data[i];
                if (data[i] == 1)
                    mask ^= sum + 1;
                i++;
            }
            sum++;
        }

        redundancy = 0;        // tutaj nadmiar pełni też rolę numeru bitu w masce
        for (i = 0; i < sum; i++) {    // zapisz bity kontrolne
            if (Math.pow(2, redundancy) - 1 == i) {
                if ((mask & ((long) 1 << redundancy)) == 0)
                    code[i] = 0;
                else
                    code[i] = 1;
                redundancy++;
            }
        }

        return code;
    }

    @Override
    int[] decode() {
        int n = code.length;
        int d = 0;
        int nadmiar = 0;
        for (int i = 0; i < n; i++)        // liczenie długości danych
        {
            if (Math.pow(2, nadmiar) - 1 != i) d++;
            else nadmiar++;
        }

        data = new int[d];
        d = 0;
        nadmiar = 0;

        for (int i = 0; i < n; i++)        // przepisanie danych
        {
            if (Math.pow(2, nadmiar) - 1 != i)    // bit danych
            {
                data[d] = code[i];
                d++;
            } else nadmiar++;
        }

        return data;
    }

    @Override
    void fixCorruptedBitString() {
        int n = code.length;
        int d = 0;
        int nadmiar = 0;
        for (int i = 0; i < n; i++)        // liczenie długości danych
        {
            if (Math.pow(2, nadmiar) - 1 != i) d++;
            else nadmiar++;
        }

        data = new int[d];

        int maska = 0;
        d = 0;
        nadmiar = 0;

        for (int i = 0; i < n; i++) {
            // kontrola poprawności
            if (code[i] == 1) maska ^= i + 1;

            // określanie typu bitów
            if (Math.pow(2, nadmiar) - 1 != i)        // bit danych
            {
                d++;
                bitTypes[i] = 0;            // poprawny (jak na razie) bit danych
            } else {
                bitTypes[i] = 3;                // poprawny (jak na razie) bit redundantny
                nadmiar++;
            }
        }

        if (maska != 0)                    // wykryto błąd
        {
            errors++;
            int numer = maska - 1;            // numeracja bitów od 1, tablicy - od 0

            errorPosition = numer;

            if (numer < code.length) {
                if (bitTypes[numer] == 0) bitTypes[numer] = 1;    // korygujemy bit danych
                else if (bitTypes[numer] == 3) bitTypes[numer] = 4;    // korygujemy bit redundantny

                if (code[numer] == 1) code[numer] = 0;
                else code[numer] = 1;
            }
        }
    }
}

