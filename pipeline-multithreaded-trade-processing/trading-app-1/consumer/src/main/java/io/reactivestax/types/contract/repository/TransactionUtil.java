package io.reactivestax.types.contract.repository;

public interface TransactionUtil {

   void startTransaction();

   void commitTransaction();

   void rollbackTransaction();
}
