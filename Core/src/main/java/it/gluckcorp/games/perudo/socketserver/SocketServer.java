package it.gluckcorp.games.perudo.socketserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer {
    private final SocketService service;
    private final static int MAX_PARALLEL_GAMES = 8;
    private final static int MAX_PARALLEL_PLAYER_ACCEPTANCE = 4;

    private boolean running;
    private ServerSocket serverSocket;
    private ExecutorService connectionExecutor, gameExecutor;

    public SocketServer(int port, SocketService service) throws Exception {
        this.service = service;
        serverSocket = new ServerSocket(port);

        connectionExecutor = Executors.newFixedThreadPool(2 + MAX_PARALLEL_PLAYER_ACCEPTANCE);
        gameExecutor = Executors.newFixedThreadPool(MAX_PARALLEL_GAMES);
    }

    public void start() {
        final Runnable connectionHandler = () -> {
            try {
                while (running) {
                    final Socket serviceSocket = serverSocket.accept();
                    connectionExecutor.execute(() -> service.connectPlayer(serviceSocket));
                }
            } catch (Exception e) {
                if (running)
                    e.printStackTrace();
            }
        };

        final Runnable multiTableHandler = () -> {
            while (running) {
                System.out.println("Preparing new Table");
                try {
                    gameExecutor.execute(service.prepareTable()::startGame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        running = true;
        connectionExecutor.execute(connectionHandler);

        try {
            connectionExecutor.submit(multiTableHandler).get();
            stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() throws Exception {
        running = false;

        shutdownAndAwaitTermination(connectionExecutor);
        shutdownAndAwaitTermination(gameExecutor);
        serverSocket.close();
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
