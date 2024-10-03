package io.reactivestax.contract.repository;

public interface PayloadRepository {
    String[] insertTradeIntoTradePayloadTable(String filePath) throws Exception;
}
