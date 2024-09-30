package io.reactivestax.service;

import io.reactivestax.domain.AccountPosition;

public interface PositionUpdateService {
   String updatePosition(AccountPosition accountPosition);
}
