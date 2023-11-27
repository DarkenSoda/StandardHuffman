import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitWriter {
    private OutputStream outputStream;
    private int currentByte;
    private int numBitsFilled;

    public BitWriter(String filePath) throws IOException {
        this.outputStream = new FileOutputStream(filePath, false);
        this.currentByte = 0;
        this.numBitsFilled = 0;
    }

    public void writeBit(int bit) throws IOException {
        if (!(bit == 0 || bit == 1)) {
            throw new IllegalArgumentException("Argument must be 0 or 1");
        }

        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;

        if (numBitsFilled == 8) {
            outputStream.write(currentByte);
            currentByte = 0;
            numBitsFilled = 0;
        }
    }
    
    public void close() throws IOException {
        if (numBitsFilled > 0) {
            currentByte = currentByte << (8 - numBitsFilled);
            outputStream.write(currentByte);
        }
        currentByte = 0;
        numBitsFilled = 0;
        outputStream.close();
    }
}