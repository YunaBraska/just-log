package berlin.yuna.justlog;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class TreeWalker extends SimpleFileVisitor<Path> {

    private int deep = -1;
    private int fileVisitCount = 0;
    private int limit = -1;
    private long endMs = -1;
    private long startMs = -1;
    private Path root = Path.of(System.getProperty("user.dir"));

    private final List<Path> result = new ArrayList<>();
    private final Collection<PathMatcher> filePatterns = new HashSet<>();
    private final Collection<PathMatcher> skipPatterns = new HashSet<>();
    private final Collection<String> filePaths = new HashSet<>();
    private final Collection<String> skipPaths = new HashSet<>();
    private final Collection<Consumer<Path>> listener = new ArrayList<>();

    public static TreeWalker walkTree() {
        return new TreeWalker();
    }

    public static TreeWalker walkTree(final Path root) {
        return new TreeWalker().root(root);
    }

    public Optional<Path> findFirst() {
        run(1);
        return firstResult();
    }

    public Optional<Path> firstResult() {
        return result.isEmpty() ? Optional.empty() : Optional.of(result.iterator().next());
    }

    public List<Path> list() {
        return run(-1);
    }

    public List<Path> listResults() {
        return result;
    }

    public TreeWalker run() {
        run(limit);
        return this;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);
        final Path relPath = root.relativize(dir);
        if ((deep != -1 && relPath.getNameCount() >= deep)
                || (!skipPaths.isEmpty() && skipPaths.contains(relPath.toString()))
                || (!skipPatterns.isEmpty() && skipPatterns.stream().anyMatch(pattern -> pattern.matches(relPath)))
        ) {
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return getResult();
        }
    }

    private FileVisitResult getResult() {
        return limit != -1 && fileVisitCount >= limit ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        fileVisitCount++;
        final Path relPath = root.relativize(file);
        if (
                (!filePatterns.isEmpty() && filePatterns.stream().anyMatch(pattern -> pattern.matches(relPath)))
                        || (!filePaths.isEmpty() && filePaths.contains(relPath.toString()))
                        || (filePatterns.isEmpty() && filePaths.isEmpty())
        ) {
            listener.forEach(peek -> peek.accept(file));
            result.add(file);
        }
        return getResult();
    }

    public Path root() {
        return root;
    }

    public TreeWalker root(final Path root) {
        this.root = root;
        return this;
    }

    public Collection<String> filePaths() {
        return filePaths;
    }

    public Collection<PathMatcher> filePatterns() {
        return filePatterns;
    }

    public TreeWalker filePatterns(final PathMatcher... filePatterns) {
        return filePatterns(List.of(filePatterns));
    }

    public TreeWalker filePatterns(final Collection<PathMatcher> filePatterns) {
        this.filePatterns.addAll(filePatterns);
        return this;
    }

    public TreeWalker filePatterns(final String... filePatterns) {
        return filePatterns(List.of(filePatterns));
    }

    public TreeWalker filePatterns(final Iterable<String> filePatterns) {
        filePatterns.forEach(filePaths::add);
        return this;
    }

    public Collection<String> ignorePaths() {
        return skipPaths;
    }

    public Collection<PathMatcher> ignorePatterns() {
        return skipPatterns;
    }

    public TreeWalker ignorePatterns(final PathMatcher... filePatterns) {
        return ignorePatterns(List.of(filePatterns));
    }

    public TreeWalker ignorePatterns(final Collection<PathMatcher> filePatterns) {
        this.skipPatterns.addAll(filePatterns);
        return this;
    }

    public TreeWalker ignorePatterns(final String... filePatterns) {
        return ignorePatterns(List.of(filePatterns));
    }

    public TreeWalker ignorePatterns(final Iterable<String> filePatterns) {
        filePatterns.forEach(skipPaths::add);
        return this;
    }

    public Collection<Consumer<Path>> fileListener() {
        return listener;
    }

    public TreeWalker fileListener(final Consumer<Path>... peeks) {
        return fileListener(List.of(peeks));
    }

    public TreeWalker fileListener(final Collection<Consumer<Path>> peeks) {
        this.listener.addAll(peeks);
        return this;
    }

    public int deep() {
        return deep;
    }

    public TreeWalker deep(final int deep) {
        this.deep = deep;
        return this;
    }

    public int fileVisitCount() {
        return fileVisitCount;
    }

    public int size() {
        return result.size();
    }

    public boolean hasResults() {
        return !result.isEmpty();
    }

    public int limit() {
        return limit;
    }

    public TreeWalker limit(final int limit) {
        this.limit = limit;
        return this;
    }

    public long durationMs() {
        return endMs - startMs;
    }

    private List<Path> run(final int limit) {
        startMs = System.currentTimeMillis();
        result.clear();
        fileVisitCount = 0;
        this.limit = limit;
        try {
            Files.walkFileTree(root, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        endMs = System.currentTimeMillis();
        return result;
    }
}
