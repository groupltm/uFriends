package mywifip2pkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by hungmai on 07/04/2016.
 */

/**
 * 
 * @author hungmai
 * Code:
 * 200: send message
 * 220: end message
 * 400: send image
 * 420: end image
 */
public class FileTransferService {

    public static boolean sendCode(final InputStream inputStream, final OutputStream out){
        int len = 0;
        byte buf[] = new byte [4];
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static boolean sendMessage(final InputStream inputStream, final OutputStream out) {
        byte buf[] = new byte[1024];
        int len;

        int i = 0;
        int j = 0;

        try {
            while ((len = inputStream.read(buf)) != -1) {
                byte[] tempBuf = Arrays.copyOfRange(buf, 0, len);
                byte[] newBuff;

                if (inputStream.available() == 0) {
                    newBuff = addMessageCode(tempBuf, true);
                    out.write(newBuff, 0, len + 6);
                } else {
                    newBuff = addMessageCode(tempBuf, false);
                    out.write(newBuff, 0, len + 4);
                }

                if (len != 1024) {
                    j++;
                }

                i += len;
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    public static boolean sendImage(final InputStream inputStream, final OutputStream out) {
        byte buf[] = new byte[1024];
        int len;

        int i = 0;
        int j = 0;

        try {
            while ((len = inputStream.read(buf)) != -1) {
                byte[] tempBuf = Arrays.copyOfRange(buf, 0, len);
                byte[] newBuff;

                if (inputStream.available() == 0) {
                    newBuff = addImageCode(tempBuf, true);
                    out.write(newBuff, 0, len + 6);
                } else {
                    newBuff = addImageCode(tempBuf, false);
                    out.write(newBuff, 0, len + 4);
                }

                if (len != 1024) {
                    j++;
                }

                i += len;
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static int receiveFile(final InputStream inputStream, FileTransferReceiveDataListener listener) {
        byte buf[] = new byte[1028];
        byte oldBuf[] = null;
        int len = 0;
        int oldLen = 0;
        int result = -1;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            while ((len = inputStream.read(buf)) != -1) {

            	
            	
                if (len < 1028 || (len == 1028 && oldLen > 0)) {
                    oldLen += len;
                    byte[] tempBuf = Arrays.copyOfRange(buf, 0, len);
                    if (oldBuf != null) {
                        oldBuf = combineTwoByteArray(oldBuf, tempBuf);
                    } else {
                        oldBuf = tempBuf;
                    }


                    if (oldLen > 1028) {
                        byte[] realBuf = getRealBuffer(oldBuf, 1028, false);
                        out.write(realBuf, 0, 1024);

                        oldBuf = Arrays.copyOfRange(oldBuf, 1028, oldLen);

                        oldLen -= 1028;
                    }

                    byte[] code = detectCode(oldBuf);

                    if (checkReceivedCode(code)){
                        //result = 1;
                    	listener.onReceiveCode();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        
                        //break;
                    }

                    if (checkPing(code)){
                        //result = 2;
                    	listener.onReceivePing();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        //break;
                    }

                    if (checkPingOK(code)){
                        //result = 3;
                    	listener.onReceivePingOK();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        //break;
                    }
                    
                    if (checkRequestStreamCode(code)){
                    	listener.onReceiveRequestStreamCode();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        //break;
                    }
                    
                    if (checkAllowStreamCode(code)){
                    	listener.onReceiveAllowStreamCode();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        //break;
                    }
                    
                    if (checkDisallowStreamCode(code)){
                    	listener.onReceiveDisallowStreamCode();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        //break;
                    }
                    
                    if (checkStopStreamCode(code)){
                    	listener.onStopStream();
                        if (oldLen > 4){
                        	oldBuf = Arrays.copyOfRange(oldBuf, 4, oldLen);
                            oldLen -= 4;
                        }else {
                        	oldBuf = null;
                        	oldLen = 0;
                        }
                        //break;
                    }

                    if (checkCode(code) || checkImageCode(code) || checkAlreadyStreamCode(code)) {
                        byte[] endFile = detectEndFile(oldBuf, oldLen);
                        int indexEndFile = checkEndFile(/*endFile*/ oldBuf);
                        if (indexEndFile != -1) {
                            byte[] realBuf = getRealBufferWithIndex(oldBuf, indexEndFile);
                            out.write(realBuf, 0, realBuf.length);
                            if (oldLen - indexEndFile > 2){
                            	oldBuf = Arrays.copyOfRange(oldBuf, indexEndFile + 2, oldLen);
                            	oldLen = oldLen - indexEndFile + 2;
                            }else {
                            	oldBuf = null;
                            	oldLen = 0;
                            }
                            
                            if (checkCode(code))
                            	//result = 0;
                            	listener.onReceiveMessage(out);
                            else if (checkImageCode(code))
                            	//result = 4;
                            	listener.onReceiveImage(out);
                            else if (checkAlreadyStreamCode(code)){
                            	listener.onAlreadyStream(out);
                            }
                            
                            out = new ByteArrayOutputStream();
                            
                            //break;
                        }
                    }
                } else {
                    byte[] code = detectCode(buf);
                    boolean isMessageEnd = checkCode(code);
                    boolean isImageEnd = checkImageCode(code);

                    byte[] realBuf;
                    
                    if (isMessageEnd || isImageEnd){
                    	realBuf = getRealBuffer(buf, len, true);
                    	if (realBuf != null) {
                            out.write(realBuf, 0, 1024);
                        }
                    	
                    	oldBuf = null;
                    	oldLen = 0;
                    	
                    	if (isMessageEnd){
                    		//result = 0;
                    		listener.onReceiveMessage(out);
                    	} else{
                    		//result = 4;
                    		listener.onReceiveImage(out);
                    	}
                    	
                    	out = new ByteArrayOutputStream();
                    	//break;
                    	
                    } else {
                    	realBuf = getRealBuffer(buf, len, false);
                    	if (realBuf != null) {
                            out.write(realBuf, 0, 1024);
                        }
                    }
                }
            }

        } catch (IOException e) {

        }
        return result;
    }

    private static byte[] addImageCode(byte[] buffer, boolean endFile) {
        byte[] code = new byte[4];
        byte[] codeEndFile = new byte[2];
        byte[] result;

        if (!endFile) {
            code[0] = '2';
            code[1] = '0';
            code[2] = '0';
            code[3] = ' ';

            result = combineTwoByteArray(code, buffer);
        } else {
            code[0] = '2';
            code[1] = '4';
            code[2] = '0';
            code[3] = ' ';

            codeEndFile[0] = '\r';
            codeEndFile[1] = '\n';

            result = combineTwoByteArray(code, buffer);
            result = combineTwoByteArray(result, codeEndFile);
        }

        return result;
    }
    
    private static byte[] addMessageCode(byte[] buffer, boolean endFile) {
        byte[] code = new byte[4];
        byte[] codeEndFile = new byte[2];
        byte[] result;

        if (!endFile) {
            code[0] = '2';
            code[1] = '0';
            code[2] = '0';
            code[3] = ' ';

            result = combineTwoByteArray(code, buffer);
        } else {
            code[0] = '2';
            code[1] = '2';
            code[2] = '0';
            code[3] = ' ';

            codeEndFile[0] = '\r';
            codeEndFile[1] = '\n';

            result = combineTwoByteArray(code, buffer);
            result = combineTwoByteArray(result, codeEndFile);
        }

        return result;
    }

    private static byte[] combineTwoByteArray(byte[] a, byte[] b) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(a);
            os.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return os.toByteArray();
    }

    private static boolean checkCode(byte[] code) {
        if (code[0] == '2' && code[1] == '2' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }
    
    private static boolean checkImageCode(byte[] code) {
        if (code[0] == '2' && code[1] == '4' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }

    private static boolean checkReceivedCode(byte[] code){
        if (code[0] == '2' && code[1] == '5' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }

    private static boolean checkPing(byte[] code){
        if (code[0] == '3' && code[1] == '4' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }

    private static boolean checkPingOK(byte[] code){
        if (code[0] == '3' && code[1] == '6' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }
    
    private static boolean checkRequestStreamCode(byte[] code){
        if (code[0] == '3' && code[1] == '8' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }
    
    private static boolean checkAllowStreamCode(byte[] code){
        if (code[0] == '4' && code[1] == '0' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }
    
    private static boolean checkDisallowStreamCode(byte[] code){
        if (code[0] == '4' && code[1] == '2' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }
    
    private static boolean checkAlreadyStreamCode(byte[] code){
        if (code[0] == '4' && code[1] == '4' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }
    
    private static boolean checkStopStreamCode(byte[] code){
        if (code[0] == '4' && code[1] == '6' && code[2] == '0' && code[3] == ' ') {
            return true;
        }

        return false;
    }

    private static byte[] detectCode(byte[] buffer) {

        byte[] code = Arrays.copyOfRange(buffer, 0, 4);

        return code;
    }

    private static byte[] detectEndFile(byte[] buffer, int len) {
        byte[] endFile = new byte[2];
        endFile[0] = buffer[len - 2];
        endFile[1] = buffer[len - 1];
    	
        return endFile;
    }

    private static int checkEndFile(/*byte[] endFile*/ byte[] buffer) {
//        if (endFile[0] == '\r' && endFile[1] == '\n') {
//            return true;
//        }
    	
    	for (int i = 0; i < buffer.length; i++){
    		if (buffer[i] == '\r' && i != buffer.length - 1){
    			if (buffer[i + 1] == '\n'){
    				return i;
    			}
    		}
    	}

        return -1;
    }

    private static byte[] getRealBuffer(byte[] buffer, int len, boolean endFile) {
        byte[] realBuffer = new byte[0];
        if (len > 4) {
            if (endFile == false) {
                realBuffer = Arrays.copyOfRange(buffer, 4, len);
            } else {
                if (len > 6) {
                    realBuffer = Arrays.copyOfRange(buffer, 4, len - 2);
                }
            }

        } else {
            realBuffer = null;
        }


        return realBuffer;
    }
    
    private static byte[] getRealBufferWithIndex(byte[] buffer, int index){
    	byte[] realBuffer = new byte[0];
    	
        if (index > 4) {   	
        	realBuffer = Arrays.copyOfRange(buffer, 4, index);
        } else {
            realBuffer = null;
        }

        return realBuffer;
    }
    
    public interface FileTransferReceiveDataListener{
    	public void onReceiveCode();
    	public void onReceivePing();
    	public void onReceivePingOK();
    	public void onReceiveRequestStreamCode();
    	public void onReceiveAllowStreamCode();
    	public void onReceiveDisallowStreamCode();   	
    	public void onAlreadyStream(ByteArrayOutputStream os);
    	public void onStopStream();
    	
    	public void onReceiveMessage(ByteArrayOutputStream os);
    	public void onReceiveImage(ByteArrayOutputStream os);
    }
}
