package com.fittrack.async;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AsyncTaskRunner {

    private AsyncTaskRunner() {}

    public static <T> void run(
            Supplier<T> action,
            Consumer<T> onSuccess,
            Consumer<Throwable> onFailure
    ) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return action.get();
            }
        };

        task.setOnSucceeded(event ->
                onSuccess.accept(task.getValue())
        );

        task.setOnFailed(event ->
                onFailure.accept(task.getException())
        );

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}