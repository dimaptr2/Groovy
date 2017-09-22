package ru.velkomfood.mysap.dms.core

import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoDestinationManager
import com.sap.conn.jco.JCoFunction
import com.sap.conn.jco.JCoTable
import com.sap.conn.jco.ext.DestinationDataProvider
import groovy.sql.Sql
import groovy.transform.CompileStatic

@CompileStatic
class DataProcessor {

    private final String DB_URL = 'jdbc:sqlite:dms.db'
    private final String DB_DRIVER = 'org.sqlite.JDBC'
    private Properties params
    private JCoDestination destination
    private Sql sql

    DataProcessor() {
        params = new Properties()
    }

    void getSapDestination() {
        readInitialParameters()
        createDestinationDataFile(params.getProperty('dest_name'))
        destination = JCoDestinationManager.getDestination(params.getProperty('dest_name'))
    }

    void openConnections(){
        sql = Sql.newInstance(DB_URL, DB_DRIVER)
        createDatabaseStructure()
    }

    void closeConnections() {
        sql.close()
    }

    private void readInitialParameters() {
        File fd = new File('config/destination.properties')
        if (fd.exists()) {
            params.load(fd.newDataInputStream())
        }
    }

    private void createDestinationDataFile(String name) {
        File cfg = new File(name + params.getProperty('suffix'))
        if (!cfg.exists()) {
            Properties sap = new Properties()
            sap.setProperty(DestinationDataProvider.JCO_ASHOST, params.getProperty('host'))
            sap.setProperty(DestinationDataProvider.JCO_SYSNR, params.getProperty('sys_number'))
            sap.setProperty(DestinationDataProvider.JCO_R3NAME, params.getProperty('sys_id'))
            sap.setProperty(DestinationDataProvider.JCO_CLIENT, params.getProperty('client'))
            sap.setProperty(DestinationDataProvider.JCO_USER, params.getProperty('user'))
            sap.setProperty(DestinationDataProvider.JCO_PASSWD, params.getProperty('password'))
            sap.setProperty(DestinationDataProvider.JCO_LANG, params.getProperty('language'))
            sap.store(cfg.newDataOutputStream(), "Production environment")
        }
    }

    private void createDatabaseStructure() {

        def sb = new StringBuilder(0)
        sb << "CREATE TABLE IF NOT EXISTS documents(doc_type VARCHAR(3) NOT NULL,"
        sb << " id INTEGER NOT NULL, version VARCHAR(3) NOT NULL, part VARCHAR(3) NOT NULL,"
        sb << " description VARCHAR(50), user VARCHAR(50), PRIMARY KEY (doc_type, id, version, part))"
        sql.execute(sb.toString())

        sb.delete(0, sb.length())

        sb << "CREATE TABLE IF NOT EXISTS details(id INTEGER PRIMARY KEY,"
        sb << " vendor_id VARCHAR(10), vendor_name VARCHAR(50),"
        sb << " contract VARCHAR(50),"
        sb << " term1 DATE, term2 DATE, term3 DATE, term4 DATE, term5 DATE,"
        sb << " status1 VARCHAR(3), status2 VARCHAR(3), status3 VARCHAR(3),"
        sb << " status4 VARCHAR(3), status5 VARCHAR(3))"
        sql.execute(sb.toString())

    }

    // Main public methods

    void readAllDocuments() {

        JCoFunction bapiDocList = destination.getRepository().getFunction('BAPI_DOCUMENT_GETLIST')

        bapiDocList.getImportParameterList().setValue('DOCUMENTTYPE', 'ZDO')
        bapiDocList.getImportParameterList().setValue('STATUSINTERN', 'DB')
        bapiDocList.execute(destination)

        JCoTable docs = bapiDocList.getTableParameterList().getTable('DOCUMENTLIST')
        if (docs.getNumRows() > 0) {
            def indices = 0..(docs.getNumRows() - 1)
            for (int j in indices) {
                docs.setRow(j)
                Map document = [:]
                document['doc_type'] = docs.getString('DOCUMENTTYPE')
                long id = docs.getLong('DOCUMENTNUMBER')
                document['id'] = id
                document['version'] = docs.getString('DOCUMENTVERSION')
                document['part'] = docs.getString('DOCUMENTPART')
                document['description'] = docs.getString('DESCRIPTION')
                document['user'] = docs.getString('USERNAME')
                if (!existsDocument(id)) {
                    saveDocument(document)
                }
            }
        }

    }

    void readDocumentDetails() {

        JCoFunction bapiDetails = destination.getRepository().getFunction('BAPI_DOCUMENT_GETDETAIL2')

        List<Map> ids = getAllDocumentsFromDatabase()
        for (def dms in ids) {

        }
    }

    // Additional private methods

    private boolean existsDocument(long id) {
        boolean existence = false
        sql.query('SELECT id FROM documents WHERE id = ' + id) { resultSet ->
            while (resultSet.next()) {
                existence = true
                println("Document number ${resultSet.getLong('id')} already exists")
                break
            }
        }
        existence
    }

    private void saveDocument(Map doc) {
        def command = "INSERT INTO documents VALUES (" +
                "\'${doc['doc_type']}\', ${doc['id']}, \'${doc['version']}\', " +
                "\'${doc['part']}\', \'${doc['description']}\', \'${doc['user']}\')"
        sql.execute(command)
        println("Document number ${doc['id']} saved")
    }

    private List<Map> getAllDocumentsFromDatabase() {
        List docs = []
        sql.query('SELECT id FROM documents ORDER BY doc_type, id, version, part') { rs ->
            while (rs.next()) {
                Map row = [:]
                long key = rs.getLong('id')
                row[key] = transformToAlphaForm(key, 25)
                docs << row
            }
        }
        return docs
    }

    // alpha transformation
    private String transformToAlphaForm(long id, int maxValue) {

        def sb = new StringBuilder(0)
        String txtId = id.toString()
        int delta = maxValue - txtId.length()
        if (delta > 0) {
            def idx = 1..delta
            for (int index in idx) {
                sb << '0'
            }
        }
        sb << txtId

        sb.toString()
    }



}
