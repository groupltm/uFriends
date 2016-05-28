package mywifip2pkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import mywifip2pkit.FileTransferService.FileTransferReceiveDataListener;

/**
 * Created by hungmai on 07/04/2016.
 */
public class ReceiveSocketAsync implements Runnable, FileTransferReceiveDataListener{

    static final int PORT = 9000;

    Socket mReceiveSocket;

    public SocketReceiverDataListener mReceiveListener;

    public onReceivedDataListener mReceivedDataListener;
    
    public OnStreamListener mStreamListener;

    int mPeer;

    Thread t;

    public ReceiveSocketAsync(onReceivedDataListener receivedDataListener, SocketReceiverDataListener receiveListener, Socket receiveSocket, int peer) {
        // TODO Auto-generated constructor stub
        mReceivedDataListener = receivedDataListener;
        mReceiveSocket = receiveSocket;
        mReceiveListener = receiveListener;
        mPeer = peer;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            InputStream receiveInputStream = mReceiveSocket.getInputStream();

            while (true){
                if (mReceiveSocket.isClosed()){
                    break;
                }

                //ByteArrayOutputStream os = new ByteArrayOutputStream();

                int result = FileTransferService.receiveFile(receiveInputStream, this);
//                if (result == 1){
//                    mReceiveListener.onCompleteSendData();
//                }else if (result == 0){
//                    os.flush();
//
//                    if (os.size() > 0){
//                        if (mReceiveListener != null){
//                            mReceiveListener.onReceiveMessageData(os.toByteArray());
//
//                            mReceivedDataListener.onCompleteReceivedData(mPeer);
//                        }
//                    }
//                }else if (result == 2){
//                    mReceivedDataListener.onPing(mPeer);
//                }else if (result == 3){
//                    mReceivedDataListener.onPingOK(mPeer);
//                }else if (result == 4){
//                	os.flush();
//
//                    if (os.size() > 0){
//                        if (mReceiveListener != null){
//                            mReceiveListener.onReceiveImageData(os.toByteArray());
//
//                            mReceivedDataListener.onCompleteReceivedData(mPeer);
//                        }
//                    }
//                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void start(){
        t = new Thread(this);
        t.start();
    }

    public void stop(){

    }

	@Override
	public void onReceiveCode() {
		// TODO Auto-generated method stub
		mReceiveListener.onCompleteSendData();
	}

	@Override
	public void onReceivePing() {
		// TODO Auto-generated method stub
		mReceivedDataListener.onPing(mPeer);
	}

	@Override
	public void onReceivePingOK() {
		// TODO Auto-generated method stub
		mReceivedDataListener.onPingOK(mPeer);
	}

	@Override
	public void onReceiveMessage(ByteArrayOutputStream os) {
		// TODO Auto-generated method stub
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (os.size() > 0){
            if (mReceiveListener != null){
                mReceiveListener.onReceiveMessageData(os.toByteArray());

                mReceivedDataListener.onCompleteReceivedData(mPeer);
            }
        }
	}

	@Override
	public void onReceiveImage(ByteArrayOutputStream os) {
		// TODO Auto-generated method stub
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (os.size() > 0){
            if (mReceiveListener != null){
                mReceiveListener.onReceiveImageData(os.toByteArray());

                mReceivedDataListener.onCompleteReceivedData(mPeer);
            }
        }
	}

	@Override
	public void onReceiveRequestStreamCode() {
		// TODO Auto-generated method stub
		mStreamListener.onReceiveRequestStream();
	}
	
	@Override
	public void onReceiveAllowStreamCode() {
		// TODO Auto-generated method stub
		mStreamListener.onReceiveAllowStream();
	}

	@Override
	public void onReceiveDisallowStreamCode() {
		// TODO Auto-generated method stub
		mStreamListener.onReceiveDisallowStream();
	}
	
	@Override
	public void onAlreadyStream(ByteArrayOutputStream os) {
		// TODO Auto-generated method stub
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (os.size() > 0){
            if (mReceiveListener != null){
            	String streamerIP = new String(os.toByteArray(), StandardCharsets.UTF_8);
            	
                mStreamListener.onAlreadyStream(streamerIP);

                mReceivedDataListener.onCompleteReceivedData(mPeer);
            }
        }
	}
	
	@Override
	public void onStopStream() {
		// TODO Auto-generated method stub
		mStreamListener.onStopStream();
	}
	
	public interface SocketReceiverDataListener{
        public void onReceiveMessageData(byte[] data);
        public void onReceiveImageData(byte[] data);
        public void onCompleteSendData();
        
        
    }
	
	public interface OnStreamListener{
		public void onReceiveRequestStream();
        public void onReceiveAllowStream();
        public void onReceiveDisallowStream();
        public void onAlreadyStream(String streamerIP);
        public void onStopStream();
	}

    public interface onReceivedDataListener{
        public void onCompleteReceivedData(int peer);
        public void onPing(int peer);
        public void onPingOK(int peer);
    }
}