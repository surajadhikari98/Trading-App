package io.reactivestax.contract.repository;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface SecuritiesReferenceRepository {
        boolean lookupSecurities(String cusip) throws FileNotFoundException, SQLException;
}
