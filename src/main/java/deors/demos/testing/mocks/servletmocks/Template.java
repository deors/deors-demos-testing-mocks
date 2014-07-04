package deors.demos.testing.mocks.servletmocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loads and processes templates.
 *
 * A template is a collection of strings with special tokens that can be substituted by replacements
 * using the token content as the key for substitutions.
 *
 * @author deors
 * @version 1.0
 */
public final class Template {

    /**
     * The template source stream.
     */
    private InputStream templateSource;

    /**
     * The template contents.
     */
    private List<String> templateContents;

    /**
     * The template tag start character.
     */
    private static final char TEMPLATE_TAG_START = '[';

    /**
     * The template tag end character.
     */
    private static final char TEMPLATE_TAG_END = ']';

    /**
     * Default constructor. The template needs to be initialized before being processed.
     */
    public Template() {
        super();
    }

    /**
     * Constructor that initializes the template source and loads its contents.
     *
     * @param templateSource the template source stream
     *
     * @throws TemplateException an exception loading the template
     */
    public Template(InputStream templateSource) throws TemplateException {
        this();

        this.templateSource = templateSource;
        loadTemplate();
    }

    /**
     * Returns the <code>templateSource</code> property value.
     *
     * @return the property value
     *
     * @see Template#templateSource
     * @see Template#setTemplateSource(java.io.InputStream)
     */
    public InputStream getTemplateSource() {
        return templateSource;
    }

    /**
     * Loads the template. The method reads the template source file and stores its contents as an
     * array.
     *
     * @throws TemplateException an exception loading the template
     */
    public void loadTemplate() throws TemplateException {

        BufferedReader templateReader = null;

        try {
            templateReader = new BufferedReader(new InputStreamReader(templateSource));

            templateContents = new ArrayList<String>();
            String templateLine = null;
            while ((templateLine = templateReader.readLine()) != null) {
                templateContents.add(templateLine);
            }
        } catch (IOException ioe) {
            templateContents = null;

            throw new TemplateException("TMPL_ERR_NOT_LOADED", ioe); //$NON-NLS-1$
        } finally {
            if (templateReader != null) {
                try {
                    templateReader.close();
                } catch (IOException ioe) {
                    throw new TemplateException("TMPL_ERR_NOT_CLOSED", ioe); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Process the template. The method searches in the template contents for tokens, delimited by
     * the constants <code>TEMPLATE_TAG_START</code> and <code>TEMPLATE_TAG_END</code>, their
     * default values being <code>'['</code> and <code>']'</code>, and then substitutes the
     * token including the delimiters with the matching indexed value from the given hash table. If
     * a replacement is not given for a token, then the token is not modified. The result is
     * returned as an <code>ArrayList</code> object.
     *
     * @param replacements hash table with the replacements indexed by the token content
     *
     * @return the processed contents in an <code>ArrayList</code> object, each line as an string
     *         in the array
     *
     * @throws TemplateException the template contents are not loaded
     *
     * @see Template#TEMPLATE_TAG_START
     * @see Template#TEMPLATE_TAG_END
     */
    public List<String> processTemplate(Map<String, String> replacements) throws TemplateException {

        if (templateContents == null) {
            throw new TemplateException("TMPL_ERR_NEED_LOAD"); //$NON-NLS-1$
        }

        List<String> processedContents = new ArrayList<String>();

        Iterator<String> it = templateContents.iterator();
        while (it.hasNext()) {
            String line = it.next();

            if (replacements == null) {
                processedContents.add(line);
            } else {
                int startPos = line.indexOf(TEMPLATE_TAG_START);

                if (startPos == -1) {
                    processedContents.add(line);
                } else {
                    StringBuffer newLine = new StringBuffer();

                    while ((startPos = line.indexOf(TEMPLATE_TAG_START)) != -1) {
                        // two tag start characters together mean
                        // that this is not a token definition
                        if (line.substring(startPos + 1, startPos + 2).toCharArray()[0]
                                == TEMPLATE_TAG_START) {
                            newLine.append(line.substring(0, startPos + 2));
                            line = line.substring(startPos + 2);
                        } else {
                            // when the tag end character is not found mean
                            // that this is not a token definition
                            int endPos = line.indexOf(TEMPLATE_TAG_END, startPos);
                            if (endPos == -1) {
                                break;
                            }

                            // searches the replacement
                            String tokenName = line.substring(startPos + 1, endPos);
                            String replacement = replacements.get(tokenName);

                            if (replacement == null) {
                                newLine.append(line.substring(0, endPos + 1));
                            } else {
                                newLine.append(line.substring(0, startPos));
                                newLine.append(replacement);
                            }
                            line = line.substring(endPos + 1);
                        }
                    }

                    newLine.append(line);

                    processedContents.add(newLine.toString());
                }
            }
        }

        return processedContents;
    }

    /**
     * Process the template and writes the output to the given print writer.
     *
     * @param replacements hash table with the replacements indexed by the token content
     * @param target the output print writer
     *
     * @throws TemplateException the template contents are not loaded
     *
     * @see Template#processTemplate(Map)
     * @see Template#TEMPLATE_TAG_START
     * @see Template#TEMPLATE_TAG_END
     */
    public void processTemplate(Map<String, String> replacements, PrintWriter target)
        throws TemplateException {

        List<String> outputContents = processTemplate(replacements);

        for (int i = 0, n = outputContents.size(); i < n; i++) {
            target.println(outputContents.get(i));
        }
    }

    /**
     * Sets the <code>templateSource</code> property value and resets the template contents.
     *
     * @param templateSource the property new value
     *
     * @see Template#templateSource
     * @see Template#getTemplateSource()
     */
    public void setTemplateSource(InputStream templateSource) {
        this.templateSource = templateSource;
        this.templateContents = null;
    }
}
