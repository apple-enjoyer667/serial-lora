package com.mtgprod.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * ErrorLogger
 *
 * Keeps a rolling window of error logs (default: 7 days).
 * Designed to run on Raspberry Pi 3/4 with minimal dependencies.
 */
public class ErrorLogger implements AutoCloseable {

    // Définition du temps de conservation des logs et l'emplacement de la SQlite
    private static final long DEFAULT_RETENTION_DAYS = 7;
    private static final String DEFAULT_DB_PATH = "logs/lorawan_errors.db";

    private final Connection connection;
    private final long retentionDays;

    public ErrorLogger() throws SQLException {
        this(DEFAULT_DB_PATH, DEFAULT_RETENTION_DAYS);
    }

    // Constructeur alternatif qui permet de définir un chemin différent pour le fichier de la db
    public ErrorLogger(String dbPath) throws SQLException {
        this(dbPath, DEFAULT_RETENTION_DAYS);
    }

    public ErrorLogger(int retentionDays) throws SQLException {
        this(DEFAULT_DB_PATH, retentionDays);
    }

    // vrai constructeur (pas mal la cascade?!)
    public ErrorLogger(String dbPath, long retentionDays) throws SQLException {
        this.retentionDays = retentionDays;

        // Create parent directories if they don't exist
        java.io.File dbFile = new java.io.File(dbPath);
        if (dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }

        // Connect (creates the file automatically if missing)
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        // Performance tuning for Raspberry Pi (WAL mode = faster writes)
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA synchronous=NORMAL");
        }

        initTable();
    }

    /**
     * Permet d'entrer une exception avec son timestamp et toute la stack trace
     * Elle vérifie que la fenêtre de temps pour les logs soit respectée en passant
     *
     * @param e on passe directement l'object de l'exception
     */
    public synchronized void log(Exception e) {
        log(stackTraceToString(e));
    }

    /**
     * Permet d'entrer un log d'erreur personalisé
     * Vérification de la fenêtre de sauvegarde également
     *
     * @param errorMessage the error text to store
     */
    public synchronized void log(String errorMessage) {
        purgeOldEntries();
        insertEntry(Instant.now().toEpochMilli(), errorMessage);
    }

    /**
     * Récupères toutes les erreurs dans la SQLite
     * du plus ancien au plus récent
     */
    public synchronized ResultSet queryAll() throws SQLException {
        String sql = "SELECT id, date, error FROM logs ORDER BY date ASC";
        return connection.createStatement().executeQuery(sql);
    }

    /**
     * Récupères les entrées des dérnières N heures.
     *
     * @param hours nombre d'heures
     */
    public synchronized ResultSet queryLastHours(int hours) throws SQLException {
        long cutoff = Instant.now().minus(hours, ChronoUnit.HOURS).toEpochMilli();
        String sql = "SELECT id, date, error FROM logs WHERE date >= ? ORDER BY date ASC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, cutoff);
        return ps.executeQuery();
    }

    /**
     * Ferme la connection avec la SQLite
     */
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Creates the logs table if it doesn't already exist.
     * Safe to call multiple times (idempotent).
     */
    private void initTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS logs (
                    id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    date  INTEGER NOT NULL,   -- Unix timestamp in milliseconds
                    error TEXT    NOT NULL    -- Full exception stack trace or message
                );
                """;
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    /**
     * Inserts a single log entry.
     */
    private void insertEntry(long timestampMs, String errorMessage) {
        String sql = "INSERT INTO logs (date, error) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, timestampMs);
            ps.setString(2, errorMessage);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Last resort: print to stderr so the driver itself doesn't crash
            System.err.println("[ErrorLogger] Failed to insert log entry: " + e.getMessage());
        }
    }

    /**
     * Deletes all entries older than the configured retention window.
     */
    private void purgeOldEntries() {
        long cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS).toEpochMilli();
        String sql = "DELETE FROM logs WHERE date < ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cutoff);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("[ErrorLogger] Purged " + deleted + " old log entries.");
            }
        } catch (SQLException e) {
            System.err.println("[ErrorLogger] Failed to purge old entries: " + e.getMessage());
        }
    }

    /**
     * Converts an exception's stack trace to a single String.
     */
    private static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // ── Quick usage demo ─────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {

        // Try-with-resources ensures the DB connection is always closed cleanly
        try (ErrorLogger logger = new ErrorLogger("logs/lorawan_errors.db", 7)) {

            // Log a real exception
            try {
                throw new RuntimeException("LoRaWAN join failed: no ACK from gateway");
            } catch (Exception e) {
                logger.log(e);
            }

            // Log a plain message
            logger.log("TX timeout on port 1, RSSI=-112 dBm");

            // Query and print all stored logs
            System.out.println("\n=== Stored logs ===");
            ResultSet rs = logger.queryAll();
            while (rs.next()) {
                long ts   = rs.getLong("date");
                String err = rs.getString("error");
                System.out.printf("[%s] %s%n", Instant.ofEpochMilli(ts), err);
            }
        }
    }
}
