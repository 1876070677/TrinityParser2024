package cuk.api.VisitLog;


import cuk.api.VisitLog.Entities.VisitLog;
import cuk.api.VisitLog.Request.CreateRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VisitLogDAO {
    public List<VisitLog> getVisitLogs(int cursor);

    public void createVisitLog(CreateRequest createRequest);

    public void incrLikes(int id);
}
