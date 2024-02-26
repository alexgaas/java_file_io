package pagecache;

import com.github.kilianB.pcg.fast.PcgRSUFast;
import one.nio.os.Mem;
import one.nio.util.JavaInternals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.*;

public class PageCacheReadAheadTest {
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
    public void disableReadAheadTest(){
        writeToTestSeqReading(1);

        try(FileChannel ch = FileChannel.open(Paths.get(baseTestPath + fileName), READ)){
            // file channel
            Field fdChannel = JavaInternals.getField(FileChannelImpl.class, "fd");
            Object fdDescriptor = fdChannel.get(ch);
            // file descriptor value
            Field fdField = JavaInternals.getField(FileDescriptor.class, "fd");
            int fd = fdField.getInt(fdDescriptor);

            int fAdviceFlag = Mem.POSIX_FADV_RANDOM;
            Mem.posix_fadvise(fd, 0, ch.size(), fAdviceFlag);
            while(buf.hasRemaining()){
                int bytes = ch.read(buf);
                if (bytes <= 0){
                    break;
                }
            }
        } catch (IOException | IllegalAccessException e) {
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
}
