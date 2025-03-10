package com.github.cronsmith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 
 * SerializationUtils provides methods to copy/serialize/deserialize a task object
 * 
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class SerializationUtils {

    private SerializationUtils() {}

    public static <T> T copy(T serializable) {
        byte[] bytes = serialize(serializable);
        return deserialize(bytes);
    }

    public static byte[] serialize(Object serializable) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeObject(serializable, output);
        return output.toByteArray();
    }

    public static <T> T deserialize(byte[] data) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        return readObject(bis);
    }

    public static void writeObject(Object object, OutputStream os) {
        if (!(object instanceof Serializable)) {
            throw new SerializationException("Not serialiazble object");
        }
        if (os == null) {
            throw new NullPointerException("OutputStream must not be null.");
        }
        ObjectOutputStream oos = null;
        try {
            oos = os instanceof ObjectOutputStream ? (ObjectOutputStream) os
                    : new ObjectOutputStream(os);
            oos.writeObject(object);
            oos.flush();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(InputStream is) {
        if (is == null) {
            throw new NullPointerException("InputStream must not be null.");
        }
        ObjectInputStream ois = null;
        try {
            ois = is instanceof ObjectInputStream ? (ObjectInputStream) is
                    : new ObjectInputStream(is);
            return (T) ois.readObject();
        } catch (IOException e) {
            throw new SerializationException(e);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
