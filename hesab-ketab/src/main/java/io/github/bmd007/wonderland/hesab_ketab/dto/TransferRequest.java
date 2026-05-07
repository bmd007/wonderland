package io.github.bmd007.wonderland.hesab_ketab.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID fromAccountId,
                              UUID toAccountId,
                              BigDecimal amount,
                              String description) {
}
