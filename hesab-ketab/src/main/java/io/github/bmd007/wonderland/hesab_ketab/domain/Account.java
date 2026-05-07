package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@With
@Builder
public record Account(UUID id, String name, BigDecimal balance, long version, Instant createdAt) {

    //check expectedVersion
    public SuccessOrFailure apply(DomainEvent accountEvent) {
        return switch (accountEvent) {
            case AccountEvent.MoneyCredited credited -> {
                var updated = withBalance(balance.add(credited.amount()))
                    .withVersion(credited.atAccountVersion() + 1);
                yield SuccessOrFailure.done(updated, credited);
            }
            case AccountEvent.MoneyDebited debited -> {
                if (balance.compareTo(debited.amount()) < 0) {
                    yield SuccessOrFailure.notEnoughMoney(this, debited);
                }
                var updated = withBalance(balance.subtract(debited.amount()))
                    .withVersion(debited.atAccountVersion() + 1);
                yield SuccessOrFailure.done(updated, debited);
            }
            default -> throw new IllegalStateException("Unexpected value: " + accountEvent);
        };
    }

    public record SuccessOrFailure(Account finalState, boolean succeed, AccountEvent event, String reason) {
        public static SuccessOrFailure notEnoughMoney(Account finalState, AccountEvent event) {
            return new SuccessOrFailure(finalState, false, event, "Not Enough Money");
        }

        public static SuccessOrFailure done(Account finalState, AccountEvent event) {
            return new SuccessOrFailure(finalState, true, event, null);
        }

        public static SuccessOrFailure empty(Account account) {
            return new SuccessOrFailure(account, true, null, null);
        }
    }
}
