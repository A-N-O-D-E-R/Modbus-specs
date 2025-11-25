package com.anode.modbus.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Connection pool for managing multiple Modbus connections.
 * Provides connection pooling with configurable size, timeouts, and validation.
 *
 * <p><b>Thread-Safety:</b> This class is thread-safe and can be used concurrently
 * by multiple threads.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ModbusConnectionPool pool = ModbusConnectionPool.builder()
 *     .connectionFactory(() -> {
 *         ModbusTcpConnection conn = new ModbusTcpConnection("192.168.1.100", 502);
 *         conn.connect();
 *         return conn;
 *     })
 *     .poolSize(5)
 *     .build();
 *
 * try (PooledConnection conn = pool.borrowConnection()) {
 *     // Use connection
 *     conn.get().readHoldingRegisters(1, 0, 10);
 * } // Connection automatically returned to pool
 *
 * pool.close();
 * }</pre>
 */
public class ModbusConnectionPool implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ModbusConnectionPool.class);

    private final BlockingQueue<ModbusConnection> pool;
    private final ConnectionFactory connectionFactory;
    private final int poolSize;
    private final long borrowTimeoutMs;
    private final boolean validateOnBorrow;
    private volatile boolean closed = false;

    private ModbusConnectionPool(Builder builder) {
        this.poolSize = builder.poolSize;
        this.connectionFactory = builder.connectionFactory;
        this.borrowTimeoutMs = builder.borrowTimeoutMs;
        this.validateOnBorrow = builder.validateOnBorrow;
        this.pool = new ArrayBlockingQueue<>(poolSize);

        // Pre-populate pool
        try {
            for (int i = 0; i < poolSize; i++) {
                pool.offer(createConnection());
            }
            logger.info("Connection pool initialized with {} connections", poolSize);
        } catch (Exception e) {
            logger.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    /**
     * Borrows a connection from the pool.
     *
     * @return A pooled connection wrapper
     * @throws IllegalStateException if pool is closed
     * @throws RuntimeException if timeout or connection creation fails
     */
    public PooledConnection borrowConnection() {
        if (closed) {
            throw new IllegalStateException("Connection pool is closed");
        }

        try {
            ModbusConnection connection = pool.poll(borrowTimeoutMs, TimeUnit.MILLISECONDS);
            if (connection == null) {
                logger.warn("Timeout waiting for connection from pool");
                throw new RuntimeException("Timeout waiting for connection");
            }

            if (validateOnBorrow && !connection.isConnected()) {
                logger.warn("Connection invalid, creating new one");
                connection.close();
                connection = createConnection();
            }

            logger.debug("Connection borrowed from pool");
            return new PooledConnection(connection, this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for connection", e);
        }
    }

    /**
     * Returns a connection to the pool.
     *
     * @param connection The connection to return
     */
    void returnConnection(ModbusConnection connection) {
        if (closed) {
            connection.close();
            return;
        }

        if (!pool.offer(connection)) {
            logger.warn("Pool full, closing excess connection");
            connection.close();
        } else {
            logger.debug("Connection returned to pool");
        }
    }

    private ModbusConnection createConnection() {
        try {
            return connectionFactory.create();
        } catch (Exception e) {
            logger.error("Failed to create connection", e);
            throw new RuntimeException("Failed to create connection", e);
        }
    }

    /**
     * Closes the pool and all connections.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;
        logger.info("Closing connection pool");

        ModbusConnection connection;
        while ((connection = pool.poll()) != null) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.warn("Error closing connection", e);
            }
        }

        logger.info("Connection pool closed");
    }

    /**
     * Returns the pool size.
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Returns the number of available connections in the pool.
     */
    public int availableConnections() {
        return pool.size();
    }

    /**
     * Checks if the pool is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Factory interface for creating connections.
     */
    @FunctionalInterface
    public interface ConnectionFactory {
        ModbusConnection create() throws Exception;
    }

    /**
     * Wrapper for a pooled connection that returns it to the pool on close.
     */
    public static class PooledConnection implements AutoCloseable {
        private final ModbusConnection connection;
        private final ModbusConnectionPool pool;
        private boolean returned = false;

        private PooledConnection(ModbusConnection connection, ModbusConnectionPool pool) {
            this.connection = connection;
            this.pool = pool;
        }

        /**
         * Gets the underlying connection.
         */
        public ModbusConnection get() {
            if (returned) {
                throw new IllegalStateException("Connection already returned to pool");
            }
            return connection;
        }

        /**
         * Returns the connection to the pool.
         */
        @Override
        public void close() {
            if (!returned) {
                pool.returnConnection(connection);
                returned = true;
            }
        }
    }

    /**
     * Builder for creating connection pools.
     */
    public static class Builder {
        private ConnectionFactory connectionFactory;
        private int poolSize = 5;
        private long borrowTimeoutMs = 5000;
        private boolean validateOnBorrow = true;

        /**
         * Sets the connection factory.
         */
        public Builder connectionFactory(ConnectionFactory factory) {
            this.connectionFactory = factory;
            return this;
        }

        /**
         * Sets the pool size (default: 5).
         */
        public Builder poolSize(int size) {
            if (size < 1) {
                throw new IllegalArgumentException("Pool size must be at least 1");
            }
            this.poolSize = size;
            return this;
        }

        /**
         * Sets the borrow timeout in milliseconds (default: 5000).
         */
        public Builder borrowTimeout(long timeoutMs) {
            if (timeoutMs < 1) {
                throw new IllegalArgumentException("Timeout must be at least 1ms");
            }
            this.borrowTimeoutMs = timeoutMs;
            return this;
        }

        /**
         * Sets whether to validate connections on borrow (default: true).
         */
        public Builder validateOnBorrow(boolean validate) {
            this.validateOnBorrow = validate;
            return this;
        }

        /**
         * Builds the connection pool.
         */
        public ModbusConnectionPool build() {
            if (connectionFactory == null) {
                throw new IllegalStateException("Connection factory must be set");
            }
            return new ModbusConnectionPool(this);
        }
    }

    /**
     * Creates a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }
}
