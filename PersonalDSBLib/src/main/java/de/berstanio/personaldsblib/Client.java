package de.berstanio.personaldsblib;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static Object sendToServer(Object... objects) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("62.75.210.181"), 21589);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        for (Object o : objects) {
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        }
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        Object back = objectInputStream.readObject();
        objectInputStream.close();
        objectOutputStream.close();
        socket.close();
        return back;
    }
}
