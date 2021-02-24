package de.berstanio.personaldsblib;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client, welcher mit dem PersonalDSBServer kommunizieren kann
 */
public class Client {

    /**
     * Sendet eine Reihe an Daten an den PersonalDSBServer
     * @param objects Die Objekte, die an den Server gesendet werden sollen
     * @return Das Objekt, welches der Server zurückschickt.(Es gibt immer eine 1 Objekt antwort -&gt; Anfrage-Antwort-System)
     * @throws IOException Wenn es ein Problem mit der Verbindung zum Server gibt
     * @throws ClassNotFoundException Wenn der Server eine andere Version von der GHGSEK2DSBParser Bibliothek benutzt
     */
    public static Object sendToServer(Object... objects) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("62.75.210.181"), 21589);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        for (Object o : objects) {
            if (o instanceof Integer){
                objectOutputStream.writeInt((int)o);
            }else {
                objectOutputStream.writeObject(o);
            }
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
