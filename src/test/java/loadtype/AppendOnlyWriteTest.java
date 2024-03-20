package loadtype;

import org.junit.jupiter.api.*;
import com.github.kilianB.pcg.fast.PcgRSUFast;
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

import com.google.common.base.Stopwatch;

import static java.nio.file.StandardOpenOption.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppendOnlyWriteTest {
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
    public void test_1GB(){
        long result = testAppendOnlyWrite(1);
        System.out.println("Append-only write 1GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(2)
    public void test_2GB(){
        long result = testAppendOnlyWrite(2);
        System.out.println("Append-only write 2GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(3)
    public void test_4GB(){
        long result = testAppendOnlyWrite(4);
        System.out.println("Append-only write 4GB: " + result + " ms");
        output.append(result).append(" ");
    }

    @Test
    @Order(4)
    public void test_8GB(){
        long result = testAppendOnlyWrite(8);
        System.out.println("Append-only write 8GB: " + result + " ms");
        output.append(result);
    }

    @AfterAll
    public static void writeOutput() throws IOException {
        String outputFile = "appendOnlyWrite.txt";
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

    private long testAppendOnlyWrite(int gbs) {
        Stopwatch watch = Stopwatch.createStarted();
        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), CREATE_NEW, APPEND)){
            int mbCount = 1024 * gbs;
            int counter = 0;
            while(counter < mbCount){
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
        return watch.elapsed().toMillis();
    }
}
