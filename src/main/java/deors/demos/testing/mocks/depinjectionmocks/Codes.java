package deors.demos.testing.mocks.depinjectionmocks;

import java.io.Serializable;

/**
 * The Codes bean interface.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public interface Codes
    extends Serializable {

    /**
     * Returns the 'code' field value.
     *
     * @return the field value
     */
    String getCode();

    /**
     * Changes the 'code' field value.
     *
     * @param code the field new value
     */
    void setCode(String code);

    /**
     * Returns the 'value' field value.
     *
     * @return the field value
     */
    String getValue();

    /**
     * Changes the 'value' field value.
     *
     * @param value the field new value
     */
    void setValue(String value);
}
