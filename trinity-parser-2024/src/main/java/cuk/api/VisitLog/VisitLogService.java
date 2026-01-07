package cuk.api.VisitLog;

import cuk.api.VisitLog.Entities.VisitLog;
import cuk.api.VisitLog.Request.CreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VisitLogService {

    private final VisitLogDAO visitLogDAO;

    @Autowired
    public VisitLogService(VisitLogDAO visitLogDAO) {
        this.visitLogDAO = visitLogDAO;
    }
    public List<VisitLog> getVisitLogs(int cursor) {
        return visitLogDAO.getVisitLogs(cursor);
    }
    @Transactional
    public void createVisitLog(CreateRequest createRequest) {
        visitLogDAO.createVisitLog(createRequest);
    }

    @Transactional
    public void incrLikes(int id) {
        visitLogDAO.incrLikes(id);
    }
}
