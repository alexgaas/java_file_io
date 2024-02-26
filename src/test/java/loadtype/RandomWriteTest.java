package loadtype;

import com.github.kilianB.pcg.fast.PcgRSUFast;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class RandomWriteTest {
    private static final String baseMainPath = "./src/main/resources/";
    private static final String baseTestPath = "./src/test/resources/";
    private static final String fileName = String.valueOf(System.currentTimeMillis());
    private static StringBuilder charBuf;

    private static final StringBuilder output = new StringBuilder();

    @BeforeAll
    public static void setupRandomData(){
        charBuf = new StringBuilder(1024 * 1024);
        while(charBuf.length() < 1024 * 1024) {
            charBuf.append(PcgRSUFast.nextChar());
        }
    }

    @AfterEach
    public void purgeResourceDirectory() {
        Arrays.stream(Objects.requireNonNull(new File(baseTestPath).listFiles())).forEach(File::delete);
    }

    @Test
    @Order(1)
    public void test_1GB(){
        long result = testRandomWrite(1);
        System.out.println("Random write 1GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(2)
    public void test_2GB(){
        long result = testRandomWrite(2);
        System.out.println("Random write 2GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(3)
    public void test_4GB(){
        long result = testRandomWrite(4);
        System.out.println("Random write 4GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(4)
    public void test_8GB(){
        long result = testRandomWrite(8);
        System.out.println("Random write 8GB: " + result + " ms");
        output.append(result);
    }

    @AfterAll
    public static void writeOutput() throws IOException {
        String outputFile = "randomWrite.txt";
        Path path = Paths.get(baseMainPath + outputFile);
        Files.deleteIfExists(path);
        try(FileChannel ch = FileChannel.open(path, CREATE_NEW, APPEND)){
            ByteBuffer outputBuf = ByteBuffer.wrap(output.toString().getBytes(Charset.defaultCharset()));
            while(outputBuf.hasRemaining()){
                int bytes = ch.write(outputBuf);
                if (bytes <= 0){
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long testRandomWrite(int gbs) {
        Stopwatch watch = Stopwatch.createStarted();
        try(RandomAccessFile file = new RandomAccessFile(baseTestPath + fileName, "rw")){
            file.seek(0);
            int mbCount = 1024 * gbs;
            int counter = 0;
            while(counter < mbCount){
                file.writeBytes(charBuf.toString());
                counter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return watch.elapsed().toMillis();
    }
}
