package pagecache;

import com.github.kilianB.pcg.fast.PcgRSUFast;
import com.google.common.base.Stopwatch;
import one.nio.os.Mem;
import one.nio.util.JavaInternals;
import org.junit.jupiter.api.*;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PageCacheReadAheadSeqTest {
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
    public void readAheadTest_1(){
        writeToTestSeqReading(4);
        long result = readAheadTest(Mem.POSIX_FADV_SEQUENTIAL, 4);
        System.out.println("Read ahead: " + result);
        output.append(result).append(" ");
    }

    @Test
    @Order(2)
    public void readAheadTest_2(){
        writeToTestSeqReading(64);
        long result = readAheadTest(Mem.POSIX_FADV_SEQUENTIAL, 64);
        System.out.println("Read ahead: " + result);
        output.append(result).append(" ");
    }

    @Test
    @Order(3)
    public void readAheadTest_3(){
        writeToTestSeqReading(128);
        long result = readAheadTest(Mem.POSIX_FADV_SEQUENTIAL, 128);
        System.out.println("Read ahead: " + result);
        output.append(result).append(" ");
    }

    @Test
    @Order(4)
    public void readAheadTest_4(){
        writeToTestSeqReading(256);
        long result = readAheadTest(Mem.POSIX_FADV_SEQUENTIAL, 256);
        System.out.println("Read ahead: " + result);
        output.append(result);
    }

    public long readAheadTest(int fAdviceFlag, int mbs){
        Stopwatch watch = Stopwatch.createStarted();

        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), READ)){
            // file channel
            Field fdChannel = JavaInternals.getField(FileChannelImpl.class, "fd");
            Object fdDescriptor = fdChannel.get(ch);
            // file descriptor value
            Field fdField = JavaInternals.getField(FileDescriptor.class, "fd");
            int fd = fdField.getInt(fdDescriptor);

            Mem.posix_fadvise(fd, 0, ch.size(), fAdviceFlag);

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

        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return watch.elapsed().toMillis();
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

    @AfterAll
    public static void writeOutput() throws IOException {
        String outputFile = "readAheadSeq.txt";
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
}
