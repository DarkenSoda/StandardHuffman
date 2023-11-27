import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

public class HuffmanController {
    // #region FXML UI
    @FXML
    private TextArea originalText;
    @FXML
    private TextArea decompressedText;
    @FXML
    private Button compressButton;
    @FXML
    private Button decompressButton;
    @FXML
    private Button selectTextFileButton;
    @FXML
    private Button selectBinaryFileButton;
    @FXML
    private Button saveDecompressedTextButton;
    @FXML
    private Label openedFileLabel;
    // #endregion

    private File fileToDecompress;

    // #region FXML Buttons Action
    @FXML
    void onCompress(ActionEvent event) {
        if(originalText.getText() == null || originalText.getText().length() == 0){
        
            return;
        }
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Binary Files", "*.bin");
        fileChooser.getExtensionFilters().add(textFilter);
        fileChooser.setTitle("Save Compressed Binary File");

        File file = fileChooser.showSaveDialog(null);
        if (file == null)
            return;

        huffmanCompression(file);
    }

    @FXML
    void onDecompress(ActionEvent event) {
        if (fileToDecompress == null)
            return;

        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(fileToDecompress.getAbsolutePath()));

            // Convert each byte to its binary representation and concatenate
            StringBuilder binaryString = new StringBuilder();
            for (byte b : fileBytes) {
                // Convert byte to binary string with leading zeros
                String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                binaryString.append(binary);
            }

            String result = huffmanDecompression(binaryString.toString());
            decompressedText.setText(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void onSaveText(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(textFilter);
        fileChooser.setTitle("Save Text File");

        File file = fileChooser.showSaveDialog(null);
        if (file == null)
            return;

        try (FileWriter fileWriter = new FileWriter(file)) {
            if (!file.exists()) {
                file.createNewFile();
            }

            fileWriter.write(decompressedText.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSelectBinFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Binary Files", "*.bin");
        fileChooser.getExtensionFilters().add(textFilter);
        fileChooser.setTitle("Select Binary File to Decompress");

        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return;

        openedFileLabel.setText("Opened File: " + file.getName());
        fileToDecompress = file;
    }

    @FXML
    void onSelectTextFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(textFilter);
        fileChooser.setTitle("Select Text File to Compress");

        File text = fileChooser.showOpenDialog(null);
        if (text == null)
            return;

        try {
            String content = Files.readString(Path.of(text.getAbsolutePath()));
            originalText.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // #endregion

    private void huffmanCompression(File outputFile) {
        String text = originalText.getText();

        if (text == null || text.length() == 0) {
            return;
        }

        Map<Character, Integer> freqMap = new HashMap<>();

        for (char c : text.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(i -> i.frequency));
        for (var keyValuePair : freqMap.entrySet()) {
            pq.add(new Node(keyValuePair.getKey(), keyValuePair.getValue()));
        }

        // build Tree nodes till only one node remains (root)
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();

            pq.add(new Node(null, left.frequency + right.frequency, left, right));
        }

        Node root = pq.peek();

        Map<Character, String> huffmanCodeMap = new HashMap<>();
        generateHuffman(root, huffmanCodeMap, "");

        String compressedStream = "";
        for (char c : text.toCharArray()) {
            compressedStream += huffmanCodeMap.get(c);
        }

        saveCompression(compressedStream, huffmanCodeMap, outputFile);
    }

    private void generateHuffman(Node root, Map<Character, String> huffmanCodeMap, String code) {
        if (root == null)
            return;

        if (root.isLeaf()) {
            huffmanCodeMap.put(root.character, code.length() > 0 ? code : "1");
        }

        generateHuffman(root.leftChild, huffmanCodeMap, code + '0');
        generateHuffman(root.rightChild, huffmanCodeMap, code + '1');
    }

    private void saveCompression(String compressedStream, Map<Character, String> characterCodes, File outputFile) {
        try {
            BitWriter writer = new BitWriter(outputFile.getAbsolutePath());

            // Write the length of the character codes (8 bits)
            for (char bit : toByte(Integer.toBinaryString(characterCodes.size())).toCharArray()) {
                writer.writeBit(bit - '0');
            }

            // Write each character code
            for (Map.Entry<Character, String> entry : characterCodes.entrySet()) {
                char symbol = entry.getKey();
                String code = entry.getValue();

                // Write the symbol (8 bits)
                // for (char bit : toByte(Integer.toBinaryString(symbol)).toCharArray()) {
                // writer.writeBit(bit - '0');
                // }
                for (int i = 7; i >= 0; i--) {
                    writer.writeBit((symbol >> i) & 1);
                }

                // Write the length of the code (8 bits)
                for (char bit : toByte(Integer.toBinaryString(code.length())).toCharArray()) {
                    writer.writeBit(bit - '0');
                }

                // Write the code itself
                for (char bit : code.toCharArray()) {
                    writer.writeBit(bit - '0');
                }
            }

            // Write the length of the last byte of the compressed stream (3 bits)
            String lastByteLengthBinary = Integer.toBinaryString(compressedStream.length() % 8);

            // Pad with leading zeros to ensure 3 bits
            for (int i = lastByteLengthBinary.length(); i < 3; i++) {
                writer.writeBit(0);
            }

            // Write the actual bits of the last byte length
            for (char bit : lastByteLengthBinary.toCharArray()) {
                writer.writeBit(bit - '0');
            }

            // Write the compressed stream itself
            for (char bit : compressedStream.toCharArray()) {
                writer.writeBit(bit - '0');
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toByte(String s) {
        return String.format("%" + 8 + "s", s).replace(' ', '0');
    }

    private String huffmanDecompression(String data) {
        StringBuilder result = new StringBuilder();

        Map<String, Character> huffmanCodeMap = new HashMap<>();

        int characterCodesLength = Integer.parseInt(data.substring(0, 8), 2);

        int position = 8;
        for (int i = 0; i < characterCodesLength; i++) {
            char symbol = (char) Integer.parseInt(data.substring(position, position + 8), 2);
            position += 8;
            int codeLength = Integer.parseInt(data.substring(position, position + 8), 2);
            position += 8;

            String code = data.substring(position, position + codeLength);
            position += codeLength;

            huffmanCodeMap.put(code, symbol);
        }

        int lastByteSize = Integer.parseInt(data.substring(position, position + 3), 2);
        position += 3;

        String compressedStream = data.substring(position, data.length() - (8 - lastByteSize));
        String code = "";
        for (int i = 0; i < compressedStream.length(); i++) {
            code += compressedStream.charAt(i);

            Character foundCharacter = huffmanCodeMap.get(code);
            if (foundCharacter != null) {
                result.append(foundCharacter);
                code = "";
            }
        }

        return result.toString();
    }
}
