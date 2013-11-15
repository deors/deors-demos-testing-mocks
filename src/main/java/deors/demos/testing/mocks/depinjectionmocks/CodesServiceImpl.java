package deors.demos.testing.mocks.depinjectionmocks;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Codes service implementation using Spring Transaction.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
@Service("codesService")
@Transactional
public class CodesServiceImpl
    implements CodesService {

    /**
     * The Codes data access object instance.
     */
    private CodesDAO codesDAO;

    /**
     * Default constructor.
     */
    public CodesServiceImpl() {

        super();
    }

    /**
     * Returns all Codes records.
     *
     * @return all records
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Collection<? extends Codes> selectAll() {

        return codesDAO.selectAll();
    }

    /**
     * Returns Codes record by the given 'code' value.
     *
     * @param criteria the bean containing the 'code' to be selected
     * @return the selected record
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Codes select(Codes criteria) {

        return codesDAO.select(criteria);
    }

    /**
     * Adds a new Codes record.
     *
     * @param record the new record
     * @return the just added record
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Codes insert(Codes record) {

        return codesDAO.insert(record);
    }

    /**
     * Updates existing Codes records.
     *
     * @param record the record to be updated
     * @return number of records updated
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int update(Codes record) {

        return codesDAO.update(record);
    }

    /**
     * Deletes existing Codes records.
     *
     * @param record the record to be deleted
     * @return number of records deleted
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int delete(Codes record) {

        return codesDAO.delete(record);
    }

    /**
     * Sets the Codes data access object instance.
     *
     * @param codesDAO the data access object instance
     */
    public void setCodesDAO(CodesDAO codesDAO) {

        this.codesDAO = codesDAO;
    }
}
