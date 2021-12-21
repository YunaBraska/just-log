package berlin.yuna.logtest;

import berlin.yuna.justlog.formatter.SimpleLogFormatter;
import berlin.yuna.justlog.logger.DefaultLogger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

@Disabled
class PerformanceTest extends LoggerTestBase {

    public static int LOG_ITEMS = 1000000;

    private static final Map<String, Long> RESULT = new LinkedHashMap<>();
    private static final String LOG_MESSAGE1 = "MESSAGE1";
    private static final String LOG_MESSAGE2 = "MESSAGE2";
    protected static final ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    @Test
    void singleThreadTest() throws InterruptedException {
        LOG_ITEMS = 1000000;
        logBuffer();
        logPrintWriter();
        justLogNew();
        logSystem();
        logJava();
        slf4j();
        printResult();
    }

    //FIXME: Send async log messages instead og async logger :/
    @Test
    void multiThreadTest() throws InterruptedException {
        LOG_ITEMS = 500000;
        executor.submit(PerformanceTest::logBuffer);
        executor.submit(PerformanceTest::logBuffer);
        executor.submit(PerformanceTest::logPrintWriter);
        executor.submit(PerformanceTest::logPrintWriter);
        executor.submit(PerformanceTest::justLogNew);
        executor.submit(PerformanceTest::justLogNew);
        executor.submit(PerformanceTest::logSystem);
        executor.submit(PerformanceTest::logSystem);
        executor.submit(PerformanceTest::logJava);
        executor.submit(PerformanceTest::logJava);
        executor.submit(PerformanceTest::slf4j);
        executor.submit(PerformanceTest::slf4j);
        printResult();
    }

    private void printResult() throws InterruptedException {
        while (executor.getActiveThreadCount() != 0 || ((ForkJoinPool) berlin.yuna.justlog.logger.Logger.getExecutor()).getActiveThreadCount() != 0) {
            Thread.sleep(100);
        }
        Thread.sleep(2000);
        RESULT.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> System.err.printf("LogItems [%s] LoggerType [%s], needed time [%s ms]%n", LOG_ITEMS, e.getKey(), e.getValue()));
    }

    private static void logBuffer() {
        try {
            final long start = System.currentTimeMillis();
            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(java.io.FileDescriptor.out), "ASCII"), 512);
            for (int i = 0; i < LOG_ITEMS; i++) {
                out.write(LOG_MESSAGE1);
                out.write(String.valueOf(i));
                out.write('\n');
            }
            out.flush();
            RESULT.put(BufferedWriter.class.getSimpleName(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logPrintWriter() {
        try {
            final long start = System.currentTimeMillis();
            final PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(java.io.FileDescriptor.out), "UTF-8"), 512));
            for (int i = 0; i < LOG_ITEMS; i++) {
                out.write(LOG_MESSAGE1);
                out.write(String.valueOf(i));
                out.write('\n');
            }
            out.flush();
            RESULT.put(PrintWriter.class.getSimpleName(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logJava() {
        final long start = System.currentTimeMillis();
        final Logger logger = Logger.getLogger("TestLogger");
        for (int i = 0; i < LOG_ITEMS; i++) {
            logger.info(LOG_MESSAGE1);
        }
        RESULT.put("Java Util Logger", System.currentTimeMillis() - start);
    }

    private static void slf4j() {
        final org.slf4j.Logger logger = LoggerFactory.getLogger(PerformanceTest.class);
        final long start = System.currentTimeMillis();
        for (int i = 0; i < LOG_ITEMS; i++) {
            logger.info(LOG_MESSAGE1);
        }
        RESULT.put("Slf4j", System.currentTimeMillis() - start);
    }

    private static void justLogNew() {
        final berlin.yuna.justlog.logger.Logger logger = DefaultLogger.instance();
        logger.formatter(new SimpleLogFormatter().pattern(null));
//        logger.formatter(new SimpleLogFormatter2().logger(logger).pattern("%l{l=5} %c{l=10} - %m"));
//        logger.formatter(new SimpleLogFormatter2().logger(logger).pattern("[%p{l=10}] [%l{l=5}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=0}:%M:%L] %m%n%e{p=berlin,yuna}"));
//        logger.formatter(new SimpleLogFormatter().config(logger, new HashMap<>()).pattern("%d{p=HH:mm:ss.SSS} [%l] %l{l=5} %c{l=36}  %h{l=10}- %m%n"));
        final long start = System.currentTimeMillis();
        for (int i = 0; i < LOG_ITEMS; i++) {
            logger.info(() -> LOG_MESSAGE2);
        }
        while (logger.isRunning()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        RESULT.put("JustLog (New)", System.currentTimeMillis() - start);
    }

    private static void logSystem() {
        final long start = System.currentTimeMillis();
        for (int i = 0; i < LOG_ITEMS; i++) {
            System.out.println(LOG_MESSAGE1);
        }
        RESULT.put("System out", System.currentTimeMillis() - start);
    }
}
