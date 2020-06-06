package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producter01 {

    //队列名称
    private static final String QUEUE = "helloworld";

    /**
     * 1）创建连接
     * 2）创建通道
     * 3）声明队列
     * 4）发送消息
     * @param args
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = null;
        Connection connection = null;
        Channel channel = null;
        try {
            factory = new ConnectionFactory();
            //设置主机
            factory.setHost("localhost");
            //设置端口
            factory.setPort(5672);
            //设置用户名
            factory.setUsername("guest");
            //设置密码
            factory.setPassword("guest");
            //rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器
            factory.setVirtualHost("/");

            //第一大步: 1、生产者和Broker建立TCP连接
            //创建与RabbitMQ服务的TCP连接
            connection = factory.newConnection();

            //第二大步: 2、生产者和Broker建立通道
            //创建与Exchange的通道,每个连接可以创建多个通道,每个通道代表一个会话任务
            channel = connection.createChannel();

            //参数: String queue,boolean durable,boolean exclusive,boolean autoDelete,Map<String, Object> arguments
            /**
             * 参数说明:
             *  String queue: 队列名称
             *  boolean durable: 是否持久化
             *  boolean exclusive: 队列是否独占此连接
             *  boolean autoDelete: 队列不再使用时是否自动删除此队列
             *  Map<String, Object> arguments: 队列参数
             */
            //声明队列,如果Rabbit中没有此队列将自动创建
            channel.queueDeclare(QUEUE,true,false,false,null);
            String message = "HelloWprld小米"+System.currentTimeMillis();

            //参数列表: String exchange, String routingKey, BasicProperties props, byte[] body
            /**
             * 参数说明:
             *   String exchange: Exchange的名称，如果没有指定，则使用Default Exchange
             *   String routingKey: routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指定的消息队列
             *   BasicProperties props: 消息包含的属性
             *   byte[] body: 消息体
             */
            /**
             * 这里没有指定交换机，消息将发送给默认交换机，每个队列也会绑定那个默认的交换机，但是不能显 示绑定或解除绑定
             * 默认的交换机，routingKey等于队列名称
             */
            //第三大步: 3、生产者通过通道消息发送给Broker，由Exchange将消息进行转发。
            channel.basicPublish("",QUEUE,null,message.getBytes());

            System.out.println("Send Message is:'" +message+ "'");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(channel != null){
                channel.close();
            }
            if(connection!=null){
               connection.close();
            }
        }
    }
}
