package com.nhga.wifimac;

import com.nhga.wifimac.bean.WifiMac;
import com.nhga.wifimac.mapper.MacMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Recv {
    private static final int DEFAULT_PORT = 6000;
    private static final int MAX_MSG_LEN = 1600;

    public static void main(String[] args) {
        try {
            DatagramSocket udp = new DatagramSocket(DEFAULT_PORT);
            DatagramPacket dPacket;
            byte[] echo = new byte[1];
            echo[0] = (byte) 1;
            while (true) {
                dPacket = new DatagramPacket(new byte[MAX_MSG_LEN], MAX_MSG_LEN);
                udp.receive(dPacket);
                String result = new String(dPacket.getData(), 0, dPacket.getLength());
                System.out.println(result + "\n\n");

                String res[] = result.split("\n");
                WifiMac wifiMac = new WifiMac();
                wifiMac.setCMAC(res[0]);//采集器的MAC地址

                for (int i = 1; i < res.length; i++) {
                    String arr[] = res[i].split("\\|");
                    wifiMac.setSMAC(arr[0]);//源MAC
                    wifiMac.setDMAC(arr[1]);//目的MAC
                    wifiMac.setZZLX(arr[2]);//帧主类型
                    wifiMac.setFZLX(arr[3]);//帧子类型
                    wifiMac.setXD(arr[4]);//信道
                    wifiMac.setXXQD(arr[5]);//信号强度
                    wifiMac.setRDMC(arr[6]);//热点名称
                    wifiMac.setZDSFXM(arr[7]);//终端是否休眠
                    wifiMac.setISLYQ(arr[8]);//源MAC是否为路由器

                    addMac(wifiMac);
                }
                //返回一个字节给探针设备
                InetAddress addr = dPacket.getAddress();
                dPacket = new DatagramPacket(echo, echo.length);
                dPacket.setAddress(addr);
                udp.send(dPacket);
            }
        } catch (SocketException e) {
            System.out.println("1.debug!" + "\n\n");
            //e.printStackTrace();
        } catch (IOException e) {
            System.out.println("2.debug!" + "\n\n");
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("3.debug!" + "\n\n");
            //e.printStackTrace();
        }
    }

    private static void addMac(WifiMac wifiMac) {
        try {
            // 读取配置文件
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);

            // 通过SqlSessionFactoryBuilder构建一个SqlSessionFactory
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            //打开SqlSession,true表示自动提交事务
            SqlSession sqlSession = sqlSessionFactory.openSession(true);

            //执行查询
            //获取动态创建的UserDao的实现(映射器)
            MacMapper mapper = sqlSession.getMapper(MacMapper.class);
            mapper.addMac(wifiMac);

        } catch (Exception ex) {
            System.out.println("4.debug!" + "\n\n");
            //System.out.println(ex);
        }
    }
}
