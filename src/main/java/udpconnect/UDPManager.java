package udpconnect;



import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by user on 2017/6/19.
 */
public class UDPManager extends Thread{
    public static final int UDP_DATA_MIN_BUFFER_ZONE = 576-20-8;// intenet标准MTU - IP头 -UDP头
    private DatagramChannel channel;

    public UDPManager(String ipStr,int port) throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(ipStr,port));
        this.start();
    }

    @Override
    public void run() {
        SocketAddress socketAddress;
        ByteBuffer buffer = null;
        while (true){
            try {
                if (buffer==null){
                   buffer = ByteBuffer.allocate(UDP_DATA_MIN_BUFFER_ZONE);
                }
                buffer.clear();
                socketAddress = channel.receive(buffer);

                if (socketAddress!=null){
                    buffer.flip();
                    new HandleThread(channel,socketAddress,buffer);
                    buffer = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
