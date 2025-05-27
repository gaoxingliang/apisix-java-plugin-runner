package org.apache.apisix.plugin.runner.db;

import org.apache.apisix.plugin.runner.datasource.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface KafkaDelayAccessRecordDao {
    @DynDataSource(DataSourceEnum.LOGGING)
    int insert(KafkaDelayAccessRecord record);
}