package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Producer05_header {

    //队列名称
    //邮件队列
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    //SMS短信队列
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";

    //交换机名称
    //交换机
    private static final String EXCHANGE_HEADERS_INFORM="exchange_headers_inform";


    public static void main(String[] args) throws IOException, TimeoutException {

        Map<String, Object> headers_email = new Hashtable<String, Object>();
        headers_email.put("inform_type","email");
        Map<String, Object> headers_sms = new Hashtable<String, Object>();
        headers_sms.put("inform_type","sms");

        Connection connection = null;
        Channel channel = null;
        try {
            //创建一个与MQ的连接
            //通过连接工厂创建新的连接和mq建立连接
            ConnectionFactory factory = new ConnectionFactory();
            factory.setVirtualHost("/");
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");

            //创建一个连接
            connection = factory.newConnection();
            //创建与交换机的通道，每个通道代表一个会话
            channel = connection.createChannel();

            //声明交换机 
            //参数: String exchange, BuiltinExchangeType type
            /**              
             * 参数明细              
             * 1、交换机名称              
             * 2、交换机类型，fanout、topic、direct、headers              
             */
            channel.exchangeDeclare(EXCHANGE_HEADERS_INFORM, BuiltinExchangeType.HEADERS);

            //声明队列
            //参数: (String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String,  Object> arguments)
            /**
             * 参数明细:
             *  queue: 1、队列名称
             *  durable: 2、是否持久化
             *  exclusive: 3、是否独占此队列
             *  autoDelete: 4、队列不用是否自动删除
             *  arguments:  5、队列参数
             */
            channel.queueDeclare(QUEUE_INFORM_EMAIL,true,false,false,null);
            channel.queueDeclare(QUEUE_INFORM_SMS,true,false,false,null);

            //交换机和队列绑定
            //参数: String queue, String exchange, String routingKey
            /**
             * 参数明细
             *  queue: 1、队列名称
             *  exchange: 2、交换机名称
             *  routingKey: 3、路由key
             */
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_HEADERS_INFORM,"",headers_email);
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_HEADERS_INFORM,"",headers_sms);

            //发送消息
            for (int i = 0; i < 5; i++) {
                String message = "send email inform message to user" + i;
                Map<String,Object> headers = new Hashtable<String, Object>();
                headers.put("inform_type", "email");//匹配email通知消费者绑定的header
                AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder();
                properties.headers(headers);
                //向交换机发送消息
                //参数: String exchange, String routingKey, BasicProperties props, byte[] body
                /**
                 * 参数明细:
                 *  exchange: 1、交换机名称，不指定使用默认交换机名称 Default Exchange
                 *  routingKey: 2、routingKey（路由key），根据key名称将消息转发到具体的队列，这里填写队列名称表示消 息将发到此队列
                 *  props: 3、消息属性
                 *  body: 4、消息内容
                 */
                channel.basicPublish(EXCHANGE_HEADERS_INFORM,"",properties.build(),message.getBytes());
                System.out.println("send to mq "+message);
            }

            //发送消息
            for (int i = 0; i < 5; i++) {
                String message = "send sms inform message to user" + i;
                Map<String,Object> headers = new Hashtable<String, Object>();
                headers.put("inform_type", "sms");//匹配sms通知消费者绑定的header
                AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties.Builder();
                properties.headers(headers);
                //向交换机发送消息
                //参数: String exchange, String routingKey, BasicProperties props, byte[] body
                /**
                 * 参数明细:
                 *  exchange: 1、交换机名称，不指定使用默认交换机名称 Default Exchange
                 *  routingKey: 2、routingKey（路由key），根据key名称将消息转发到具体的队列，这里填写队列名称表示消 息将发到此队列
                 *  props: 3、消息属性
                 *  body: 4、消息内容
                 */
                channel.basicPublish(EXCHANGE_HEADERS_INFORM,"",properties.build(),message.getBytes());
                System.out.println("send to mq "+message);
            }

//            for (int i = 0; i < 5; i++) {
//                String message = "send sms and email inform message to user"+i;
//                //向交换机发送消息
//                //参数: String exchange, String routingKey, BasicProperties props, byte[] body
//                /**
//                 * 参数明细:
//                 *  exchange: 1、交换机名称，不指定使用默认交换机名称 Default Exchange
//                 *  routingKey: 2、routingKey（路由key），根据key名称将消息转发到具体的队列，这里填写队列名称表示消 息将发到此队列
//                 *  props: 3、消息属性
//                 *  body: 4、消息内容
//                 */
//                channel.basicPublish(EXCHANGE_TOPICS_INFORM,"inform.sms.email",null,message.getBytes());
//                System.out.println("send to mq "+message);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(channel!=null){
                channel.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
    }
}
