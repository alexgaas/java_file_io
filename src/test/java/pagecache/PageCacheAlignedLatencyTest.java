package pagecache;

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
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class PageCacheAlignedLatencyTest {
    private static final String baseMainPath = "./src/main/resources/";
    private static final String baseTestPath = "./src/test/resources/";
    private static final String fileName = String.valueOf(System.currentTimeMillis());
    private static ByteBuffer buf;

    static List<Long> latencies = new ArrayList<>();

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

    @RepeatedTest(10000)
    public void testAlignedRead(){
        writeFileForTesting(1);
        long result = readTest(0);
        latencies.add(result);
    }

    private static ArrayList<Long> getPercentile(){
        // *p*         | 0     | 0.5   | 0.9   | 0.95  | 0.99   | 0.999   | 0.9999
        ArrayList<Long> output = new ArrayList<>();
        output.add(latencies.get(0));
        output.add(latencies.get(50));
        output.add(latencies.get(90));
        output.add(latencies.get(95));
        output.add(latencies.get(99));
        output.add(latencies.get(999));
        output.add(latencies.get(9999));
        return output;
    }

    @AfterAll
    public static void writeOutput() throws IOException {
        Collections.sort(latencies);
        String outputFile = "alignedLatencyPercentile.txt";
        Path path = Paths.get(baseMainPath + outputFile);
        Files.deleteIfExists(path);
        try(FileChannel ch = FileChannel.open(path, CREATE_NEW, APPEND)){
            String output = getPercentile().stream().map(Object::toString)
                    .collect(Collectors.joining(" "));
            ByteBuffer outputBuf = ByteBuffer.wrap(output.getBytes(Charset.defaultCharset()));
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

    private long readTest(int pos){
        Stopwatch watch = Stopwatch.createStarted();
        ByteBuffer buf = ByteBuffer.allocate(4096);
        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), READ)){
            // make N readings with shift
            ch.read(buf, pos);
            ch.read(buf, pos);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return watch.elapsed().toNanos();
    }

    private void writeFileForTesting(int mbs){
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
}
