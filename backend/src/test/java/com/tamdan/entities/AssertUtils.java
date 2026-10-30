package com.tamdan.entities;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;

public class AssertUtils {

    public static Comparator<ZonedDateTime> zonedDateTimeSameInstant = Comparator.nullsFirst((ZonedDateTime e1, ZonedDateTime a2) ->
        e1.withZoneSameInstant(ZoneOffset.UTC).compareTo(a2.withZoneSameInstant(ZoneOffset.UTC))
    );

    public static Comparator<BigDecimal> bigDecimalCompareTo = Comparator.nullsFirst((left, right) -> left.compareTo(right));
}
