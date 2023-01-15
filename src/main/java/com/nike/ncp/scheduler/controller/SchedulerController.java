package com.nike.ncp.scheduler.controller;

import com.nike.ncp.scheduler.controller.annotation.PermissionLimit;
import com.nike.ncp.scheduler.core.model.JourneyInfo;
import com.nike.ncp.scheduler.core.model.JourneyLogRes;
import com.nike.ncp.scheduler.core.model.JourneyNextStart;
import com.nike.ncp.scheduler.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("/v1/scheduler")
public class SchedulerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerController.class);

    public static final String JOURNEY_ID = "journeyId";

    public static final String DEFAULT_PAGE_SIZE = "20";

    public static final String PAGE = "page";

    public static final String SIZE = "size";

    public static final String STATUS = "status";

    public static final String FILTER_TIME = "filterTime";

    @Resource
    private SchedulerService schedulerService;

    @PostMapping("/jobs")
    @PermissionLimit(limit = false)
    public ResponseEntity<JourneyNextStart> createJob(HttpServletRequest request,
                                                      @RequestBody JourneyInfo journeyInfo) throws URISyntaxException {
        JourneyNextStart journeyNextStart = schedulerService.addJobs(journeyInfo);
        return ResponseEntity.created(new URI(request.getRequestURI() + "/")).body(journeyNextStart);
    }

    @PutMapping("/jobs/{journeyId}")
    @PermissionLimit(limit = false)
    public ResponseEntity<JourneyNextStart> modifyJob(@PathVariable(JOURNEY_ID) String journeyId,
                                                      @RequestBody JourneyInfo journeyInfo) {
        JourneyNextStart journeyNextStart = schedulerService.modifyJob(journeyId, journeyInfo);
        return ResponseEntity.ok().body(journeyNextStart);
    }

    @GetMapping("/autoStart")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> autoStartJobs() {
        schedulerService.autoStartJobs();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/autoStop")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> autoStopJobs() {
        schedulerService.autoStopJobs();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/jobs/{journeyId}/start")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> manualStartJobs(@PathVariable(JOURNEY_ID) String journeyId) {
        schedulerService.manualStartJobs(journeyId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/jobs/{journeyId}/stop")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> manualStopJobs(@PathVariable(JOURNEY_ID) String journeyId) {
        schedulerService.manualStopJobs(journeyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/jobs/{journeyId}")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> deleteJobs(@PathVariable(JOURNEY_ID) String journeyId) {
        schedulerService.deleteJobs(journeyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jobs/{journeyId}/recs")
    @PermissionLimit(limit = false)
    public ResponseEntity<JourneyLogRes> queryJobExeRecs(@PathVariable(JOURNEY_ID) String journeyId,
                                                         @RequestParam(name = PAGE, defaultValue = "1") int page,
                                                         @RequestParam(name = SIZE, defaultValue = DEFAULT_PAGE_SIZE) int size,
                                                         @RequestParam(name = STATUS, required = false) int status,
                                                         @RequestParam(name = FILTER_TIME, required = false) String filterTime) {
        JourneyLogRes journeyLogRes = schedulerService.queryJobExeRecs(journeyId, page, size, status, filterTime);
        return ResponseEntity.ok().body(journeyLogRes);
    }

    @GetMapping("/jobs/{journeyId}/nextStart")
    @PermissionLimit(limit = false)
    public ResponseEntity<JourneyNextStart> queryJobNextStart(@PathVariable(JOURNEY_ID) String journeyId) {
        JourneyNextStart journeyNextStartResponse = schedulerService.queryJobNextStart(journeyId);
        return ResponseEntity.ok().body(journeyNextStartResponse);
    }
}
