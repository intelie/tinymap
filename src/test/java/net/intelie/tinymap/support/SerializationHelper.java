package net.intelie.tinymap.support;

import java.io.*;

public class SerializationHelper {
    public static <T> T roundTrip(T obj) throws Exception {
        byte[] serialized = testSerialize(obj);
        return testDeserialize(serialized);
    }

    public static <T> T testDeserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
        ObjectInputStream istream = new ObjectInputStream(bais);
        return (T) istream.readObject();
    }

    public static byte[] testSerialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream ostream = new ObjectOutputStream(baos);

        ostream.writeObject(obj);
        ostream.flush();
        return baos.toByteArray();
    }
}

