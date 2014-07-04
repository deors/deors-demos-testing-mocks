package deors.demos.testing.mocks.servletmocks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Toolkit methods for working with streams and files.<br>
 *
 * @author deors
 * @version 1.0
 */
public final class IOToolkit {

    /**
     * Flag used to mark a file to be deleted when the VM exists.
     */
    public static final boolean DELETE_ON_EXIT = true;

    /**
     * Flag used to mark a file to be left when the VM exists.
     */
    public static final boolean LEAVE_ON_EXIT = false;

    /**
     * Default constructor. This class is a toolkit and therefore it cannot be instantiated.
     */
    private IOToolkit() {
        super();
    }

    /**
     * Compares length and data from both byte arrays.
     *
     * @param array1 the first array
     * @param array2 the second array
     *
     * @return whether both arrays are of the same length and contain the same data
     */
    public static boolean compareArrays(byte[] array1, byte[] array2) {

        if (array1.length != array2.length) {
            return false;
        }

        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Partially compares data from both byte arrays.
     *
     * @param array1 the first array
     * @param array2 the second array
     * @param off the offset
     * @param length the number of bytes to be compared
     *
     * @return whether both arrays contain the same data
     */
    public static boolean compareArrays(byte[] array1, byte[] array2, int off, int length) {

        for (int i = off; i < off + length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compares data from both input streams using the given buffer size. The method does not use
     * the <code>available()</code> method from the <code>InputStream</code> class because some
     * implementations does not return valid values.
     *
     * @param stream1 the first input stream
     * @param stream2 the second input stream
     * @param bufferSize the buffer size
     *
     * @return whether both input streams contain the same data
     *
     * @throws IOException an I/O exception
     */
    public static boolean compareStreams(InputStream stream1, InputStream stream2, int bufferSize)
        throws IOException {

        boolean compareStreams = true;
        BufferedInputStream bis1 = null;
        BufferedInputStream bis2 = null;

        try {
            bis1 = new BufferedInputStream(stream1, bufferSize);
            bis2 = new BufferedInputStream(stream2, bufferSize);

            int bytesRead1 = -1;
            int bytesRead2 = -1;
            byte[] buffer1 = new byte[bufferSize];
            byte[] buffer2 = new byte[bufferSize];

            while ((bytesRead1 = bis1.read(buffer1)) != -1) {
                bytesRead2 = bis2.read(buffer2);

                if (bytesRead2 == -1) {
                    compareStreams = false;
                    break;
                }

                if (bytesRead1 != bytesRead2) {
                    compareStreams = false;
                    break;
                }

                if (!compareArrays(buffer1, buffer2, 0, bytesRead1)) {
                    compareStreams = false;
                    break;
                }
            }

            if (compareStreams) {
                // still remains data in stream 2
                bytesRead2 = bis2.read(buffer2);

                if (bytesRead2 != -1) {
                    compareStreams = false;
                }
            }

            return compareStreams;
        } finally {
            if (bis1 != null) {
                try {
                    bis1.close();
                } catch (IOException ioe) {
                    ioe = null;
                }
            }
            if (bis2 != null) {
                try {
                    bis2.close();
                } catch (IOException ioe) {
                    ioe = null;
                }
            }
        }
    }

    /**
     * Compares data from both input streams using the default buffer size. The method does not use
     * the <code>available()</code> method from the <code>InputStream</code> class because some
     * implementations does not return valid values.
     *
     * @param is1 the first input stream
     * @param is2 the second input stream
     *
     * @return whether both input streams contains the same data
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static boolean compareStreams(InputStream is1, InputStream is2)
        throws IOException {

        return compareStreams(is1, is2, 4096);
    }

    /**
     * Writes data from the source stream to the target stream using the given buffer size.
     *
     * @param is the source stream
     * @param os the target stream
     * @param bufferSize the buffer size
     *
     * @throws IOException an I/O exception
     */
    public static void copyStream(InputStream is, OutputStream os, int bufferSize)
        throws IOException {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(is, bufferSize);
            bos = new BufferedOutputStream(os, bufferSize);

            int bytesRead = -1;
            byte[] buffer = new byte[bufferSize];

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe = null;
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ioe) {
                    ioe = null;
                }
            }
        }
    }

    /**
     * Writes data from the source stream to the target stream using the default buffer size.
     *
     * @param is the source stream
     * @param os the target stream
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {

        copyStream(is, os, 4096);
    }

    /**
     * Creates a temporary file.
     *
     * @param deleteOnExit whether the file should be marked to be deleted when the VM exists
     *
     * @return the temporary file
     *
     * @throws IOException an I/O exception
     *
     * @see IOToolkit#DELETE_ON_EXIT
     * @see IOToolkit#LEAVE_ON_EXIT
     */
    public static File createTempFile(boolean deleteOnExit)
        throws IOException {

        File tempFile = File.createTempFile("deors.", ".tmp");

        if (deleteOnExit) {
            tempFile.deleteOnExit();
        }

        return tempFile;
    }

    /**
     * Prints in a writer a string in pieces determined by the given buffer size. This method can be
     * used in web application servers that does not allow large strings to be printed to a servlet
     * stream (i.e. OC4J v9.0.3).
     *
     * @param s the source string
     * @param out the target writer
     * @param bufferSize the buffer size
     *
     * @throws IOException an I/O exception
     */
    public static void printString(String s, Writer out, int bufferSize)
        throws IOException {

        String r = s;

        while (r.length() > 0) {
            out.write(r.substring(0, Math.min(r.length(), bufferSize)));
            r = r.substring(Math.min(r.length(), bufferSize));
        }
    }

    /**
     * Prints in a writer a string in pieces determined by the default buffer size. This method can
     * be used in web application servers that does not allow large strings to be printed to a
     * servlet stream (i.e. OC4J v9.0.3).
     *
     * @param s the source string
     * @param out the target writer
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static void printString(String s, Writer out)
        throws IOException {

        printString(s, out, 4096);
    }

    /**
     * Returns a byte array with the stream contents using the given buffer size.
     *
     * @param is the source stream
     * @param bufferSize the buffer size
     *
     * @return the stream contents
     *
     * @throws IOException an I/O exception
     */
    public static byte[] readStream(InputStream is, int bufferSize)
        throws IOException {

        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(is, bufferSize);

            long length = bis.available();

            // the byte array length must be a valid integer number
            if (length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("IOTK_ERR_STREAM_TOO_LONG"); //$NON-NLS-1$
            }

            byte[] bytes = new byte[(int) length];

            // the file is read into the byte array
            int bytesRead = bis.read(bytes);

            if (bytesRead != length) {
                throw new IOException("IOTK_ERR_STREAM_UNREADABLE"); //$NON-NLS-1$
            }

            return bytes;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    /**
     * Returns a byte array with the stream contents using the default buffer size.
     *
     * @param is the source stream
     *
     * @return the stream contents
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static byte[] readStream(InputStream is)
        throws IOException {

        return readStream(is, 4096);
    }

    /**
     * Returns a byte array with the file contents using the given buffer size.
     *
     * @param file the source file
     * @param bufferSize the buffer size
     *
     * @return the file contents
     *
     * @throws IOException an I/O exception
     */
    public static byte[] readFile(File file, int bufferSize)
        throws IOException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return readStream(fis, bufferSize);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Returns a byte array with the file contents using the default buffer size.
     *
     * @param file the source file
     *
     * @return the file contents
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static byte[] readFile(File file)
        throws IOException {

        return readFile(file, 4096);
    }

    /**
     * Returns a list of strings with the contents of the given text file using
     * the given buffer size.
     *
     * @param file the source text file
     * @param bufferSize the buffer size
     *
     * @return the file contents
     *
     * @throws IOException an I/O exception
     */
    public static List<String> readTextFile(File file, int bufferSize)
        throws IOException {

        List<String> contents = new ArrayList<String>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));

            String line = null;
            while ((line = reader.readLine()) != null) {
                contents.add(line);
            }

            return contents;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe = null;
                }
            }
        }
    }

    /**
     * Returns a list of strings with the contents of the given text file using
     * the default buffer size.
     *
     * @param file the source text file
     *
     * @return the file contents
     *
     * @throws IOException an I/O exception
     */
    public static List<String> readTextFile(File file)
        throws IOException {

        return readTextFile(file, 4096);
    }

    /**
     * Writes the stream contents to a temporary file using the given buffer size.
     *
     * @param is the source stream
     * @param bufferSize the buffer size
     *
     * @return the temporary file with the stream contents
     *
     * @throws IOException an I/O exception
     */
    public static File writeStream(InputStream is, int bufferSize)
        throws IOException {

        File f = createTempFile(LEAVE_ON_EXIT);

        FileOutputStream fos = new FileOutputStream(f);

        copyStream(is, fos, bufferSize);

        return f;
    }

    /**
     * Writes the stream contents to a temporary file using the default buffer size.
     *
     * @param is the source stream
     *
     * @return the temporary file with the stream contents
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static File writeStream(InputStream is)
        throws IOException {

        return writeStream(is, 4096);
    }

    /**
     * Writes the byte array contents to a temporary file using the given buffer size.
     *
     * @param data the source data
     * @param bufferSize the buffer size
     *
     * @return the temporary file with the array contents
     *
     * @throws IOException an I/O exception
     */
    public static File writeFile(byte[] data, int bufferSize)
        throws IOException {

        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(data);
            return writeStream(bais, bufferSize);
        } finally {
            if (bais != null) {
                bais.close();
            }
        }
    }

    /**
     * Writes the byte array contents to a temporary file using the default buffer size.
     *
     * @param data the source data
     *
     * @return the temporary file with the array contents
     *
     * @throws IOException an I/O exception
     *
     * @see CommonsContext#DEFAULT_BUFFER_SIZE
     */
    public static File writeFile(byte[] data)
        throws IOException {

        return writeFile(data, 4096);
    }

    /**
     * Returns the extension part of a file name, excluding the dot character.
     *
     * @param file the file
     *
     * @return the file extension
     */
    public static String extractExtension(File file) {

        if (file == null) {
            return null;
        }

        String fileName = file.getName();
        int extPos = fileName.lastIndexOf('.');
        if (extPos == -1) {
            return "";
        }
        return fileName.substring(extPos + 1);
    }

    /**
     * Returns the name part of a file name, excluding the dot character.
     *
     * @param file the file
     *
     * @return the file extension
     */
    public static String extractName(File file) {

        if (file == null) {
            return null;
        }

        String fileName = file.getName();
        int extPos = fileName.lastIndexOf('.');
        if (extPos == -1) {
            return fileName;
        }
        return fileName.substring(0, extPos);
    }

    /**
     * Returns a new file with the same path and name as the given source file but with
     * the same extension as the other given file.
     *
     * @param source the file which name and parent will be used
     * @param getext the file which extension will be used
     *
     * @return the new file
     */
    public static File makeSameExtension(File source, File getext) {

        if (source == null || getext == null) {
            return null;
        }

        File parent = source.getParentFile();
        String name = extractName(source);
        String extension = extractExtension(getext);

        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append('.');
        sb.append(extension);

        return new File(parent, sb.toString());
    }
}
