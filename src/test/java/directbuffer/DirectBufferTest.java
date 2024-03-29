package directbuffer;

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
import static java.nio.file.StandardOpenOption.READ;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DirectBufferTest {
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
    public void test_1MB(){
        writeToTestSeqReading(1);
        long result = testHeapBufferReading(1);
        System.out.println("Direct buffer reading 1MB: " + result);
        output.append(result).append(" ");
    }

    @Test
    @Order(2)
    public void test_16MB(){
        writeToTestSeqReading(16);
        long result = testHeapBufferReading(16);
        System.out.println("Direct buffer reading 16MB: " + result);
        output.append(result).append(" ");
    }

    @Test
    @Order(3)
    public void test_256MB(){
        writeToTestSeqReading(256);
        long result = testHeapBufferReading(256);
        System.out.println("Direct buffer reading 256MB: " + result);
        output.append(result).append(" ");
    }

    @Test
    @Order(4)
    public void test_1GB(){
        writeToTestSeqReading(1024);
        long result = testHeapBufferReading(1024);
        System.out.println("Direct buffer reading 1GB: " + result);
        output.append(result);
    }

    @AfterAll
    public static void writeOutput() throws IOException {
        String outputFile = "directBufferReading.txt";
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

    private void writeToTestSeqReading(int mbs) {
        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), CREATE_NEW, WRITE)){
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

    private long testHeapBufferReading(int mbs) {
        Stopwatch watch = Stopwatch.createStarted();
        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), READ)){
            int counter = 0;
            while(counter < mbs){
                while(buf.hasRemaining()){
                    int bytes = ch.read(buf);
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
        return watch.elapsed(TimeUnit.MICROSECONDS);
    }
}
