package org.example.spqr.models.dm;

import lombok.Builder;
import lombok.Value;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.example.spqr.utils.DateUtils.toDateOrNull;
import static org.example.spqr.utils.DateUtils.toZonedDateTime;

@Value
@Builder(builderClassName = "Builder")
public class EntitySearchSetDm {

    String entityId;
    List<String> entityIds;

    Date startSearchTime;
    String partitionLowerBoundaryDate;
    Date partitionLowerBoundaryTime;

    Date endSearchTime;
    String partitionUpperBoundaryDate;
    Date partitionUpperBoundaryTime;

    Date creationTime;
    Date updateTime;

    public static class Builder {
        private final DateTimeFormatter oldFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        private final java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            this.entityIds = singletonList(entityId);
            return this;
        }

        public Builder startSearchTime(DateTime startSearchTime) {
            if (startSearchTime == null) return this;
            DateTime startSearchTimeWithDefaultZone = startSearchTime.withZone(DateTimeZone.getDefault());
            this.startSearchTime = startSearchTimeWithDefaultZone.toDate();
            DateTime partitionLowerBoundary = startSearchTimeWithDefaultZone.withTimeAtStartOfDay();
            this.partitionLowerBoundaryTime = partitionLowerBoundary.toDate();
            this.partitionLowerBoundaryDate = oldFormatter.print(partitionLowerBoundary);
            return this;
        }

        public Builder endSearchTime(DateTime endSearchTime) {
            if (endSearchTime == null) return this;
            DateTime endSearchTimeWithDefaultZone = endSearchTime.withZone(DateTimeZone.getDefault());
            this.endSearchTime = endSearchTimeWithDefaultZone.toDate();
            DateTime partitionUpperBoundary = endSearchTimeWithDefaultZone.plusDays(1).withTimeAtStartOfDay();
            this.partitionUpperBoundaryTime = partitionUpperBoundary.toDate();
            this.partitionUpperBoundaryDate = oldFormatter.print(partitionUpperBoundary);
            return this;
        }

        public Builder updateTime(DateTime updateTime) {
            if (updateTime == null) return this;
            this.updateTime = updateTime.toDate();
            return this;
        }

        public Builder creationTime(ZonedDateTime creationTime) {
            if (creationTime == null) return this;

            this.creationTime = toDateOrNull(creationTime);

            ZonedDateTime partitionLowerBoundary = creationTime.toLocalDate().atStartOfDay(creationTime.getZone());
            this.partitionLowerBoundaryTime = toDateOrNull(partitionLowerBoundary);
            this.partitionLowerBoundaryDate = partitionLowerBoundary.format(formatter);

            ZonedDateTime partitionUpperBoundary = creationTime.toLocalDate().plusDays(1).atStartOfDay(creationTime.getZone());
            this.partitionUpperBoundaryTime = toDateOrNull(partitionUpperBoundary);
            this.partitionUpperBoundaryDate = partitionUpperBoundary.format(formatter);
            return this;
        }
    }
}
