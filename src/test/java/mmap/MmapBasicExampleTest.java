package mmap;

import com.github.kilianB.pcg.fast.PcgRSUFast;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.*;

public class MmapBasicExampleTest {
    private static final String baseMainPath = "./src/main/resources/";
    private static final String baseTestPath = "./src/test/resources/";
    private static final String fileName = String.valueOf(System.currentTimeMillis());
    private static ByteBuffer buf;

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
    public void testMmap(){
        writeFileForTesting(10);
        mmapBasicTest();
    }

    private long mmapBasicTest(){
        Stopwatch watch = Stopwatch.createStarted();
        ByteBuffer buf = ByteBuffer.allocate(4096);
        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), READ, WRITE)){
            MappedByteBuffer mmap = ch.map(FileChannel.MapMode.READ_WRITE, 0, ch.size());
            // load data in memory
            mmap.load();
            // make some operations with memory using buffer same way as we do with file
            mmap.put("test".getBytes(Charset.defaultCharset()));
            // flash data to file back
            mmap.force();
        } catch (Exception e) {
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
