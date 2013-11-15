package deors.demos.testing.mocks.servletmocks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for managing INI configuration files.<br>
 *
 * The INI configuration entries are written in pairs <code><i>key</i>,<i>value</i></code>
 * separated by the character <code>'='</code> and lines starting with the character
 * <code>';'</code> are considered as comments; both characters are configurable.<br>
 *
 * The INI configuration entries are organized in sections. Each section begins in a line that
 * contains the section name surrounded by the characters <code>'['</code> and <code>']'</code>
 * (also configurable). If the file have no sections or there are entries before the first, these
 * entries belong to the default section.<br>
 *
 * @author jorge.hidalgo
 * @version 2.4
 */
public final class INIFileManager {

    /**
     * The configuration file.
     */
    private File iniFile;

    /**
     * List used to keep the order of the sections in the INI configuration file. The default
     * section, if present, is indexed using a blank string as its name.
     */
    private final List<String> sections = new ArrayList<String>();

    /**
     * Map used to keep the order of the entries in a section in the INI configuration file.
     * The key in this map is the section name. A blank string is used for the default section.
     * The object stored is a List with the keys of the entries that belong to the section.
     */
    private final Map<String, List<String>> keysBySection = new HashMap<String, List<String>>();

    /**
     * Map used to store the values in the INI configuration file. The key in this map is
     * the section plus a period character and the key of the entry. If the entry belongs to the
     * default section, the key in this hashtable is the period character plus the key of the entry.
     */
    private final Map<String, String> valuesByID = new HashMap<String, String>();

    /**
     * Map used to store the comments associated to the entries or sections in the INI
     * configuration file. The key in this map is the section plus a period character and the
     * key of the entry. If the entry belongs to the default section, the key in this hashtable is
     * the period character plus the key of the entry. If the comment belongs to a section, the key
     * is the section only. The default section have not comments associated. The object stored is
     * a List with the comments associated to the entry or section.
     */
    private final Map<String, List<String>> commentsByID = new HashMap<String, List<String>>();

    /**
     * The start of a line with comments.
     */
    private static final String COMMENTS_STARTS_WITH = ";";

    /**
     * The start of a section.
     */
    private static final String SECTIONS_STARTS_WITH = "[";

    /**
     * The end of a section.
     */
    private static final String SECTIONS_ENDS_WITH = "]";

    /**
     * The separator between keys and values.
     */
    private static final String KEY_VALUE_SEPARATOR = "=";

    /**
     * The period character, used to separate section names from key names when composing key ID's.
     */
    private static final String PERIOD = ".";

    /**
     * Constructor that sets the configuration file.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the given file is
     * a directory or cannot be read.
     *
     * @param iniFile the file with the configuration information
     *
     * @throws java.io.IOException an I/O exception
     */
    public INIFileManager(File iniFile)
        throws java.io.IOException {

        this.iniFile = iniFile;

        if (!iniFile.exists()) {
            throw new FileNotFoundException("ERR_FILE_NOT_FOUND");
        }

        if (iniFile.isDirectory()) {
            throw new IllegalArgumentException("ERR_FILE_IS_DIRECTORY");
        }

        if (!iniFile.canRead()) {
            throw new IllegalArgumentException("ERR_FILE_IS_NOT_READABLE");
        }

        readFile();
    }

    /**
     * Constructor that sets the configuration file using its name.
     *
     * @param iniFileName name of the file with the configuration information
     *
     * @throws java.io.IOException an I/O exception
     */
    public INIFileManager(String iniFileName)
        throws java.io.IOException {

        this(new File(iniFileName));
    }

    /**
     * Adds an entry to the default section and sets its value. If the entry already exists the
     * method returns <code>false</code>.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the key and/or the
     * value are <code>null</code> or empty.
     *
     * @param key the key of the new entry
     * @param value the value of the new entry
     *
     * @return <code>true</code> if the entry was added or <code>false</code> if the entry
     *         already exists
     */
    public boolean addEntry(String key, String value) {

        return addEntry(null, key, value, null);
    }

    /**
     * Adds an entry to the given section and sets its value. If the entry already exists the method
     * returns <code>false</code>. If the section is <code>null</code> or empty, the entry is
     * added to the default section.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the key and/or the
     * value are <code>null</code> or empty.
     *
     * @param section the section of the new entry
     * @param key the key of the new entry
     * @param value the value of the new entry
     *
     * @return <code>true</code> if the entry was added or <code>false</code> if the entry
     *         already exists
     */
    public boolean addEntry(String section, String key, String value) {

        return addEntry(section, key, value, null);
    }

    /**
     * Adds an entry to the given section and sets its value and its comment. If the entry already
     * exists the method returns <code>false</code>. If the section is <code>null</code> or
     * empty, the entry is added to the default section. If the comment is <code>null</code> or
     * empty, it will not be added.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the key and/or the
     * value are <code>null</code> or empty.
     *
     * @param section the section of the new entry
     * @param key the key of the new entry
     * @param value the value of the new entry
     * @param comments the comments of the new entry
     *
     * @return <code>true</code> if the entry was added or <code>false</code> if the entry
     *         already exists
     */
    public boolean addEntry(String section, String key, String value, List<String> comments) {

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("ERR_KEY_NULL");
        }

        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("ERR_VALUE_NULL");
        }

        String entryID = getEntryID(section, key);

        if (valuesByID.containsKey(entryID)) {
            return false;
        }

        valuesByID.put(entryID, value);

        if (comments != null && !comments.isEmpty()) {
            commentsByID.put(entryID, comments);
        }

        addSection(section);

        String sectionID = getEntryID(section, null);
        List keys = keysBySection.get(sectionID);

        keys.add(key);

        return true;
    }

    /**
     * Adds the default section to the INI file. If the section exists, the method returns
     * <code>false</code>.
     *
     * @return <code>true</code> if the section was added or <code>false</code> if the section
     *         already exists
     */
    public boolean addSection() {

        return addSection(null, null);
    }

    /**
     * Adds the given section to the INI file. If the section exists, the method returns
     * <code>false</code>.
     *
     * @param section the name of the new section
     *
     * @return <code>true</code> if the section was added or <code>false</code> if the section
     *         already exists
     */
    public boolean addSection(String section) {

        return addSection(section, null);
    }

    /**
     * Adds the given section to the INI file and sets its comments. If the section is
     * <code>null</code> or empty, the method adds the comments of the default section. If the
     * section exists, the method returns <code>false</code>.
     *
     * @param section the name of the new section
     * @param comments the comments of the new section
     *
     * @return <code>true</code> if the section was added or <code>false</code> if the section
     *         already exists
     */
    public boolean addSection(String section, List<String> comments) {

        String sectionID = getEntryID(section, null);

        if (sections.contains(sectionID)) {
            return false;
        }

        sections.add(sectionID);
        keysBySection.put(sectionID, new ArrayList<String>());

        if (comments != null && !comments.isEmpty()) {
            commentsByID.put(sectionID, comments);
        }

        return true;
    }

    /**
     * Returns the entry ID. For the default section (the section and the key are <code>null</code>
     * or empty), the entry ID is a blank string. For keys in the default section (the section is
     * <code>null</code> or empty), the entry ID is the period character plus the key. For other
     * sections than the default one (the key is <code>null</code> or empty), the entry ID is the
     * section only. For keys in other sections than the default one, the entry ID is the section
     * plus the period character plus the key.
     *
     * @param section the section
     * @param key the key
     *
     * @return the entry ID
     */
    private String getEntryID(String section, String key) {

        String retValue = null;
        if (section == null || section.length() == 0) {
            if (key == null || key.length() == 0) {
                retValue = "";
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(PERIOD);
                buffer.append(key);
                retValue = buffer.toString();
            }
        } else {
            if (key == null || key.length() == 0) {
                retValue = section;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(section);
                buffer.append(PERIOD);
                buffer.append(key);
                retValue = buffer.toString();
            }
        }

        return retValue;
    }

    /**
     * Returns a list with the names of the sections in the INI configuration file.
     *
     * @return a list with the names of the sections in the INI configuration file
     */
    public List<String> getSections() {

        return sections;
    }

    /**
     * Returns the value for the given entry in the default section. If the entry is not found the
     * method returns <code>null</code>.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the key is
     * <code>null</code> or empty.
     *
     * @param key key of the entry
     *
     * @return the entry value or <code>null</code> if the entry is not found
     */
    public String getValue(String key) {

        return getValue(null, key);
    }

    /**
     * Returns the value for the given entry in the given section. If the entry is not found the
     * method returns <code>null</code>.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the key is
     * <code>null</code> or empty.
     *
     * @param section section of the entry
     * @param key key of the entry
     *
     * @return the entry value or <code>null</code> if the entry is not found
     */
    public String getValue(String section, String key) {

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("ERR_KEY_NULL");
        }

        return valuesByID.get(getEntryID(section, key));
    }

    /**
     * Returns the value for the given entry in the given section. If the entry is not found the
     * method returns the given default value.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the key is
     * <code>null</code> or empty.
     *
     * @param section section of the entry
     * @param key key of the entry
     * @param defaultValue default value
     *
     * @return the entry value or <code>null</code> if the entry is not found
     */
    public String getValue(String section, String key, String defaultValue) {

        String value = getValue(section, key);
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Reads the INI configuration file. Invalid lines are ignored.
     *
     * @throws java.io.IOException an I/O exception
     */
    private void readFile()
        throws java.io.IOException {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(iniFile));

            ArrayList tempComments = null;
            String section = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    if (tempComments != null) {
                        tempComments.add("");
                    }

                    continue;
                }

                if (line.startsWith(COMMENTS_STARTS_WITH)) {
                    if (tempComments == null) {
                        tempComments = new ArrayList();
                    }

                    tempComments.add(line.substring(1).trim());

                    continue;
                }

                if (line.startsWith(SECTIONS_STARTS_WITH)) {
                    int endPos = line.indexOf(SECTIONS_ENDS_WITH);
                    if (endPos != -1) {
                        section = line.substring(1, endPos);
                        if (tempComments == null || tempComments.isEmpty()) {
                            addSection(section);
                        } else {
                            addSection(section, tempComments);
                            tempComments = null;
                        }
                    }

                    continue;
                }

                int sepPos = line.indexOf(KEY_VALUE_SEPARATOR);
                if (sepPos != -1) {
                    String key = line.substring(0, sepPos).trim();
                    String value = line.substring(sepPos + 1).trim();

                    if (key.length() == 0 || value.length() == 0) {
                        continue;
                    }

                    if (tempComments == null || tempComments.isEmpty()) {
                        addEntry(section, key, value);
                    } else {
                        addEntry(section, key, value, tempComments);
                        tempComments = null;
                    }
                }
            }
        } catch (IOException ioe) {
            throw new IOException("ERR_IO_READ", ioe);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                throw new IOException("ERR_IO_READ", ioe);
            }
        }
    }
}
