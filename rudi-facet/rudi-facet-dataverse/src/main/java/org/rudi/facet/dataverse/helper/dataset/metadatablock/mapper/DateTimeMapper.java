package org.rudi.facet.dataverse.helper.dataset.metadatablock.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import org.rudi.common.core.util.DateTimeUtils;
import org.springframework.stereotype.Component;

@Component
public class DateTimeMapper {
	private static final ZoneId DATAVERSE_TIMESTAMP_V2_ZONE_ID = DateTimeUtils.UTC_ZONE_ID;

	private static final int NANO_IN_A_SECOND = 1000000000;

	@Nonnull
	public String toDataverseTimestamp(@Nonnull OffsetDateTime source) {
		final ZonedDateTime utc = source.atZoneSameInstant(DATAVERSE_TIMESTAMP_V2_ZONE_ID);
		final long epochSecond = utc.toEpochSecond();
		final long epochNano = epochSecond * NANO_IN_A_SECOND + source.getNano();
		return Long.toString(epochNano);
	}

	@Nonnull
	public OffsetDateTime fromDataverseTimestamp(@Nonnull String dataverseTimestampString) {
		final long dataverseTimestamp = Long.parseLong(dataverseTimestampString);
		final long epochSecond = dataverseTimestamp / NANO_IN_A_SECOND;
		final long nanoAdjustment = dataverseTimestamp % NANO_IN_A_SECOND;
		final Instant instant = Instant.ofEpochSecond(epochSecond, nanoAdjustment);
		return OffsetDateTime.ofInstant(instant, DATAVERSE_TIMESTAMP_V2_ZONE_ID);
	}

}
