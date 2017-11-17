package com.harry.asynchronous;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Created by Harry on 2017/11/12.
 */
public class Client implements Watcher {
    private static final Logger LOG = Logger.getLogger(Client.class);
    ZooKeeper zk;
    String hostPort;

    public Client(String hostPort) {
        this.hostPort = hostPort;
    }

    void startZK() throws IOException {
        zk = new ZooKeeper(hostPort, 15000, this);
    }

    String queueCommand(String command) throws KeeperException {
        while (true) {
            try {
                String name = zk.create("/tasks/task-",
                        command.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT_SEQUENTIAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(WatchedEvent watchedEvent) {
        LOG.info(watchedEvent.toString());
    }

    public static void main(String args[]) throws IOException, KeeperException {
        Client client = new Client(args[0]);

        client.startZK();

        String name = client.queueCommand(args[1]);

        LOG.info("Created " + name);
    }
}