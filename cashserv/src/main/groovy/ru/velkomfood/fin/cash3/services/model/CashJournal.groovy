package ru.velkomfood.fin.cash3.services.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class CashJournal {

    BigInteger id
    String cajoNumber
    String companyId
    java.sql.Date postingDate
    BigInteger year
    String positionText
    BigInteger deliveryId
    BigDecimal amount

}

@EqualsAndHashCode
class ShipmentHead {

    private BigInteger id
    private String companyId
    private BigInteger deliveryTypeId
    private String partnerId
    private java.sql.Date postingDate
    private BigDecimal amount

}

@EqualsAndHashCode
class ShipmentItem {

    BigInteger id
    BigInteger position
    String description
    BigInteger materialId
    BigDecimal quantity
    BigDecimal price
    BigDecimal netPrice;
    BigDecimal grossPrice
    BigDecimal vat
    BigInteger vatRate

}
