package berlin.yuna.justlog.writer;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.logger.Logger;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

//FIXME: improvements: https://github.com/LMAX-Exchange/disruptor
public class SimpleWriter extends LogWriter implements Serializable {

    @Serial
    private static final long serialVersionUID = -5437769891469935281L;
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

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     * @param config this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    @Override
    public LogWriter config(final LoggerConfig config) {
        encoding = config.getWriterValue("encoding", this.getClass()).map(Charset::forName).orElse(StandardCharsets.US_ASCII);
        bufferSize = config.getWriterValue("buffer-size", this.getClass()).map(Integer::valueOf).filter(i -> i > 0).orElse(100);
        out = createBufferedWriter(false);
        err = createBufferedWriter(true);
        return this;
    }

    @Override
    public LogWriter logOut(final Supplier<String> msg) {
        executor.execute(() -> LogOutImpl(msg.get()));
        return this;
    }

    @Override
    public LogWriter logError(final Supplier<String> msg) {
        executor.execute(() -> LogErrImpl(msg.get()));
        return this;
    }

    public Charset encoding() {
        return encoding;
    }

    public int bufferSize() {
        return bufferSize;
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
