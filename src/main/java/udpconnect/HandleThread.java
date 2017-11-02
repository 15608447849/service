package udpconnect;



import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Path;

/**
 * Created by user on 2017/6/19.
 */
public class HandleThread extends Thread{
    private DatagramChannel channel;
    private SocketAddress toClient;
    private ByteBuffer buffer;
    public HandleThread(DatagramChannel channel, SocketAddress socketAddress, ByteBuffer buffer) {
        this.channel = channel;
        this.toClient = socketAddress;
        this.buffer = buffer;
        this.start();
    }


    @Override
    public void run() {
        if (!buffer.hasRemaining()) return;
        byte command = -1;
        String result = "command is mismatching.";

        byte[] data = null;
        byte[] fileMD5 = null;
        long fileLength = 0;

        byte recCmd  = buffer.get();
        if (recCmd == 1){
            //查询文件信息
            data = new byte[buffer.remaining()];
            buffer.get(data);

            try {
                String filePath = new String(data,"UTF-8");
                File file = new FileQuery(filePath).queryFile().toFile();
                fileMD5 = MD5Util.getFileMD5Bytes(file);
                command = 20;
                fileLength = file.length();
                Log.i(toClient +" 查询文件 - "+ result+" ,已返回MD5:"+ MD5Util.getMD5String(filePath)+" ,文件大小:"+ fileLength+" byte");

            } catch (UnsupportedEncodingException e) {
                result = "file path error: ["+ e.getMessage()+"]";
            } catch (IOException e) {
                result = "file not fount: ["+ e.getMessage()+"]";
            }
        }


        buffer.clear();
        buffer.put(command);
        if (command == -1){
                buffer.put(result.getBytes());
        }
        if (command == 20){
            //返回文件信息
            buffer.put(fileMD5);
            buffer.putLong(fileLength);
            buffer.put(data);
        }
        buffer.flip();
        if (channel.isOpen()){
            try {
                channel.send(buffer,toClient);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
