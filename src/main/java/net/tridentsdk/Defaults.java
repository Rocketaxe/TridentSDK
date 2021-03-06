/*
 * Trident - A Multithreaded Server Alternative
 * Copyright 2014 The TridentSDK Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.tridentsdk;

// if these shouldn't exist, or should go somewhere else, just move them
// this is probably temporary

import net.tridentsdk.concurrent.TaskExecutor;
import net.tridentsdk.util.TridentLogger;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

/**
 * Contains the default values used in server.json
 *
 * @author The TridentSDK Team
 */
@ThreadSafe
public final class Defaults {
    /**
     * Maximum allowed players on the server
     */
    public static final int MAX_PLAYERS = 10;
    /**
     * The interval which to clean out chunks
     */
    public static final int CHUNK_CLEAN_TICK_INTERVAL = 500;
    /**
     * The text displayed below the server name in the multiplayer menu
     */
    public static final String MOTD = "Just another Trident server...";
    /**
     * The difficulty of the game
     */
    public static final Difficulty DIFFICULTY = Difficulty.EASY;
    /**
     * The icon on the left of the server
     */
    public static final String MOTD_IMAGE_LOCATION = "/server-icon.png";
    /**
     * The threshold used for compression
     */
    public static final int COMPRESSION_THRESHOLD = 256;
    /**
     * The server port
     */
    public static final int PORT = 25565;
    /**
     * The default address for the server
     */
    public static final String ADDRESS = "localhost";
    /**
     * The default view distance of the server
     */
    public static final int VIEW_DISTANCE = 15;

    public static final boolean IMAGE_CHANGING_ALLOWED = false;

    public static final byte GAME_MODE = GameMode.SURVIVAL.asByte();

    /**
     * The server's default exception handler
     */
    public static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (thread, throwable) -> TridentLogger.error(throwable);

    /**
     * The thread factory which makes a thread that handles exceptions
     */
    public static final ThreadFactory ERROR_HANDLED = runnable -> new Thread(() -> {
        try {
            runnable.run();
        } catch (Exception e) {
            TridentLogger.error(e);
        }
    });

    /**
     * Executes tasks on the same thread that inserts it
     */
    public static final TaskExecutor DIRECT_EXECUTOR = new TaskExecutor() {
        @Override
        public void addTask(Runnable task) {
            task.run();
        }

        @Override
        public <V> Future<V> submitTask(Callable<V> task) {
            RunnableFuture<V> future = new FutureTask<>(task);
            try {
                future.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return future;
        }

        @Override
        public void interrupt() {
        }

        @Override
        public Thread asThread() {
            return Thread.currentThread();
        }
    };

    private Defaults() {
    }
}
