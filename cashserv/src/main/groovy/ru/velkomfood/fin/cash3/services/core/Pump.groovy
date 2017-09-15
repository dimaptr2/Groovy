package ru.velkomfood.fin.cash3.services.core

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.sap.conn.jco.*
import com.sap.conn.jco.ext.DestinationDataProvider
import groovy.sql.Sql
import ru.velkomfood.fin.cash3.services.model.ShipmentHead

import java.time.LocalDate

class Pump {

    final String DEST1 = "R15"
    final String DEST2 = "R14"
    final String SUFFIX = ".jcoDestination"

    private JCoDestination destination1
    private JCoDestination destination2
    private Properties sap1
    private Properties sap2
    private Sql sql

    Pump() {
        sap1 = new Properties()
        createDestinationDataFile(DEST1, sap1)
        sap2 = new Properties()
        createDestinationDataFile(DEST2, sap2)
    }

    private void createDestinationDataFile(String destName, Properties props) {
        String fileName = destName + SUFFIX
        File file = new File(fileName)
        if (file.isDirectory() || !file.exists()) {
            props.store(file.newDataOutputStream(), "Production environment")
        }
    }

    void initSAPdestinations() {
        destination1 = JCoDestinationManager.getDestination(DEST1)
        destination2 = JCoDestinationManager.getDestination(DEST2)
    }

    void openDbConnection() {
        def ds = new MysqlDataSource()
        ds.setServerName('localhost')
        ds.setPort(3306)
        ds.setDatabaseName('cj')
        ds.setUser('finman')
        ds.setPassword('12345678')
        sql = new Sql(ds)
    }

    void closeDbConnection() {
        sql.close()
    }

    // Here is a main method
    void processDataStream() {

        def sapDate = {
            String[] dates = LocalDate.now().toString().split('-')
            String txtDate = ''
            dates.each { txtDate += it }.join('')
            txtDate
        }
        def date = sapDate.call()


        // Get shipments by the current date
        def th1 = new Thread()

        th1.start() {
            BigInteger t1 = new Date().getTime()
            println('Uploading shipments')
            getShipments(destination1, date)
            BigInteger t2 = new Date().getTime()
            showAboutExecution('Shipments', t1, t2)
        }

        def th2 = new Thread()
        // Get invoices by the current date
        th2.start() {
            BigInteger t1 = new Date().getTime()
            println('Uploading invoices')
            getInvoices(destination2, date)
            BigInteger t2 = new Date().getTime()
            showAboutExecution('Invoices', t1, t2)
        }

    }

    private List getShipments(JCoDestination destination, String date) {

        JCoFunction rfcDeliveryList = destination
                .getRepository()
                .getFunction("Z_RFC_GET_CURRENT_SHIPMENTS")

        rfcDeliveryList.getImportParameterList().setValue("I_ERDAT", date)
        rfcDeliveryList.getImportParameterList().setValue("I_VKORG", "1000")
        rfcDeliveryList.execute(destination)
        JCoTable shipments = rfcDeliveryList.getTableParameterList().getTable("SHIPMENTS")

        for (int i = 0; i < shipments.getNumRows(); i++) {
            BigInteger key = shipments.getLong("VBELN")
            String search = "SELECT id FROM delivery_head WHERE id = ${key}"
            sql.query(search) {
                resultSet ->
                    while (resultSet.next()) {
                        BigInteger id = resultSet.getLong('id')
                        sql.execute("DELETE FROM delivery_head WHERE id = ${id}")
                        println("Delivery number ${id} deleted")
                    }
            }
            if (key > 0)
                getShipmentDetails(destination, key)
        }

    }

    private void getShipmentDetails(JCoDestination destination, BigInteger id) {

        String txtId = id.toString()
        int diff = 10 - txtId.length()
        def range = [1..diff]
        // Add initial zeroes, if the length of field less than 10
        if (diff > 0) {
            range.each { txtId = '0' + txtId }
        }

        JCoFunction rfcDelivery = destination
                .getRepository()
                .getFunction("Z_RFC_GET_SHIPMENT")

        rfcDelivery.getImportParameterList().setValue("I_VBELN", txtId)
        rfcDelivery.execute(destination)
        ShipmentHead head = new ShipmentHead()
        JCoStructure likp = rfcDelivery.getExportParameterList().getStructure("HEADER")
        for (JCoField f in likp) {
            switch (f.name) {
                case "VBELN":
                    head.id = f.getBigInteger()
                    break
                case "VKORG":
                    head.companyId = f.getString()
                    break
                case "KUNAG":
                    head.partnerId = '1-' + f.getString()
                    break
                case "WADAT":
                    head.postingDate = new java.sql.Date(f.getDate().getTime())
                    break

            }
        } // each
        head.deliveryTypeId = 2
        head.amount = new BigDecimal(0.00)
        String sqlCommand = buildSqlCommand(head)
        sql.executeInsert(sqlCommand)
        println("Shipment ${head.id} created")

    }

    // Build the SQL inserted command
    private String buildSqlCommand(ShipmentHead head) {
        def command = new StringBuilder()
        command << "INSERT INTO delivery_head "
        command << "(id, company_id, delivery_type_id, "
        command << "partner_id, posting_date, amount) "
        command << " VALUES (${head.id}, \'${head.companyId}\', ${head.deliveryTypeId}, \'${head.partnerId}\', "
        command << "\'${head.postingDate}\', ${head.amount})"
        command.toString()
    }

    private void getInvoices(JCoDestination destination, String date) {

    }

    private void getInvoiceDetails(JCoDestination destination, BigInteger id) {

    }

    private void showAboutExecution(String name, BigInteger v1, BigInteger v2) {

        String unit
        BigInteger delta = ((v2 - v1) / 1000).toBigInteger()

        if (delta < 60) {
            unit = 'sec'
        } else {
            delta /= 60
            if (delta < 60) {
                unit = 'min'
            } else {
                delta /= 60
                if (delta > 60) {
                    unit = 'hours'
                }
            }
        }

        println("Thread hallowed by thy name ${name}, Time of execution is: ${delta} ${unit}")

    }

}
