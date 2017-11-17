package com.harry.asynchronous;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Harry on 2017/11/12.
 */
public class Worker implements Watcher {
    private static final Logger LOG = Logger.getLogger(Worker.class);

    ZooKeeper zk;
    String hostProt;
    String serverId = Integer.toHexString(new Random().nextInt());
    private String status;

    public Worker(String hostProt) {
        this.hostProt = hostProt;
    }

    void startZK() throws IOException {
        zk = new ZooKeeper(hostProt, 15000, this);
    }

    public void process(WatchedEvent watchedEvent) {
        LOG.info(watchedEvent.toString() + ", " + hostProt);
    }

    void register () {
        zk.create("/workers/worker-" + serverId,
                "Idle".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL,
                createWorkerCallback,
                null
        );
    }

    AsyncCallback.StringCallback createWorkerCallback = new AsyncCallback.StringCallback() {
        public void processResult(int i, String s, Object o, String s1) {
            switch (KeeperException.Code.get(i)) {
                case CONNECTIONLOSS:
                    register();
                    break;
                case OK:
                    LOG.info("Registered successfully: " + serverId);
                    break;
                case NODEEXISTS:
                    LOG.warn("Already registered: " + serverId);
                    break;
                default:
                    LOG.error("Something went wrong: " + KeeperException.create(KeeperException.Code.get(i), s));
            }
        }
    };

    public static void main(String args[]) throws Exception {
        Worker w = new Worker(args[0]);
        w.startZK();

        w.register();

        Thread.sleep(30000);
    }

    AsyncCallback.StatCallback statusUpdateCallback = new AsyncCallback.StatCallback() {
        public void processResult(int i, String s, Object o, Stat stat) {
            switch (KeeperException.Code.get(i)) {
                case CONNECTIONLOSS:
                    updateStatus((String)o);
                    return;
            }
        }
    };

    private void updateStatus(String status) {
        if(status == this.status) {
            zk.setData("/workers/" + "name", status.getBytes(), -1, statusUpdateCallback, status);
        }
    }
    public void setStatus(String status) {
        this.status = status;
        updateStatus(status);
    }
}
