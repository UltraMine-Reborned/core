package org.ultraminereborned.async;

import org.ultramine.server.util.GlobalExecutors;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Async {

    public static <T> CompletableFuture<T> async (Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    public static CompletableFuture<Void> async (Runnable runnable) {
        return CompletableFuture.runAsync(runnable);
    }

    public static CompletableFuture<Void> globallyAsync (Runnable runnable) {
        return CompletableFuture.runAsync(runnable, GlobalExecutors.cachedIO());
    }

    public static <T> T await(CompletableFuture<T> completableFuture) {
        return completableFuture.join();
    }
}
