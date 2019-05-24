package cn.simple;

import cn.AbstractLock;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

public class ZookeeperLock extends AbstractLock {

    private static String ZK_SERVER = "192.168.31.237:2181";
    private static int SESSION_TIMEOUT = 5000;
    private static String LOCK_PATH = "/lock";
    private CountDownLatch countDownLatch;

    private static ZkClient zkClient = new ZkClient(ZK_SERVER,SESSION_TIMEOUT);

    @Override
    protected boolean tryLock() {
        try {
            zkClient.createEphemeral(LOCK_PATH);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public void waitLock() {

        IZkDataListener dataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                if(countDownLatch != null){
                    countDownLatch.countDown();
                }
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }
        };

        zkClient.subscribeDataChanges(LOCK_PATH, dataListener);

        if(zkClient.exists(LOCK_PATH)){
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        zkClient.unsubscribeDataChanges(LOCK_PATH, dataListener);

    }

    public void unLock() {
        //释放锁
        if (zkClient != null) {
            zkClient.delete(LOCK_PATH);
            zkClient.close();
            System.out.println("释放锁资源...");
        }
    }

}
