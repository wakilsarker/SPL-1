package hospital.util;

import java.io.*;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

/** Small reusable CSV reader/writer. Values containing commas or quotes are supported. */
public final class CsvUtil {
    private static final Map<String, Object> LOCAL_LOCKS = new HashMap<>();
    private CsvUtil() { }

    public static List<String[]> read(String file) {
        ensureFile(file);
        return locked(file, () -> {
            List<String[]> rows = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) rows.add(parse(line));
                }
            } catch (IOException e) { System.out.println("Could not read " + file + ": " + e.getMessage()); }
            return rows;
        });
    }

    public static void append(String file, String[] row) {
        ensureFile(file);
        locked(file, () -> {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardOpenOption.APPEND)) {
                writer.write(join(row)); writer.newLine();
            } catch (IOException e) { System.out.println("Could not save data: " + e.getMessage()); }
            return null;
        });
    }

    public static void writeAll(String file, List<String[]> rows) {
        ensureFile(file);
        locked(file, () -> {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardOpenOption.TRUNCATE_EXISTING)) {
                for (String[] row : rows) { writer.write(join(row)); writer.newLine(); }
            } catch (IOException e) { System.out.println("Could not update data: " + e.getMessage()); }
            return null;
        });
    }

    /** Runs a multi-step operation while every cooperating process is excluded. */
    public static void withFileLock(String lockFile, Runnable operation) {
        ensureFile(lockFile);
        locked(lockFile, () -> {
            operation.run();
            return null;
        });
    }

    private static <T> T locked(String file, FileOperation<T> operation) {
        String key = Paths.get(file).toAbsolutePath().normalize().toString();
        Object localLock;
        synchronized (LOCAL_LOCKS) {
            localLock = LOCAL_LOCKS.computeIfAbsent(key, ignored -> new Object());
        }
        synchronized (localLock) {
            try (FileChannel channel = FileChannel.open(Paths.get(file), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                FileLock fileLock = channel.lock();
                try {
                    return operation.run();
                } finally {
                    fileLock.release();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot lock data file: " + file, e);
            }
        }
    }

    public static void ensureFile(String file) {
        try {
            Path path = Paths.get(file);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                try { Files.createFile(path); }
                catch (FileAlreadyExistsException ignored) { }
            }
        } catch (IOException e) { throw new IllegalStateException("Cannot create data file: " + file, e); }
    }

    private static String join(String[] values) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) text.append(',');
            String value = values[i] == null ? "" : values[i];
            text.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        return text.toString();
    }

    private static String[] parse(String line) {
        List<String> values = new ArrayList<>(); StringBuilder value = new StringBuilder(); boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append(c); i++; }
            else if (c == '"') quoted = !quoted;
            else if (c == ',' && !quoted) { values.add(value.toString()); value.setLength(0); }
            else value.append(c);
        }
        values.add(value.toString()); return values.toArray(new String[0]);
    }

    private interface FileOperation<T> { T run(); }
}
