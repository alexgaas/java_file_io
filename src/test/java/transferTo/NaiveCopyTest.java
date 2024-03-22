package transferTo;

import com.github.kilianB.pcg.fast.PcgRSUFast;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NaiveCopyTest {
    private static final String baseMainPath = "./src/main/resources/";
    private static final String baseTestPath = "./src/test/resources/";
    private static final String fileName = String.valueOf(System.currentTimeMillis());

    private static ByteBuffer buf;

    private static final StringBuilder output = new StringBuilder();

    @BeforeAll
    public static void setupRandomData(){
        buf = ByteBuffer.allocateDirect(1024 * 1024);
        while(buf.hasRemaining()){
            buf.putChar(PcgRSUFast.nextChar());
        }
        buf.flip();
    }

    @AfterEach
    public void purgeResourceDirectory() {
        Arrays.stream(Objects.requireNonNull(new File(baseTestPath).listFiles())).forEach(File::delete);
    }

    @Test
    @Order(1)
    public void test_64MB(){
        testAppendOnlyWrite(64);
        long result = copyFile();
        System.out.println("Naive copy 1GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(2)
    public void test_256MB(){
        testAppendOnlyWrite(256);
        long result = copyFile();
        System.out.println("Naive copy 2GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(3)
    public void test_512MB(){
        testAppendOnlyWrite(512);
        long result = copyFile();
        System.out.println("Naive copy 4GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(4)
    public void test_1GB(){
        testAppendOnlyWrite(1024);
        long result = copyFile();
        System.out.println("Naive copy 8GB: " + result + " ms");
        output.append(result);
    }

    @AfterAll
    public static void writeOutput() throws IOException {
        String outputFile = "naiveCopy.txt";
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

    private void testAppendOnlyWrite(int mbs) {
        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), CREATE_NEW, APPEND)){
            int counter = 0;
            while(counter < mbs){
                while(buf.hasRemaining()){
                    int bytes = ch.write(buf);
                    if (bytes <= 0){
                        break;
                    }
                }
                buf.flip();
                counter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long copyFile() {
        Stopwatch watch = Stopwatch.createStarted();
        try(FileChannel src = FileChannel.open(Paths.get(baseTestPath + fileName), READ);
            FileChannel dest = FileChannel.open(Paths.get(baseTestPath + fileName + "_copy"), CREATE_NEW, WRITE)){
                ByteBuffer buf = ByteBuffer.allocate((int) src.size());
                while(buf.hasRemaining()){
                    int bytes = src.read(buf);
                    if (bytes <= 0){
                        break;
                    }
                    bytes = dest.write(buf);
                    if (bytes <= 0){
                        break;
                    }
                }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return watch.elapsed(TimeUnit.MILLISECONDS);
    }
}
