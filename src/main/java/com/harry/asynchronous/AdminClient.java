package com.harry.asynchronous;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Harry on 2017/11/12.
 */
public class AdminClient implements Watcher {
    private static final Logger LOG = Logger.getLogger(AdminClient.class);

    ZooKeeper zk;
    String hostPort;

    public AdminClient(String hostPort) {
        this.hostPort = hostPort;
    }

    void start() throws IOException {
        zk = new ZooKeeper(hostPort, 15000, this);
    }

    void listState() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte masterData[] = new byte[0];
        try {
            masterData = zk.getData("/master", false, stat);
            Date startDate = new Date(stat.getCtime());
            LOG.info("Master: " + new String(masterData) + "since " + startDate);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage() + "No Master.");
            e.printStackTrace();
        }

        LOG.info("Workers: ");
        for(String w : zk.getChildren("/workers", false)) {
            byte data[] = zk.getData("/workers/" + w, false, null);
            String state = new String(data);
            LOG.info("\t" + w + ": " + state);
        }

        LOG.info("Tasks: ");
        for(String t : zk.getChildren("/assign", false)) {
            LOG.info("\t" + t);
        }
    }
    public void process(WatchedEvent watchedEvent) {
        LOG.info(watchedEvent);
    }

    public static void main(String args[]) throws KeeperException, InterruptedException, IOException {
        AdminClient adminClient = new AdminClient(args[0]);
        adminClient.start();
        adminClient.listState();
    }
}
