package group_1312141_1312269.ufriends;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by hungmai on 07/04/2016.
 */
public class FileTransferService {

    public static boolean sendFile(final InputStream inputStream, final OutputStream out) {
        byte buf[] = new byte[1024];
        int len;

        try {
            while ((len = inputStream.read(buf)) != -1) {
                byte[] tempBuf = Arrays.copyOfRange(buf, 0, len);
                byte[] newBuff;

                if (inputStream.available() == 0) {
                    newBuff = addCode(tempBuf, true);
                    out.write(newBuff, 0, len + 6);
                } else {
                    newBuff = addCode(tempBuf, false);
                    out.write(newBuff, 0, len + 4);
                }
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean receiveFile(final InputStream inputStream, final OutputStream out) {
        byte buf[] = new byte[1028];
        byte oldBuf[] = null;
        int len = 0;
        int oldLen = 0;

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

                    if (checkCode(code)) {
                        byte[] endFile = detectEndFile(oldBuf, oldLen);
                        if (checkEndFile(endFile)) {
                            byte[] realBuf = getRealBuffer(oldBuf, oldLen, true);
                            out.write(realBuf, 0, oldLen - 6);
                            break;
                        }
                    }
                } else {
                    byte[] code = detectCode(buf);
                    boolean isEnd = checkCode(code);

                    byte[] realBuf = getRealBuffer(buf, len, isEnd);

                    if (realBuf != null) {
                        out.write(realBuf, 0, 1024);
                    }

                    if (isEnd) {
                        break;
                    }
                }
            }

        } catch (IOException e) {

        }
        return true;
    }

    private static byte[] addCode(byte[] buffer, boolean endFile) {
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

    private static boolean checkEndFile(byte[] endFile) {
        if (endFile[0] == '\r' && endFile[1] == '\n') {
            return true;
        }

        return false;
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
}

