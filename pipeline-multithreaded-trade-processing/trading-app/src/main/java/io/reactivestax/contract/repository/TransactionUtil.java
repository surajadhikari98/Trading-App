package io.reactivestax.contract.repository;

public interface TransactionUtil {

   void startTransaction();

   void commitTransaction();

   void rollbackTransaction();
}
