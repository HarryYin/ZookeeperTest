package com.harry;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;

/**
 * ͬ����ȡ����Ȩ��
 * Created by Harry on 2017/11/11.
 */
public class Master implements Watcher{

    ZooKeeper zk;
    String hostPort;
    String serverId = Integer.toHexString(new Random().nextInt());
    boolean isLeader = false;

    public Master(String hostPort) {
        this.hostPort = hostPort;
    }

    void startZK() {
        try {
            zk = new ZooKeeper(hostPort, 15000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent);
    }

    void runForMaster() throws InterruptedException {
        while (true){
            try {
                zk.create("/master", serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                isLeader = true;
                break;
            } catch (KeeperException e) {
                isLeader = false;
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(checkMaster()) {
                break;
            }
        }
    }

    // returns true if there is a master
    boolean checkMaster(){
        while (true) {
            try {
                Stat stat = new Stat();
                byte[] data = new byte[0];
                data = zk.getData("/master", false, stat);
                isLeader = new String(data).equals(serverId);
                return true;
            } catch (KeeperException e) {
                // no master, so try create again
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopZK() throws InterruptedException {
        zk.close();
    }

    public static void main(String args[]) throws InterruptedException {
        Master m = new Master(args[0]);
        m.startZK();

        // ��ǰ���̳�Ϊ���ڵ����һ�����̳�Ϊ���ڵ�󷵻�
        m.runForMaster();

        if(m.isLeader) {
            // �������ڵ��Ӧ���߼�ʱ���ڴ˴���ʼִ����Щ�߼���
            System.out.println("I'm the leader.");
            // wait for a bit
            Thread.sleep(60000);
        } else {
            System.out.println("Someone else is ther leader.");
        }

        m.stopZK();
    }

}
