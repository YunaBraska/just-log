package berlin.yuna.justlog.writer;

import berlin.yuna.justlog.logger.Logger;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

//FIXME: improvements: https://github.com/LMAX-Exchange/disruptor
public class SimpleWriter extends LogWriter {

    private BufferedWriter out;
    private BufferedWriter err;
    private Charset encoding;
    private int bufferSize;
    private final Executor executor;


    public SimpleWriter() {
        executor = Logger.getExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeWriter(false);
            closeWriter(true);
        }));
    }

    @Override
    public LogWriter config(final Map<String, String> config) {
        encoding = Optional.ofNullable(config.get("encoding")).map(Charset::forName).orElse(StandardCharsets.US_ASCII);
        bufferSize = Optional.ofNullable(config.get("buffer-size")).map(Integer::valueOf).filter(i -> i > 0).orElse(100);
        out = createBufferedWriter(false);
        err = createBufferedWriter(true);
        return this;
    }

    @Override
    public LogWriter logOut(final String msg) {
        executor.execute(() -> LogOutImpl(msg));
        return this;
    }

    @Override
    public LogWriter logError(final String msg) {
        executor.execute(() -> LogErrImpl(msg));
        return this;
    }

    private void LogOutImpl(final String msg) {
        try {
            out.write(msg);
        } catch (Exception e) {
            e.printStackTrace();
            recreateWriter(false);
        }
    }

    private void LogErrImpl(final String msg) {
        try {
            err.write(msg);
        } catch (Exception e) {
            e.printStackTrace();
            recreateWriter(true);
        }
    }

    private SimpleWriter recreateWriter(final boolean errorWriter) {
        closeWriter(errorWriter);
        if (!errorWriter) {
            out = createBufferedWriter(false);
        } else {
            err = createBufferedWriter(true);
        }
        return this;
    }

    private SimpleWriter closeWriter(final boolean errorWriter) {
        try {
            if (!errorWriter) {
                out.close();
            } else {
                err.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private BufferedWriter createBufferedWriter(final boolean error) {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(error ? FileDescriptor.err : FileDescriptor.out),
                        Optional.ofNullable(encoding).orElse(StandardCharsets.US_ASCII)
                ), bufferSize
        );
    }
}
