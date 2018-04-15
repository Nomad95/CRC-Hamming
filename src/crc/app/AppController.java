package app;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class AppController implements Initializable {

    @FXML
    private TextField input;
    @FXML
    private Label result;
    @FXML
    private ComboBox comboCRC;
    @FXML
    private Label checksumTextfield;
    @FXML
    private Label dataAndChecksumLabel;
    @FXML
    private Label labelBefore;
    @FXML
    private Label labelAfter;
    @FXML
    private ComboBox comboError;
    @FXML
    private Label checksumReceived;
    @FXML
    private Label checksumGenerated;
    @FXML
    private Label isEqual;
    @FXML
    private Label errorPoss;
    @FXML
    private Slider generatorSlider;

    private String crcType;
    private Hamming hamming;
    private String oryginalData;
    private int cutBytes = 0;
    Checksum checksum;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        crcType = comboCRC.getValue().toString();
        comboCRC.getSelectionModel().selectedItemProperty()
                .addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                    crcType = newValue;
                    System.out.println("Value is: " + crcType);
                });
        comboError.getSelectionModel().selectedItemProperty()
                  .addListener((ChangeListener<String>) (observable, oldValue, newValue) -> hammingTest(newValue));
    }

    @FXML
    private void handleCountAction(ActionEvent event) {
        String ipt = input.getText();
        if (ipt.length() < 10) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Błąd danych wejściowych");
            alert.setHeaderText("Za krótki ciąg wejściowy!");
            alert.setContentText("Wprowadzany ciąg musi mieć długośc conajmniej 10 znaków!");
            alert.showAndWait();
        } else
            countCrc(ipt);
    }

    private byte[] addArrays(byte[] one, byte[] two) {

        byte[] combined = new byte[one.length + two.length];

        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }
        return combined;
    }

    private long fromByteArray(byte[] bytes) {
        if (bytes.length < 8) {
            byte[] newBytes = new byte[8];
            for (int i = 0; i < 8; i++) {
                if (i < (8 - bytes.length)) {
                    newBytes[i] = 0;
                } else {
                    newBytes[i] = bytes[i - (8 - bytes.length)];
                }
            }
            return ByteBuffer.wrap(newBytes).getLong();
        } else {
            return ByteBuffer.wrap(bytes).getLong();
        }
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.SIZE);
        buffer.putLong(x);
        return buffer.array();
    }

    private void countCrc(String input) {
        byte inputBytes[] = input.getBytes();
        selectCRCType();
        Long checksumValue = getChecksumValueFromInput(inputBytes);
        displayChecksumAsBytesAndDecimal(checksumValue);
        byte[] checksumBytes = getChecksumBytes(checksumValue);
        byte[] dataAndChecksum = addChecksumBytesToTheEndOfInputBytes(inputBytes, checksumBytes);
        displayDataAndChecksum(dataAndChecksum);

        doHamming(dataAndChecksumLabel.getText());

        checksum.reset();
        checksum.update(dataAndChecksum, 0, dataAndChecksum.length - (8 - cutBytes));

        byte[] checksumSent = getSentChecksum(dataAndChecksum);
        Long checksum32Valuenew = checksum.getValue();
        String message = new String(Arrays.copyOfRange(dataAndChecksum, 0, dataAndChecksum.length - (8 - cutBytes)));

        System.out.println("Dziadostwo wyslane: " + fromByteArray(checksumSent));
        System.out.println("Dziadostwo: " + checksum32Valuenew);
        System.out.println("Przekazana wiadomosc: " + message);
    }

    private byte[] getSentChecksum(byte[] dataAndChecksum) {
        return Arrays.copyOfRange(dataAndChecksum, dataAndChecksum.length - (8 - cutBytes), dataAndChecksum.length);
    }

    private void selectCRCType() {
        switch (crcType) {
            case "CRC32":
                checksum = new CRC32();
                cutBytes = 4;
                break;
            case "CRC16":
                checksum = new CRC16();
                cutBytes = 6;
                break;
            case "CRC8":
                checksum = new CRC8(0x9c, (short) 0xff);
                cutBytes = 7;
                break;
        }
    }

    private void displayDataAndChecksum(byte[] dataAndChecksum) {
        dataAndChecksumLabel.setText(stringFromByteArray(dataAndChecksum));
        System.out.println("checksum " + Arrays.toString(dataAndChecksum));
    }

    private byte[] addChecksumBytesToTheEndOfInputBytes(byte[] inputBytes, byte[] checksumBytes) {
        return addArrays(inputBytes, checksumBytes);
    }

    private byte[] getChecksumBytes(Long checksumValue) {
        return Arrays.copyOfRange(longToBytes(checksumValue), cutBytes, 8);
    }

    private void displayChecksumAsBytesAndDecimal(Long checksumValue) {
        checksumTextfield.setText(Long.toBinaryString(checksumValue) + "    [ " + checksumValue + " ]");
    }

    private Long getChecksumValueFromInput(byte[] inputBytes) {
        checksum.update(inputBytes, 0, inputBytes.length);
        return checksum.getValue();
    }

    private String stringFromByteArray(byte[] bytes) {
        String s1 = "";
        for (byte bit : bytes) {
            s1 += String.format("%8s", Integer.toBinaryString(bit & 0xFF)).replace(' ', '0');
        }
        return s1;
    }

    private void doHamming(String data) {
        System.out.println("Before hamming: " + data);
        hamming = new Hamming();
        hamming.setData(data);
        oryginalData = data;

        int[] encoded = hamming.encode();

        createErrorComboOfEncodedLengthAndSetErrorsToZero(encoded.length);

        hamming.fixCorruptedBitString();
        int[] decoded = hamming.decode();

        displayTextBeforeHamming(decoded);//?

        if (hamming.getErrorPosition() != 0) {
            System.out.println("Znaleziono i poprawiono blad na pozycji: " + hamming.getErrorPosition());
        } else {
            System.out.println("Algorytm hamminga nie odnalazl bledow");
        }

        System.out.println("Coded by ham: " + Arrays.toString(encoded));
        System.out.println("Decoded by ham: " + Arrays.toString(decoded));
        System.out.println("docoded checsum: " + Arrays.toString(hammingToBytes(decoded)));
    }

    private void displayTextBeforeHamming(int[] decoded) {
        byte[] bytesBefore = Arrays.copyOfRange(hammingToBytes(decoded), 0, hammingToBytes(decoded).length - (8 - cutBytes));
        labelBefore.setText(new String(bytesBefore));
    }

    private void createErrorComboOfEncodedLengthAndSetErrorsToZero(int encoded) {
        List<String> list = new ArrayList<>();
        ObservableList<String> observableList = FXCollections.observableList(list);

        for (int i = 0; i <= encoded; i++) {
            observableList.add(Integer.toString(i));
        }
        comboError.setItems(observableList);
        comboError.getSelectionModel().selectFirst();
    }

    private void hammingTest(String errors) {
        hamming.setData(oryginalData);
        hamming.encode();
        hamming.interfereCodeWithRandomErrors(Integer.parseInt(errors));
        int[] code = hamming.decode();

        byte[] bytesBefore = Arrays.copyOfRange(hammingToBytes(code), 0, hammingToBytes(code).length - (8 - cutBytes));
        hamming.fixCorruptedBitString();
        int[] fixed = hamming.decode();
        byte[] bytesAfter = Arrays.copyOfRange(hammingToBytes(fixed), 0, hammingToBytes(fixed).length - (8 - cutBytes));
        byte[] bytesAfterWithChecksum = hammingToBytes(fixed);

        labelBefore.setText(new String(bytesBefore));
        labelAfter.setText(new String(bytesAfter));

        if (hamming.getErrorPosition() == 0) {
            errorPoss.setText("nie wykrył błędów");
        } else {
            errorPoss.setText("Wykrył i poprawił błąd na pozycji: " + hamming.getErrorPosition());
            hamming.resetErrorPosition();
        }

        crcTest(bytesAfterWithChecksum);
    }

    private void crcTest(byte[] toTest) {
        checksum.reset();

        checksum.update(toTest, 0, toTest.length - (8 - cutBytes));
        byte checksumSent[] = Arrays.copyOfRange(toTest, toTest.length - (8 - cutBytes), toTest.length);
        Long checksumValueOld = fromByteArray(checksumSent);
        Long checksumValueNew = checksum.getValue();
        String message = new String(Arrays.copyOfRange(toTest, 0, toTest.length - (8 - cutBytes)));

        checksumReceived.setText(checksumValueOld.toString());
        checksumGenerated.setText(checksumValueNew.toString());
        if (Objects.equals(checksumValueOld, checksumValueNew))
            isEqual.setText("Tak");
        else
            isEqual.setText("Nie");

    }

    private byte[] hammingToBytes(int[] encoded) {
        String byteString = "";
        for (int i = 0; i < encoded.length; i++) {
            byteString += encoded[i];
        }

        byte[] bytesArray = new byte[byteString.length() / 8];
        String substring;
        for (int i = 0; i < byteString.length(); i = i + 8) {
            substring = byteString.substring(i, i + 8);
            int val = Integer.parseInt(substring, 2);
            bytesArray[i / 8] = (byte) val;
        }
        return bytesArray;
    }

    private String generateRandomString(double numberOfLetters) {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = (int) numberOfLetters;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @FXML
    public void generateInput() {
        input.setText(generateRandomString(generatorSlider.getValue()));
    }
}
