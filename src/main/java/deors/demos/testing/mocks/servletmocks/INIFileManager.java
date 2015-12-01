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
 * Class for managing INI configuration files.
 *
 * The INI configuration entries are written in pairs <code><i>key</i>,<i>value</i></code>
 * separated by the character <code>'='</code> and lines starting with the character
 * <code>';'</code> are considered as comments; both characters are configurable.
 *
 * The INI configuration entries are organized in sections. Each section begins in a line that
 * contains the section name surrounded by the characters <code>'['</code> and <code>']'</code>
 * (also configurable). If the file have no sections or there are entries before the first, these
 * entries belong to the default section.
 *
 * @author deors
 * @version 1.0
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
     * the section plus a dot character and the key of the entry. If the entry belongs to the
     * default section, the key in this hash table is the dot character plus the key of the entry.
     */
    private final Map<String, String> valuesByID = new HashMap<String, String>();

    /**
     * Map used to store the comments associated to the entries or sections in the INI
     * configuration file. The key in this map is the section plus a dot character and the
     * key of the entry. If the entry belongs to the default section, the key in this hash table is
     * the dot character plus the key of the entry. If the comment belongs to a section, the key
     * is the section only. The default section have not comments associated. The object stored is
     * a List with the comments associated to the entry or section.
     */
    private final Map<String, List<String>> commentsByID = new HashMap<String, List<String>>();

    /**
     * The start of a line with comments. Configurable in the properties file using the key
     * <code>inimgr.commentsStartsWith</code>. Default value is <code>;</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String COMMENTS_STARTS_WITH = ";"; //$NON-NLS-1$

    /**
     * The start of a line with a section. Configurable in the properties file using the key
     * <code>inimgr.sectionNamesStartsWith</code>. Default value is <code>[</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String SECTIONS_STARTS_WITH = "["; //$NON-NLS-1$

    /**
     * The end of a line with a section. Configurable in the properties file using the key
     * <code>inimgr.sectionNamesEndsWith</code>. Default value is <code>]</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String SECTIONS_ENDS_WITH = "]"; //$NON-NLS-1$

    /**
     * The separator between keys and values. Configurable in the properties file using the key
     * <code>inimgr.keyValueSeparator</code>. Default value is <code>=</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String KEY_VALUE_SEPARATOR = "="; //$NON-NLS-1$

    /**
     * The dot character, used to separate section names from key names when composing key ID's.
     */
    private static final String DOT = "."; //$NON-NLS-1$

    /**
     * Constructor that sets the configuration file.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the given file is
     * a directory or cannot be read.
     *
     * @param iniFile the file with the configuration information
     *
     * @throws IOException an I/O exception
     */
    public INIFileManager(File iniFile)
        throws IOException {

        this.iniFile = iniFile;

        checkFile();
        readFile();
    }

    /**
     * Adds an entry to the given section and sets its value. If the entry already exists the method
     * returns <code>false</code>. If the section is <code>null</code> or empty, the entry is
     * added to the default section.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the key and/or the
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
     * empty, it will not be added.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the key and/or the
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

        if (checkString(key)) {
            throw new IllegalArgumentException("INIMGR_ERR_KEY_NULL"); //$NON-NLS-1$
        }

        if (checkString(value)) {
            throw new IllegalArgumentException("INIMGR_ERR_VALUE_NULL"); //$NON-NLS-1$
        }

        String entryID = getEntryID(section, key);

        if (valuesByID.containsKey(entryID)) {
            return false;
        }

        // the value of the new entry is added
        // to the values hash table
        valuesByID.put(entryID, value);

        // the comments of the new entry are added
        // to the comments hash table
        if (comments != null && !comments.isEmpty()) {
            commentsByID.put(entryID, comments);
        }

        // if the section does not exist we add it
        // to the end of the file
        addSection(section);

        // we need the list that keeps the order in the section
        String sectionID = getEntryID(section, null);
        List<String> keys = keysBySection.get(sectionID);

        // the new entry is added to the end of the section
        keys.add(key);

        return true;
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

        // the section is added to the end of the file
        sections.add(sectionID);
        keysBySection.put(sectionID, new ArrayList<String>());

        // the comments of the new section are added
        // to the comments hash table
        if (comments != null && !comments.isEmpty()) {
            commentsByID.put(sectionID, comments);
        }

        return true;
    }

    /**
     * Checks the INI file before the manager is fully initialized.
     *
     * @throws FileNotFoundException the INI file is not available or not valid
     */
    private void checkFile()
        throws FileNotFoundException {

        if (!iniFile.exists()) {
            throw new FileNotFoundException("INIMGR_ERR_FILE_NOT_FOUND"); //$NON-NLS-1$
        }

        if (iniFile.isDirectory()) {
            throw new IllegalArgumentException("INIMGR_ERR_FILE_IS_DIRECTORY"); //$NON-NLS-1$
        }

        if (!iniFile.canRead()) {
            throw new IllegalArgumentException("INIMGR_ERR_FILE_IS_NOT_READABLE"); //$NON-NLS-1$
        }
    }

    /**
     * Checks the given value and returns <code>true</code> if it is <code>null</code>
     * or the empty string.
     *
     * @param value the string that will be checked
     *
     * @return whether the string is empty or not
     */
    private boolean checkString(String value) {

        return value == null || value.isEmpty();
    }


    /**
     * Returns the entry ID for keys in the default section.
     *
     * @param key the key
     *
     * @return the entry ID
     *
     * @see #getEntryID(String, String)
     */
    private String getEntryID(String key) {

        String retValue = null;
        if (checkString(key)) {
            retValue = "";
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append(DOT);
            buffer.append(key);
            retValue = buffer.toString();
        }

        return retValue;
    }

    /**
     * Returns the entry ID. For the default section (the section and the key are <code>null</code>
     * or empty), the entry ID is a blank string. For keys in the default section (the section is
     * <code>null</code> or empty), the entry ID is the dot character plus the key. For other
     * sections than the default one (the key is <code>null</code> or empty), the entry ID is the
     * section only. For keys in other sections than the default one, the entry ID is the section
     * plus the dot character plus the key.
     *
     * @param section the section
     * @param key the key
     *
     * @return the entry ID
     */
    private String getEntryID(String section, String key) {

        String retValue = getEntryID(key);
        if (!checkString(section)) {
            retValue = section + retValue;
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
     * Returns the value for the given entry in the given section. If the entry is not found the
     * method returns <code>null</code>.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the key is
     * <code>null</code> or empty.
     *
     * @param section section of the entry
     * @param key key of the entry
     *
     * @return the entry value or <code>null</code> if the entry is not found
     */
    public String getValue(String section, String key) {

        if (checkString(key)) {
            throw new IllegalArgumentException("INIMGR_ERR_KEY_NULL"); //$NON-NLS-1$
        }

        return valuesByID.get(getEntryID(section, key));
    }

    /**
     * Reads the INI configuration file. Invalid lines are ignored.
     *
     * @throws IOException an I/O exception
     */
    private void readFile()
        throws IOException {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(iniFile));

            ArrayList<String> tempComments = null;
            String section = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (tempComments != null) {
                        tempComments.add("");
                    }

                    continue;
                }

                if (line.startsWith(COMMENTS_STARTS_WITH)) {
                    if (tempComments == null) {
                        tempComments = new ArrayList<String>();
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

                    if (key.isEmpty() || value.isEmpty()) {
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
            throw new IOException("INIMGR_ERR_IO_READ", ioe); //$NON-NLS-1$
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                throw new IOException("INIMGR_ERR_IO_READ", ioe); //$NON-NLS-1$
            }
        }
    }
}
