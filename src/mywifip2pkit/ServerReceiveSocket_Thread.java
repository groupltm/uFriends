package mywifip2pkit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by hungmai on 07/04/2016.
 */
public class ServerReceiveSocket_Thread implements Runnable{

    public static final int PORT = 9000;

    boolean flag = false;
    Thread t;

    ServerSocket mServerSocket;

    ServerSendSocket_Thread.ServerSocketListener mListener;

    public ServerReceiveSocket_Thread(ServerSendSocket_Thread.ServerSocketListener svListener) throws IOException{
        mServerSocket = new ServerSocket(PORT);
        mListener = svListener;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Socket receiveSocket;
        while (!mServerSocket.isClosed()){
            try {
                receiveSocket = mServerSocket.accept();
                mListener.onReceive_ReceiveSocket(receiveSocket);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void start(){
        t = new Thread(this);
        t.start();
    }

    public void stop(){
        try {
            mServerSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

