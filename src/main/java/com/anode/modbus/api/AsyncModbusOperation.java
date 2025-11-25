package com.anode.modbus.api;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.FunctionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Async/reactive API for executing Modbus operations using CompletableFuture.
 * All operations are executed asynchronously and return CompletableFuture for
 * non-blocking operation.
 *
 * <p><b>Thread-Safety:</b> This class is thread-safe.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * AsyncModbusOperation op = new AsyncModbusOperation(functionCode, connectionSupplier);
 *
 * // Async read
 * op.unitId(1).address(0).quantity(10)
 *   .readAsync()
 *   .thenAccept(values -> System.out.println("Read: " + Arrays.toString(values)))
 *   .exceptionally(ex -> {
 *       System.err.println("Error: " + ex.getMessage());
 *       return null;
 *   });
 *
 * // Async write
 * op.unitId(1).address(0)
 *   .writeAsync(1234)
 *   .thenRun(() -> System.out.println("Write completed"));
 * }</pre>
 */
public class AsyncModbusOperation {

    private static final Logger logger = LoggerFactory.getLogger(AsyncModbusOperation.class);

    private final ModbusOperation syncOperation;
    private final Executor executor;

    /**
     * Creates an async operation with the default executor (ForkJoinPool.commonPool()).
     */
    public AsyncModbusOperation(FunctionCode functionCode, Supplier<ModbusConnection> connectionSupplier) {
        this(functionCode, connectionSupplier, ForkJoinPool.commonPool());
    }

    /**
     * Creates an async operation with a custom executor.
     */
    public AsyncModbusOperation(FunctionCode functionCode, Supplier<ModbusConnection> connectionSupplier, Executor executor) {
        this.syncOperation = new ModbusOperation(functionCode, connectionSupplier);
        this.executor = executor;
    }

    /**
     * Sets the unit/slave ID for the operation.
     */
    public AsyncModbusOperation unitId(int unitId) {
        syncOperation.unitId(unitId);
        return this;
    }

    /**
     * Sets the starting address for the operation.
     */
    public AsyncModbusOperation address(int address) {
        syncOperation.address(address);
        return this;
    }

    /**
     * Sets the quantity of registers/coils to read.
     */
    public AsyncModbusOperation quantity(int quantity) {
        syncOperation.quantity(quantity);
        return this;
    }

    /**
     * Gets the function code for this operation.
     */
    public FunctionCode getFunctionCode() {
        return syncOperation.getFunctionCode();
    }

    // ==================== Async Read Operations ====================

    /**
     * Executes a read operation asynchronously.
     *
     * @return CompletableFuture containing the read values
     */
    public CompletableFuture<int[]> readAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing async read operation");
                return syncOperation.read();
            } catch (ModbusConnectionException e) {
                logger.error("Async read failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Reads a single value asynchronously.
     *
     * @return CompletableFuture containing the single value
     */
    public CompletableFuture<Integer> readSingleAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing async read single operation");
                return syncOperation.readSingle();
            } catch (ModbusConnectionException e) {
                logger.error("Async read single failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Reads coils as boolean array asynchronously.
     *
     * @return CompletableFuture containing boolean values
     */
    public CompletableFuture<boolean[]> readBooleansAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing async read booleans operation");
                return syncOperation.readBooleans();
            } catch (ModbusConnectionException e) {
                logger.error("Async read booleans failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    // ==================== Async Write Operations ====================

    /**
     * Writes a single value asynchronously.
     *
     * @param value The value to write
     * @return CompletableFuture that completes when write is done
     */
    public CompletableFuture<Void> writeAsync(int value) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing async write operation: value={}", value);
                syncOperation.write(value);
            } catch (ModbusConnectionException e) {
                logger.error("Async write failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Writes a boolean value asynchronously.
     *
     * @param value The boolean value to write
     * @return CompletableFuture that completes when write is done
     */
    public CompletableFuture<Void> writeAsync(boolean value) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing async write operation: value={}", value);
                syncOperation.write(value);
            } catch (ModbusConnectionException e) {
                logger.error("Async write failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Writes multiple values asynchronously.
     *
     * @param values The values to write
     * @return CompletableFuture that completes when write is done
     */
    public CompletableFuture<Void> writeAsync(int[] values) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing async write multiple operation: count={}", values.length);
                syncOperation.write(values);
            } catch (ModbusConnectionException e) {
                logger.error("Async write multiple failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Writes multiple boolean values asynchronously.
     *
     * @param values The boolean values to write
     * @return CompletableFuture that completes when write is done
     */
    public CompletableFuture<Void> writeAsync(boolean[] values) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing async write multiple booleans operation: count={}", values.length);
                syncOperation.write(values);
            } catch (ModbusConnectionException e) {
                logger.error("Async write multiple booleans failed", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
