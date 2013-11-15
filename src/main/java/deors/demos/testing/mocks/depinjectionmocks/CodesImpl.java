package deors.demos.testing.mocks.depinjectionmocks;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Codes bean lementation and map definition.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
@Entity
@Table(name = "CODES")
public class CodesImpl
    implements Codes {

    /**
     * Serialization Id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The 'code' field.
     */
    private String code;

    /**
     * The 'value' field.
     */
    private String value;

    /**
     * Default constructor.
     */
    public CodesImpl() {

        super();
    }

    /**
     * Constructor that sets 'code' and 'value' fields.
     *
     * @param code the 'code' field value
     * @param value the 'value' field value
     */
    public CodesImpl(String code, String value) {

        super();
        setCode(code);
        setValue(value);
    }

    /**
     * Returns the 'code' field value.
     *
     * @return the field value
     */
    @Id
    @Column(name = "CODE")
    public final String getCode() {

        return code;
    }

    /**
     * Changes the 'code' field value.
     *
     * @param code the field new value
     */
    public final void setCode(String code) {

        this.code = code;
    }

    /**
     * Returns the 'value' field value.
     *
     * @return the field value
     */
    @Column(name = "VALUE")
    public final String getValue() {

        return value;
    }

    /**
     * Changes the 'value' field value.
     *
     * @param value the field new value
     */
    public final void setValue(String value) {

        this.value = value;
    }
}
