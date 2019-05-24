package cn.enjoy.product.zk;

import org.I0Itec.zkclient.ZkClient;

/**
 * Created by VULCAN on 2018/7/28.
 */
public class ServiceRegister {

    private  static final String BASE_SERVICES = "/services";
    private static final String  SERVICE_NAME="/products";
    private static final String ZK_ADDRESS = "192.168.31.237:2181";
    private static final int SESSION_OUTTIME = 10000;//ms

    public static  void register(String address,int port) {
        try {

            ZkClient zkc = new ZkClient(ZK_ADDRESS, SESSION_OUTTIME);

            if(!zkc.exists(BASE_SERVICES + SERVICE_NAME)) {
                zkc.createPersistent(BASE_SERVICES + SERVICE_NAME,true);
            }
            String server_path = address+":"+port;
            zkc.createEphemeralSequential(BASE_SERVICES + SERVICE_NAME+"/child",server_path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
